package com.ruivo.appraisal.machine;

import com.ruivo.appraisal.AppraisalConfig;

import com.ruivo.appraisal.plugin.PluginItem;
import com.ruivo.appraisal.plugin.PluginSpec;
import com.ruivo.appraisal.plugin.PluginStats;
import com.ruivo.appraisal.recipe.DecayGenerationRecipe;
import com.ruivo.appraisal.registry.ModBlockEntities;
import com.ruivo.appraisal.registry.ModRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * 衰变发电机。中心槽放衰变输入物，四个角放插件。
 *
 * <p>产能只和机器自身有关，输入物只决定衰变时长。能量不接受外部输入，
 * 每 tick 主动向相邻方块推送，推送总量受传输速度限制。</p>
 */
public class DecayGeneratorBlockEntity extends BlockEntity {

    public static final int SLOT_CENTER = 0;
    public static final int PLUGIN_START = 1;
    public static final int PLUGIN_COUNT = 4;
    public static final int SLOT_COUNT = PLUGIN_START + PLUGIN_COUNT;

    public static final int BASE_CAPACITY = 100_000;
    public static final int BASE_TRANSFER = 400;
    public static final int BASE_GENERATION = 200;

    private final ItemStackHandler inventory = new ItemStackHandler(SLOT_COUNT) {
        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
            }
        }
    };

    private final GeneratorEnergy energy = new GeneratorEnergy();
    private final IItemHandler automation = new IItemHandler() {
        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return inventory.getStackInSlot(SLOT_CENTER);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (stack.isEmpty() || !isDecayInput(stack)) {
                return stack;
            }
            if (!inventory.getStackInSlot(SLOT_CENTER).isEmpty()) {
                return stack;
            }
            if (!simulate) {
                inventory.setStackInSlot(SLOT_CENTER, stack.copyWithCount(1));
            }
            return stack.getCount() <= 1 ? ItemStack.EMPTY : stack.copyWithCount(stack.getCount() - 1);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            ItemStack existing = inventory.getStackInSlot(SLOT_CENTER);
            if (existing.isEmpty() || !isDecayOutput(existing)) {
                return ItemStack.EMPTY;
            }
            return inventory.extractItem(SLOT_CENTER, Math.min(amount, 1), simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return isDecayInput(stack);
        }
    };

    private int progress;

    public DecayGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DECAY_GENERATOR.get(), pos, state);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) {
            return;
        }
        if (level.hasNeighborSignal(pos)) {
            return;
        }

        PluginStats stats = pluginStats();
        int targetCapacity = (int) Math.round(
                AppraisalConfig.DECAY_GENERATOR_CAPACITY.get() * stats.capacityMultiplier());
        if (energy.getMaxEnergyStored() != targetCapacity) {
            energy.setCapacity(targetCapacity);
        }
        int transfer = (int) Math.round(
                AppraisalConfig.DECAY_GENERATOR_TRANSFER.get() * stats.transferMultiplier());
        energy.setTransfer(transfer);

        int solar = SolarGeneration.fePerTick(stats, level, pos);
        energy.addEnergy(solar);

        DecayGenerationRecipe recipe = findRecipe();
        if (recipe != null) {
            progress++;
            int base = (int) Math.round(
                    AppraisalConfig.DECAY_GENERATOR_GENERATION.get()
                            * stats.generatorEfficiencyMultiplier());
            energy.addEnergy(base);
            if (progress >= recipe.time()) {
                inventory.setStackInSlot(SLOT_CENTER, recipe.output().copy());
                progress = 0;
            }
            setChanged();
        } else if (progress != 0) {
            progress = 0;
            setChanged();
        }

        chargeTools(transfer);
        pushEnergy(level, pos, transfer);
    }

    private void chargeTools(int budget) {
        for (int index = PLUGIN_START; index < SLOT_COUNT; index++) {
            if (budget <= 0 || energy.getEnergyStored() <= 0) {
                return;
            }
            ItemStack stack = inventory.getStackInSlot(index);
            if (stack.isEmpty()) {
                continue;
            }
            IEnergyStorage tool = stack.getCapability(Capabilities.EnergyStorage.ITEM);
            if (tool == null || !tool.canReceive()) {
                continue;
            }
            int available = Math.min(budget, energy.getEnergyStored());
            int accepted = tool.receiveEnergy(available, true);
            if (accepted <= 0) {
                continue;
            }
            int extracted = energy.extractEnergy(accepted, false);
            if (extracted > 0) {
                tool.receiveEnergy(extracted, false);
                inventory.setStackInSlot(index, stack);
                budget -= extracted;
            }
        }
    }

    private void pushEnergy(Level level, BlockPos pos, int budget) {
        for (Direction direction : Direction.values()) {
            if (budget <= 0 || energy.getEnergyStored() <= 0) {
                return;
            }
            IEnergyStorage target = level.getCapability(Capabilities.EnergyStorage.BLOCK,
                    pos.relative(direction), direction.getOpposite());
            if (target == null || !target.canReceive()) {
                continue;
            }
            int available = Math.min(budget, energy.getEnergyStored());
            int accepted = target.receiveEnergy(available, true);
            if (accepted <= 0) {
                continue;
            }
            int extracted = energy.extractEnergy(accepted, false);
            if (extracted > 0) {
                target.receiveEnergy(extracted, false);
                budget -= extracted;
            }
        }
    }

    public DecayGenerationRecipe findRecipe() {
        ItemStack stack = inventory.getStackInSlot(SLOT_CENTER);
        if (stack.isEmpty() || level == null) {
            return null;
        }
        return level.getRecipeManager()
                .getRecipeFor(ModRecipes.DECAY_GENERATION_TYPE.get(), new SingleRecipeInput(stack), level)
                .map(RecipeHolder::value)
                .orElse(null);
    }

    public boolean isDecayInput(ItemStack stack) {
        if (stack.isEmpty() || level == null) {
            return false;
        }
        return level.getRecipeManager()
                .getRecipeFor(ModRecipes.DECAY_GENERATION_TYPE.get(), new SingleRecipeInput(stack), level)
                .isPresent();
    }

    public boolean isDecayOutput(ItemStack stack) {
        if (stack.isEmpty() || level == null) {
            return false;
        }
        for (RecipeHolder<DecayGenerationRecipe> holder : level.getRecipeManager()
                .getAllRecipesFor(ModRecipes.DECAY_GENERATION_TYPE.get())) {
            if (stack.is(holder.value().output().getItem())) {
                return true;
            }
        }
        return false;
    }

    public PluginStats pluginStats() {
        List<PluginSpec> specs = new ArrayList<>(PLUGIN_COUNT);
        for (int index = PLUGIN_START; index < SLOT_COUNT; index++) {
            ItemStack stack = inventory.getStackInSlot(index);
            if (stack.getItem() instanceof PluginItem plugin) {
                specs.add(plugin.spec());
            }
        }
        return PluginStats.combine(specs);
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

    public IEnergyStorage getEnergy() {
        return energy;
    }

    public int getProgress() {
        return progress;
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.DECAY_GENERATOR.get(), (generator, side) -> generator.automation);
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.DECAY_GENERATOR.get(), (generator, side) -> generator.energy);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        tag.putInt("energy", energy.getEnergyStored());
        tag.putInt("progress", progress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        PluginStats stats = pluginStats();
        energy.setCapacity((int) Math.round(
                AppraisalConfig.DECAY_GENERATOR_CAPACITY.get() * stats.capacityMultiplier()));
        energy.setTransfer((int) Math.round(
                AppraisalConfig.DECAY_GENERATOR_TRANSFER.get() * stats.transferMultiplier()));
        energy.setEnergy(tag.getInt("energy"));
        progress = tag.getInt("progress");
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    private final class GeneratorEnergy extends EnergyStorage {

        GeneratorEnergy() {
            super(AppraisalConfig.DECAY_GENERATOR_CAPACITY.get(), 0,
                    AppraisalConfig.DECAY_GENERATOR_TRANSFER.get(), 0);
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return 0;
        }

        @Override
        public boolean canReceive() {
            return false;
        }

        void addEnergy(int amount) {
            energy = Math.min(capacity, energy + amount);
        }

        void setEnergy(int value) {
            energy = Math.max(0, Math.min(capacity, value));
        }

        void setCapacity(int value) {
            capacity = Math.max(1, value);
            if (energy > capacity) {
                energy = capacity;
            }
        }

        void setTransfer(int value) {
            maxExtract = Math.max(1, value);
        }
    }
}
