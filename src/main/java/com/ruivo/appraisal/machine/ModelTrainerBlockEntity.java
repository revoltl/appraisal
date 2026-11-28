package com.ruivo.appraisal.machine;

import com.ruivo.appraisal.AppraisalConfig;
import com.ruivo.appraisal.appraisal.AppraisalEngine;
import com.ruivo.appraisal.experience.ExperienceRules;
import com.ruivo.appraisal.item.BlockDataCardItem;
import com.ruivo.appraisal.item.ExperienceDataCardItem;
import com.ruivo.appraisal.plugin.ModelNames;
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
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 模型训练机。四个功能槽放方块数据卡、分类模型插件、经验数据卡和待鉴定方块，
 * 另外九个插件槽沿用鉴定机的插件体系。
 *
 * <p>训练时长基础六千刻，速度插件可以缩短。完成时消耗方块数据卡里所有可训练的产物，
 * 把经验数据卡压到对应阶段，并把倍率写进分类模型插件。</p>
 */
public class ModelTrainerBlockEntity extends BlockEntity implements MenuProvider {

    public static final int SLOT_BLOCK_CARD = 0;
    public static final int SLOT_MODEL_PLUGIN = 1;
    public static final int SLOT_EXP_CARD = 2;
    public static final int SLOT_TARGET = 3;
    public static final int PLUGIN_START = 4;
    public static final int PLUGIN_COUNT = 9;
    public static final int SLOT_COUNT = PLUGIN_START + PLUGIN_COUNT;

    public static final int COOLANT_CAPACITY = 8_000;
    public static final int BASE_TRAINING_TIME = 6_000;

    public static final int STATUS_READY = 0;
    public static final int STATUS_MISSING_TARGET = 1;
    public static final int STATUS_MISSING_MODEL_PLUGIN = 2;
    public static final int STATUS_LEVEL_TOO_LOW = 3;
    public static final int STATUS_MISSING_BLOCK_CARD = 4;
    public static final int STATUS_NO_TRAINING_DATA = 5;
    public static final int STATUS_MISSING_EXP_CARD = 6;
    public static final int STATUS_DIMENSION_BREACH = 7;
    public static final int STATUS_TRAINING = 8;
    public static final int STATUS_NO_ENERGY = 9;
    public static final int STATUS_NO_COOLANT = 10;

    private static final int COOLANT_TICKS_PER_CHARGE = 400;

    private final ItemStackHandler inventory = new ItemStackHandler(SLOT_COUNT) {
        @Override
        public int getSlotLimit(int slot) {
            return slot == SLOT_TARGET ? 1 : 64;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
            }
        }
    };

    private final MachineEnergyStorage energy = new MachineEnergyStorage();
    private final FluidTank coolant = new FluidTank(COOLANT_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };

    private boolean training;
    private int progress;
    private int progressTotal;
    private int requiredLevel;

    @Nullable
    private Plan plan;

    private record Entry(Item item, int count, double multiplier) {
    }

    private record Plan(Block block, List<Entry> entries, int experienceCost, int duration) {
    }

    public ModelTrainerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MODEL_TRAINER.get(), pos, state);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) {
            return;
        }
        if (level.hasNeighborSignal(pos)) {
            return;
        }

        PluginStats stats = pluginStats();
        energy.setCapacity((int) Math.round(
                AppraisalConfig.MODEL_TRAINER_ENERGY_CAPACITY.get() * stats.capacityMultiplier()));
        energy.setTransferLimit((int) Math.round(
                AppraisalConfig.MODEL_TRAINER_ENERGY_TRANSFER.get() * stats.transferMultiplier()));
        energy.addEnergy(SolarGeneration.fePerTick(stats, level, pos));

        if (!training) {
            return;
        }

        Plan current = plan;
        if (current == null) {
            current = buildPlan(stats);
            if (current == null) {
                cancelTraining();
                return;
            }
            plan = current;
            progressTotal = current.duration();
            if (progress > progressTotal) {
                progress = progressTotal;
            }
        }

        AppraisalRecipe recipe = currentRecipe();
        if (recipe == null || getAppraisalLevel() < recipe.minLevel()
                || !planStillValid(current, recipe)) {
            cancelTraining();
            return;
        }

        int duration = current.duration();
        int perTick = (int) Math.ceil((double) recipe.energy() * stats.energyMultiplier() / duration);
        if (perTick > 0 && energy.getEnergyStored() < perTick) {
            return;
        }

        int coolantPerTick = coolantPerTick(stats);
        if (coolantPerTick > 0 && coolant.getFluidAmount() < coolantPerTick) {
            return;
        }

        energy.consume(perTick);
        if (coolantPerTick > 0) {
            coolant.drain(coolantPerTick, IFluidHandler.FluidAction.EXECUTE);
        }
        progressTotal = duration;
        progress++;
        if (progress >= duration) {
            completeTraining(current);
        }
    }

    /**
     * 训练机每刻消耗的冷却液。每四百刻消耗一次容量比例对应的量，六千刻大约十五份。
     */
    private int coolantPerTick(PluginStats stats) {
        double total = coolant.getCapacity() * stats.coolantFraction();
        return (int) Math.ceil(total / COOLANT_TICKS_PER_CHARGE);
    }

    /**
     * 当前是否满足开始条件。训练中返回真，界面上的按钮此时用来停止训练。
     */
    public boolean canStartTraining() {
        if (training) {
            return true;
        }
        return status() == STATUS_READY;
    }

    /**
     * 当前训练准备状态。界面按这个值显示开始按钮上方的提示。
     */
    public int status() {
        if (training) {
            Plan current = plan;
            if (current == null) {
                return STATUS_TRAINING;
            }
            AppraisalRecipe recipe = currentRecipe();
            if (recipe != null) {
                PluginStats stats = pluginStats();
                int duration = Math.max(1, current.duration());
                int perTick = (int) Math.ceil(
                        (double) recipe.energy() * stats.energyMultiplier() / duration);
                if (perTick > 0 && energy.getEnergyStored() < perTick) {
                    return STATUS_NO_ENERGY;
                }
                int coolantPerTick = coolantPerTick(stats);
                if (coolantPerTick > 0 && coolant.getFluidAmount() < coolantPerTick) {
                    return STATUS_NO_COOLANT;
                }
            }
            return STATUS_TRAINING;
        }

        if (!(inventory.getStackInSlot(SLOT_MODEL_PLUGIN).getItem() instanceof ModelPluginItem)) {
            return STATUS_MISSING_MODEL_PLUGIN;
        }
        ItemStack target = inventory.getStackInSlot(SLOT_TARGET);
        if (!(target.getItem() instanceof BlockItem)) {
            return STATUS_MISSING_TARGET;
        }
        AppraisalRecipe recipe = currentRecipe();
        if (recipe == null) {
            return STATUS_MISSING_TARGET;
        }
        PluginStats stats = pluginStats();
        int level = AppraisalConfig.MODEL_TRAINER_LEVEL.get() + stats.levelBonus();
        if (level < recipe.minLevel()) {
            return STATUS_LEVEL_TOO_LOW;
        }

        ItemStack card = inventory.getStackInSlot(SLOT_BLOCK_CARD);
        if (!(card.getItem() instanceof BlockDataCardItem)) {
            return STATUS_MISSING_BLOCK_CARD;
        }
        int kinds = countTrainable(card, recipe, level).size();
        if (kinds == 0) {
            return STATUS_NO_TRAINING_DATA;
        }
        ItemStack exp = inventory.getStackInSlot(SLOT_EXP_CARD);
        if (!(exp.getItem() instanceof ExperienceDataCardItem)) {
            return STATUS_MISSING_EXP_CARD;
        }

        int stored = ExperienceDataCardItem.getStoredExperience(exp);
        if (kinds > maxKinds(stored) || stored < experienceCost(kinds, stats)) {
            return STATUS_DIMENSION_BREACH;
        }
        return STATUS_READY;
    }

    /**
     * 开始训练。训练进行中再次点击会停止。
     */
    public boolean startTraining() {
        if (training) {
            cancelTraining();
            return true;
        }
        Plan built = buildPlan(pluginStats());
        if (built == null) {
            return false;
        }
        plan = built;
        training = true;
        progress = 0;
        progressTotal = built.duration();
        AppraisalRecipe recipe = currentRecipe();
        requiredLevel = recipe == null ? 0 : recipe.minLevel();
        setChanged();
        return true;
    }

    @Nullable
    private Plan buildPlan(PluginStats stats) {
        AppraisalRecipe recipe = currentRecipe();
        if (recipe == null) {
            return null;
        }
        int level = getAppraisalLevel();
        if (level < recipe.minLevel()) {
            return null;
        }

        ItemStack pluginStack = inventory.getStackInSlot(SLOT_MODEL_PLUGIN);
        if (!(pluginStack.getItem() instanceof ModelPluginItem modelPlugin)) {
            return null;
        }
        ItemStack cardStack = inventory.getStackInSlot(SLOT_BLOCK_CARD);
        if (!(cardStack.getItem() instanceof BlockDataCardItem)) {
            return null;
        }
        ItemStack expStack = inventory.getStackInSlot(SLOT_EXP_CARD);
        if (!(expStack.getItem() instanceof ExperienceDataCardItem)) {
            return null;
        }
        ItemStack targetStack = inventory.getStackInSlot(SLOT_TARGET);
        if (!(targetStack.getItem() instanceof BlockItem blockItem)) {
            return null;
        }

        Map<Item, Integer> counts = countTrainable(cardStack, recipe, level);
        if (counts.isEmpty()) {
            return null;
        }

        int stored = ExperienceDataCardItem.getStoredExperience(expStack);
        if (counts.size() > maxKinds(stored)) {
            return null;
        }
        int cost = experienceCost(counts.size(), stats);
        if (stored < cost) {
            return null;
        }

        double base = 0.01D * modelPlugin.modelStrength() * stats.chanceMultiplier();
        List<Entry> entries = new ArrayList<>();
        for (Map.Entry<Item, Integer> entry : counts.entrySet()) {
            entries.add(new Entry(entry.getKey(), entry.getValue(), entry.getValue() * base));
        }

        int duration = Math.max(1, (int) Math.round(
                AppraisalConfig.MODEL_TRAINER_TRAINING_TIME.get() / stats.speedMultiplier()));
        return new Plan(recipe.input().representativeBlock(), entries, cost, duration);
    }

    private Map<Item, Integer> countTrainable(ItemStack card, AppraisalRecipe recipe, int level) {
        Map<Item, Integer> counts = new LinkedHashMap<>();
        ItemContainerContents contents = card.getOrDefault(
                DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        for (int index = 0; index < contents.getSlots(); index++) {
            ItemStack stack = contents.getStackInSlot(index);
            if (stack.isEmpty()) {
                continue;
            }
            for (AppraisalOutput output : recipe.outputs()) {
                if (output.minLevel() > level) {
                    continue;
                }
                if (stack.is(output.result().item().value())) {
                    counts.merge(stack.getItem(), stack.getCount(), Integer::sum);
                    break;
                }
            }
        }
        return counts;
    }

    private static int maxKinds(int stored) {
        if (stored >= ExperienceRules.PRIOR_THREE) {
            return 27;
        }
        if (stored >= ExperienceRules.PRIOR_TWO) {
            return 18;
        }
        if (stored >= ExperienceRules.PRIOR_ONE) {
            return 9;
        }
        return 0;
    }

    private static int experienceCost(int kinds, PluginStats stats) {
        double base = kinds <= 9 ? ExperienceRules.PRIOR_ONE
                : kinds <= 18 ? ExperienceRules.PRIOR_TWO
                : ExperienceRules.PRIOR_THREE;
        return (int) Math.round(base * stats.blockConsumeChance());
    }

    /**
     * 可训练种类超过经验数据卡当前阶段的上限。界面在开始按钮上方显示红字。
     */
    public boolean isDimensionBreach() {
        AppraisalRecipe recipe = currentRecipe();
        if (recipe == null) {
            return false;
        }
        int level = getAppraisalLevel();
        if (level < recipe.minLevel()) {
            return false;
        }
        ItemStack card = inventory.getStackInSlot(SLOT_BLOCK_CARD);
        ItemStack exp = inventory.getStackInSlot(SLOT_EXP_CARD);
        if (!(card.getItem() instanceof BlockDataCardItem)
                || !(exp.getItem() instanceof ExperienceDataCardItem)) {
            return false;
        }
        int kinds = countTrainable(card, recipe, level).size();
        return kinds > maxKinds(ExperienceDataCardItem.getStoredExperience(exp));
    }

    private boolean planStillValid(Plan plan, AppraisalRecipe recipe) {
        ItemStack exp = inventory.getStackInSlot(SLOT_EXP_CARD);
        if (!(exp.getItem() instanceof ExperienceDataCardItem)
                || ExperienceDataCardItem.getStoredExperience(exp) < plan.experienceCost()) {
            return false;
        }
        ItemStack card = inventory.getStackInSlot(SLOT_BLOCK_CARD);
        if (!(card.getItem() instanceof BlockDataCardItem)) {
            return false;
        }
        Map<Item, Integer> counts = countTrainable(card, recipe, getAppraisalLevel());
        for (Entry entry : plan.entries()) {
            if (counts.getOrDefault(entry.item(), 0) < entry.count()) {
                return false;
            }
        }
        return true;
    }

    private void completeTraining(Plan plan) {
        ItemStack cardStack = inventory.getStackInSlot(SLOT_BLOCK_CARD);
        ItemStack expStack = inventory.getStackInSlot(SLOT_EXP_CARD);
        ItemStack pluginStack = inventory.getStackInSlot(SLOT_MODEL_PLUGIN);

        if (cardStack.getItem() instanceof BlockDataCardItem) {
            consumeCard(cardStack, plan.entries());
        }
        if (expStack.getItem() instanceof ExperienceDataCardItem) {
            int stored = ExperienceDataCardItem.getStoredExperience(expStack);
            ExperienceDataCardItem.setStoredExperience(expStack,
                    Math.max(0, stored - plan.experienceCost()));
        }
        if (pluginStack.getItem() instanceof ModelPluginItem) {
            Map<ResourceLocation, Double> results = new LinkedHashMap<>();
            for (Entry entry : plan.entries()) {
                results.put(BuiltInRegistries.ITEM.getKey(entry.item()), entry.multiplier());
            }
            TrainingResults.write(pluginStack, plan.block(), results,
                    pluginStats().voidEnabled());
            if (level != null) {
                ModelPluginItem modelPlugin = (ModelPluginItem) pluginStack.getItem();
                String nameKey = ModelNames.random(modelPlugin.family(), level.random);
                var customName = Component.translatable("model.appraisal.named",
                        Component.translatable(nameKey));
                var nameColor = modelPlugin.nameColor();
                if (nameColor != null) {
                    customName = customName.withStyle(nameColor);
                }
                customName = customName.withStyle(style -> style.withItalic(false));
                pluginStack.set(DataComponents.CUSTOM_NAME, customName);
            }
        }

        training = false;
        progress = 0;
        this.plan = null;
        inventory.setStackInSlot(SLOT_MODEL_PLUGIN,
                inventory.getStackInSlot(SLOT_MODEL_PLUGIN).copy());
        setChanged();
    }

    private void consumeCard(ItemStack card, List<Entry> entries) {
        ItemContainerContents contents = card.getOrDefault(
                DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        List<ItemStack> items = new ArrayList<>(contents.getSlots());
        for (int index = 0; index < contents.getSlots(); index++) {
            ItemStack stack = contents.getStackInSlot(index).copy();
            for (Entry entry : entries) {
                if (stack.is(entry.item())) {
                    stack = ItemStack.EMPTY;
                    break;
                }
            }
            items.add(stack);
        }
        card.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
    }

    private void cancelTraining() {
        training = false;
        progress = 0;
        plan = null;
        setChanged();
    }

    public AppraisalRecipe currentRecipe() {
        if (level == null) {
            return null;
        }
        ItemStack stack = inventory.getStackInSlot(SLOT_TARGET);
        if (stack.getItem() instanceof BlockItem blockItem) {
            return AppraisalEngine.findRecipe(level, blockItem.getBlock().defaultBlockState());
        }
        return null;
    }

    public int getAppraisalLevel() {
        return AppraisalConfig.MODEL_TRAINER_LEVEL.get() + pluginStats().levelBonus();
    }

    public PluginStats pluginStats() {
        List<ItemStack> stacks = new ArrayList<>(PLUGIN_COUNT);
        for (int index = PLUGIN_START; index < SLOT_COUNT; index++) {
            stacks.add(inventory.getStackInSlot(index));
        }
        return PluginAggregator.aggregate(stacks);
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

    public int getEnergyStored() {
        return energy.getEnergyStored();
    }

    public int getMaxEnergyStored() {
        return energy.getMaxEnergyStored();
    }

    public int getEnergyTransfer() {
        return energy.transferLimit();
    }

    public int getCoolantAmount() {
        return coolant.getFluidAmount();
    }

    public int getCoolantCapacity() {
        return coolant.getCapacity();
    }

    public boolean hasCoolant() {
        return !coolant.isEmpty();
    }

    public boolean isTraining() {
        return training;
    }

    public int getProgress() {
        return progress;
    }

    public int getProgressTotal() {
        return progressTotal;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK,
                ModBlockEntities.MODEL_TRAINER.get(), (trainer, side) -> trainer.energy);
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.MODEL_TRAINER.get(), (trainer, side) -> trainer.coolant);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.appraisal.model_trainer");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ModelTrainerMenu(containerId, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        tag.putInt("energy", energy.getEnergyStored());
        CompoundTag coolantTag = new CompoundTag();
        coolant.writeToNBT(registries, coolantTag);
        tag.put("coolant", coolantTag);
        tag.putBoolean("training", training);
        tag.putInt("progress", progress);
        tag.putInt("progressTotal", progressTotal);
        tag.putInt("requiredLevel", requiredLevel);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        PluginStats stats = pluginStats();
        energy.setCapacity((int) Math.round(
                AppraisalConfig.MODEL_TRAINER_ENERGY_CAPACITY.get() * stats.capacityMultiplier()));
        energy.setTransferLimit((int) Math.round(
                AppraisalConfig.MODEL_TRAINER_ENERGY_TRANSFER.get() * stats.transferMultiplier()));
        energy.setEnergy(tag.getInt("energy"));
        if (tag.contains("coolant")) {
            coolant.readFromNBT(registries, tag.getCompound("coolant"));
        }
        training = tag.getBoolean("training");
        progress = tag.getInt("progress");
        progressTotal = tag.getInt("progressTotal");
        requiredLevel = tag.getInt("requiredLevel");
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    private final class MachineEnergyStorage extends EnergyStorage {

        MachineEnergyStorage() {
            super(AppraisalConfig.MODEL_TRAINER_ENERGY_CAPACITY.get(),
                    AppraisalConfig.MODEL_TRAINER_ENERGY_TRANSFER.get(), 0, 0);
        }

        void setCapacity(int value) {
            capacity = Math.max(1, value);
            if (energy > capacity) {
                energy = capacity;
            }
        }

        void setTransferLimit(int value) {
            maxReceive = Math.max(1, value);
        }

        int transferLimit() {
            return maxReceive;
        }

        void setEnergy(int value) {
            energy = Math.max(0, Math.min(capacity, value));
        }

        int consume(int amount) {
            int consumed = Math.min(energy, Math.max(0, amount));
            energy -= consumed;
            return consumed;
        }

        void addEnergy(int amount) {
            if (amount <= 0) {
                return;
            }
            energy = Math.min(capacity, energy + amount);
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
}
