package com.ruivo.appraisal.machine;

import com.ruivo.appraisal.AppraisalConfig;
import com.ruivo.appraisal.appraisal.AppraisalDrop;
import com.ruivo.appraisal.appraisal.AppraisalEngine;
import com.ruivo.appraisal.appraisal.AppraisalMode;
import com.ruivo.appraisal.plugin.ModelPluginItem;
import com.ruivo.appraisal.plugin.PluginAggregator;
import com.ruivo.appraisal.plugin.PluginItem;
import com.ruivo.appraisal.plugin.PluginStats;
import com.ruivo.appraisal.recipe.AppraisalOutput;
import com.ruivo.appraisal.recipe.AppraisalRecipe;
import com.ruivo.appraisal.registry.ModBlockEntities;
import com.ruivo.appraisal.training.TrainingResults;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 鉴定机方块实体。
 *
 * <p>鉴定等级由机器等级与插件等级相加得到，与玩家、附魔、药水无关。只有进阶鉴定，
 * 产物按独立概率结算，每种产物最多存一组。</p>
 */
public class AppraisalMachineBlockEntity extends BlockEntity implements MenuProvider {

    public static final int INPUT_SLOT = 0;
    public static final int PLUGIN_START = 1;
    public static final int PLUGIN_COUNT = 9;
    public static final int SLOT_COUNT = PLUGIN_START + PLUGIN_COUNT;
    public static final int STORAGE_LIMIT = 64;
    public static final int COOLANT_CAPACITY = 8_000;

    private final ItemStackHandler inventory = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private final MachineEnergyStorage energy = new MachineEnergyStorage(
            AppraisalConfig.MACHINE_ENERGY_CAPACITY.get(),
            AppraisalConfig.MACHINE_ENERGY_TRANSFER.get(),
            0);

    /**
     * 能量存储。容量和传输上限随电容插件变化，拆下插件时把多余能量压回新容量。
     */
    private final class MachineEnergyStorage extends EnergyStorage {

        MachineEnergyStorage(int capacity, int maxReceive, int maxExtract) {
            super(capacity, maxReceive, maxExtract, 0);
        }

        void setCapacity(int value) {
            this.capacity = Math.max(1, value);
            if (this.energy > this.capacity) {
                this.energy = this.capacity;
            }
        }

        void setTransferLimit(int value) {
            this.maxReceive = Math.max(1, value);
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public int extractEnergy(int toExtract, boolean simulate) {
            return 0;
        }

        /**
         * 机器自身消耗能量。外部无法抽取，所以内部消耗单独走这里，不经过 extractEnergy。
         */
        int consume(int amount) {
            int consumed = Math.min(this.energy, Math.max(0, amount));
            this.energy -= consumed;
            return consumed;
        }

        void addEnergy(int amount) {
            if (amount <= 0) {
                return;
            }
            this.energy = Math.min(this.capacity, this.energy + amount);
            setChanged();
        }

        @Override
        public int receiveEnergy(int toReceive, boolean simulate) {
            int received = super.receiveEnergy(toReceive, simulate);
            if (received > 0 && !simulate) {
                setChanged();
            }
            return received;
        }
    }

    private final FluidTank coolant = new FluidTank(COOLANT_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };

    private final Map<Item, Integer> storage = new LinkedHashMap<>();
    private final AutomationHandler automationHandler = new AutomationHandler(this);

    private final int[] faceConfig = new int[6];
    private int redstoneMode;

    private int machineLevel;
    private int progress;
    private int progressTotal;
    private int requiredLevel;

    public AppraisalMachineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.APPRAISAL_MACHINE.get(), pos, state);
        this.machineLevel = AppraisalConfig.MACHINE_LEVEL.get();
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) {
            return;
        }
        if (level.hasNeighborSignal(pos)) {
            return;
        }

        PluginStats stats = pluginStats();
        int targetCapacity = energyCapacity(stats);
        if (energy.getMaxEnergyStored() != targetCapacity) {
            energy.setCapacity(targetCapacity);
        }
        energy.setTransferLimit((int) Math.round(AppraisalConfig.MACHINE_ENERGY_TRANSFER.get() * stats.transferMultiplier()));
        energy.addEnergy(SolarGeneration.fePerTick(stats, level, pos));

        ItemStack input = inventory.getStackInSlot(INPUT_SLOT);
        if (input.isEmpty()) {
            progress = 0;
            return;
        }

        BlockState inputState = stateOf(input);
        if (inputState == null) {
            progress = 0;
            return;
        }

        AppraisalRecipe recipe = AppraisalEngine.findRecipe(level, inputState);
        if (recipe == null) {
            progress = 0;
            return;
        }

        int levelValue = machineLevel + stats.levelBonus();
        requiredLevel = recipe.minLevel();
        if (levelValue < recipe.minLevel()) {
            progress = 0;
            return;
        }

        Map<Item, Double> bonusChances = new HashMap<>();
        Set<Item> whitelist = new HashSet<>();
        boolean suppressOthers = false;
        var trainedBlock = recipe.input().representativeBlock();
        for (int index = PLUGIN_START; index < SLOT_COUNT; index++) {
            ItemStack pluginStack = inventory.getStackInSlot(index);
            if (!(pluginStack.getItem() instanceof ModelPluginItem)) {
                continue;
            }
            Map<ResourceLocation, Double> trained = TrainingResults.read(pluginStack)
                    .get(BuiltInRegistries.BLOCK.getKey(trainedBlock));
            if (trained != null) {
                for (Map.Entry<ResourceLocation, Double> entry : trained.entrySet()) {
                    Item item = BuiltInRegistries.ITEM.get(entry.getKey());
                    bonusChances.merge(item, entry.getValue(), Double::sum);
                    whitelist.add(item);
                }
            }
            if (TrainingResults.isSuppressing(pluginStack)) {
                suppressOthers = true;
            }
        }

        if (!canProduce(recipe, levelValue, stats.voidEnabled(), suppressOthers, whitelist)) {
            progress = 0;
            return;
        }

        int coolantCost = (int) Math.ceil(coolant.getCapacity() * stats.coolantFraction());
        if (coolantCost > 0 && coolant.getFluidAmount() < coolantCost) {
            progress = 0;
            return;
        }

        int baseDuration = Math.max(1, recipe.duration());
        int effectiveDuration = Math.max(1, (int) Math.round(baseDuration / stats.speedMultiplier()));
        int perTick = (int) Math.ceil((double) recipe.energy() * stats.energyMultiplier() / effectiveDuration);
        if (perTick > 0 && energy.getEnergyStored() < perTick) {
            return;
        }

        energy.consume(perTick);
        progressTotal = effectiveDuration;
        progress++;

        if (progress >= effectiveDuration) {
            progress = 0;
            List<AppraisalDrop> drops = AppraisalEngine.roll(
                    recipe, AppraisalMode.ADVANCED, levelValue, level.random,
                    stats.chanceMultiplier(), bonusChances, suppressOthers, whitelist);
            for (AppraisalDrop drop : drops) {
                insert(new ItemStack(drop.item().value(), drop.count()));
            }
            if (coolantCost > 0) {
                coolant.drain(coolantCost,
                        net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
            }
            if (level.random.nextDouble() < stats.blockConsumeChance()) {
                input.shrink(1);
            }
            setChanged();
        }
    }

    public int getAppraisalLevel() {
        return machineLevel + pluginStats().levelBonus();
    }

    /**
     * 汇总九个插件槽的参数。每次调用重新计算，插件槽最多九个，开销可以忽略。
     */
    public PluginStats pluginStats() {
        List<ItemStack> stacks = new java.util.ArrayList<>(PLUGIN_COUNT);
        for (int i = PLUGIN_START; i < SLOT_COUNT; i++) {
            stacks.add(inventory.getStackInSlot(i));
        }
        return PluginAggregator.aggregate(stacks);
    }

    /**
     * 当前等级下的可行产物是否还有存储空间。正常模式任意一个产物满就停，
     * 虚空插件开启后只要还有一个产物未满就继续。
     */
    private boolean canProduce(AppraisalRecipe recipe, int level, boolean voidEnabled,
                               boolean suppressOthers, Set<Item> whitelist) {
        boolean anyRoom = false;
        for (AppraisalOutput output : recipe.outputs()) {
            if (output.minLevel() > level || output.probability() <= 0.0D) {
                continue;
            }
            Item item = output.result().item().value();
            if (suppressOthers && !whitelist.contains(item)) {
                continue;
            }
            if (isFull(item)) {
                if (!voidEnabled) {
                    return false;
                }
            } else {
                anyRoom = true;
            }
        }
        return anyRoom;
    }

    private boolean isFull(Item item) {
        return storage.getOrDefault(item, 0) >= storageCapacity(item);
    }

    /**
     * 单个产物的存储上限。全局上限六十四，再取物品自身的堆叠上限，
     * 堆叠十六或一的物品不会被强行存到六十四。
     */
    public int storageCapacity(Item item) {
        return Math.min(STORAGE_LIMIT, new ItemStack(item).getMaxStackSize());
    }

    private static BlockState stateOf(ItemStack stack) {
        if (stack.getItem() instanceof BlockItem blockItem) {
            return blockItem.getBlock().defaultBlockState();
        }
        return null;
    }

    private void insert(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        Item item = stack.getItem();
        int capacity = storageCapacity(item);
        int current = storage.getOrDefault(item, 0);
        int space = capacity - current;
        if (space <= 0) {
            return;
        }
        storage.put(item, current + Math.min(space, stack.getCount()));
        sync();
    }


    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void sync() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public java.util.List<Item> getStorageKeys() {
        return new java.util.ArrayList<>(storage.keySet());
    }

    public void removeStorage(Item item, int amount) {
        int current = storage.getOrDefault(item, 0);
        int left = current - amount;
        if (left <= 0) {
            storage.remove(item);
        } else {
            storage.put(item, left);
        }
        setChanged();
        sync();
    }

    public ItemStack takeStorage(int index, int amount) {
        java.util.List<Item> keys = getStorageKeys();
        if (index < 0 || index >= keys.size()) {
            return ItemStack.EMPTY;
        }
        Item item = keys.get(index);
        int stored = storage.getOrDefault(item, 0);
        int taken = Math.min(stored, amount);
        if (taken <= 0) {
            return ItemStack.EMPTY;
        }
        removeStorage(item, taken);
        return new ItemStack(item, taken);
    }

    public boolean extractStorage(int index, Player player) {
        java.util.List<Item> keys = new java.util.ArrayList<>(storage.keySet());
        if (index < 0 || index >= keys.size()) {
            return false;
        }
        Item item = keys.get(index);
        int count = storage.getOrDefault(item, 0);
        if (count <= 0) {
            return false;
        }
        ItemStack stack = new ItemStack(item, count);
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
        storage.remove(item);
        setChanged();
        sync();
        return true;
    }

    public void dropContents() {
        if (level == null) {
            return;
        }
        for (int i = 0; i < SLOT_COUNT; i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                        worldPosition.getZ() + 0.5D, stack);
            }
        }
        for (Map.Entry<Item, Integer> entry : storage.entrySet()) {
            Containers.dropItemStack(level, worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                    worldPosition.getZ() + 0.5D, new ItemStack(entry.getKey(), entry.getValue()));
        }
        storage.clear();
    }

    public int getFaceConfig(int index) {
        return faceConfig[index];
    }

    public void cycleFaceConfig(int index) {
        faceConfig[index] = (faceConfig[index] + 1) % 3;
        setChanged();
        sync();
    }

    public int getRedstoneMode() {
        return redstoneMode;
    }

    public void setRedstoneMode(int mode) {
        redstoneMode = Math.max(0, Math.min(2, mode));
        setChanged();
        sync();
    }

    public AutomationHandler getAutomationHandler() {
        return automationHandler;
    }

    public net.neoforged.neoforge.energy.IEnergyStorage getEnergyStorage() {
        return energy;
    }

    public net.neoforged.neoforge.fluids.capability.IFluidHandler getCoolantTank() {
        return coolant;
    }

    public int countPlugin(PluginItem plugin) {
        int count = 0;
        for (int index = PLUGIN_START; index < SLOT_COUNT; index++) {
            ItemStack stack = inventory.getStackInSlot(index);
            if (stack.getItem() == plugin) {
                count += stack.getCount();
            }
        }
        return count;
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public Map<Item, Integer> getStorage() {
        return storage;
    }

    public int getEnergyStored() {
        return energy.getEnergyStored();
    }

    public int getMaxEnergyStored() {
        return energy.getMaxEnergyStored();
    }

    public int getEnergyTransfer() {
        return (int) Math.round(AppraisalConfig.MACHINE_ENERGY_TRANSFER.get() * pluginStats().transferMultiplier());
    }

    private static int energyCapacity(PluginStats stats) {
        return (int) Math.round(AppraisalConfig.MACHINE_ENERGY_CAPACITY.get() * stats.capacityMultiplier());
    }

    public int getCoolantAmount() {
        return coolant.getFluidAmount();
    }

    public int getCoolantCapacity() {
        return coolant.getCapacity();
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }

    public int getProgress() {
        return progress;
    }

    public int getProgressTotal() {
        return progressTotal;
    }

    public int getMachineLevel() {
        return machineLevel;
    }

    public boolean hasCoolant() {
        return !coolant.isEmpty();
    }

    public static void registerCapabilities(net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.APPRAISAL_MACHINE.get(), (machine, side) -> machine.automationHandler);
        event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.APPRAISAL_MACHINE.get(), (machine, side) -> machine.energy);
        event.registerBlockEntity(net.neoforged.neoforge.capabilities.Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.APPRAISAL_MACHINE.get(), (machine, side) -> machine.coolant);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.appraisal.appraisal_machine");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new AppraisalMachineMenu(containerId, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        tag.put("energy", energy.serializeNBT(registries));
        CompoundTag coolantTag = new CompoundTag();
        coolant.writeToNBT(registries, coolantTag);
        tag.put("coolant", coolantTag);
        tag.putInt("machineLevel", machineLevel);
        for (int i = 0; i < faceConfig.length; i++) {
            tag.putInt("face" + i, faceConfig[i]);
        }
        tag.putInt("redstone", redstoneMode);
        tag.putInt("progress", progress);
        tag.putInt("progressTotal", progressTotal);

        CompoundTag storageTag = new CompoundTag();
        for (Map.Entry<Item, Integer> entry : storage.entrySet()) {
            storageTag.putInt(BuiltInRegistries.ITEM.getKey(entry.getKey()).toString(), entry.getValue());
        }
        tag.put("storage", storageTag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("inventory")) {
            inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        }
        if (tag.contains("energy")) {
            energy.deserializeNBT(registries, tag.get("energy"));
        }
        PluginStats stats = pluginStats();
        energy.setCapacity(energyCapacity(stats));
        energy.setTransferLimit((int) Math.round(AppraisalConfig.MACHINE_ENERGY_TRANSFER.get() * stats.transferMultiplier()));
        if (tag.contains("coolant")) {
            coolant.readFromNBT(registries, tag.getCompound("coolant"));
        }
        machineLevel = tag.contains("machineLevel") ? tag.getInt("machineLevel") : AppraisalConfig.MACHINE_LEVEL.get();
        for (int i = 0; i < faceConfig.length; i++) {
            faceConfig[i] = tag.getInt("face" + i);
        }
        redstoneMode = tag.getInt("redstone");
        progress = tag.getInt("progress");
        progressTotal = tag.getInt("progressTotal");

        storage.clear();
        CompoundTag storageTag = tag.getCompound("storage");
        for (String key : storageTag.getAllKeys()) {
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(key));
            if (item != Items.AIR) {
                int amount = storageTag.getInt(key);
                if (amount > 0) {
                    storage.put(item, Math.min(amount, storageCapacity(item)));
                }
            }
        }
    }
}
