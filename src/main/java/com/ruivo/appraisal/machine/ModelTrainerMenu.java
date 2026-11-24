package com.ruivo.appraisal.machine;

import com.ruivo.appraisal.appraisal.AppraisalEngine;
import com.ruivo.appraisal.item.BlockDataCardItem;
import com.ruivo.appraisal.item.ExperienceDataCardItem;
import com.ruivo.appraisal.plugin.PluginItem;
import com.ruivo.appraisal.registry.ModBlocks;
import com.ruivo.appraisal.registry.ModMenus;
import com.ruivo.appraisal.registry.ModTags;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/**
 * 模型训练机菜单。主页面四个功能槽，插件页面九个插件槽，两页共用一个菜单。
 */
public class ModelTrainerMenu extends AbstractContainerMenu {

    public static final int PAGE_MAIN = 0;
    public static final int PAGE_PLUGIN = 1;

    public static final int DATA_ENERGY = 0;
    public static final int DATA_ENERGY_MAX = 1;
    public static final int DATA_COOLANT = 2;
    public static final int DATA_COOLANT_MAX = 3;
    public static final int DATA_PROGRESS = 4;
    public static final int DATA_PROGRESS_TOTAL = 5;
    public static final int DATA_LEVEL = 6;
    public static final int DATA_REQUIRED_LEVEL = 7;
    public static final int DATA_TRAINING = 8;
    public static final int DATA_PAGE = 9;
    public static final int DATA_STATUS = 10;
    public static final int DATA_CAN_START = 11;
    public static final int DATA_COUNT = 12;

    public static final int BUTTON_START = 100;
    public static final int BUTTON_PAGE_MAIN = 101;
    public static final int BUTTON_PAGE_PLUGIN = 102;

    public static final int MACHINE_SLOT_COUNT = ModelTrainerBlockEntity.SLOT_COUNT;
    public static final int PLAYER_SLOT_START = MACHINE_SLOT_COUNT;

    private final ModelTrainerBlockEntity machine;
    private final ContainerData data;
    private final int[] cached = new int[DATA_COUNT];
    private int page = PAGE_MAIN;

    public ModelTrainerMenu(int containerId, Inventory inventory, FriendlyByteBuf buf) {
        this(containerId, inventory,
                (ModelTrainerBlockEntity) inventory.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public ModelTrainerMenu(int containerId, Inventory inventory, ModelTrainerBlockEntity machine) {
        super(ModMenus.MODEL_TRAINER.get(), containerId);
        this.machine = machine;
        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                if (isClientSide()) {
                    return cached[index];
                }
                cached[index] = switch (index) {
                    case DATA_ENERGY -> machine.getEnergyStored();
                    case DATA_ENERGY_MAX -> machine.getMaxEnergyStored();
                    case DATA_COOLANT -> machine.getCoolantAmount();
                    case DATA_COOLANT_MAX -> machine.getCoolantCapacity();
                    case DATA_PROGRESS -> machine.getProgress();
                    case DATA_PROGRESS_TOTAL -> machine.getProgressTotal();
                    case DATA_LEVEL -> machine.getAppraisalLevel();
                    case DATA_REQUIRED_LEVEL -> machine.getRequiredLevel();
                    case DATA_TRAINING -> machine.isTraining() ? 1 : 0;
                    case DATA_PAGE -> page;
                    case DATA_STATUS -> machine.status();
                    case DATA_CAN_START -> machine.canStartTraining() ? 1 : 0;
                    default -> 0;
                };
                return cached[index];
            }

            @Override
            public void set(int index, int value) {
                cached[index] = value;
                if (index == DATA_PAGE) {
                    page = value;
                }
            }

            @Override
            public int getCount() {
                return DATA_COUNT;
            }
        };

        addSlot(new MachineSlot(machine.getInventory(), ModelTrainerBlockEntity.SLOT_BLOCK_CARD,
                62, 30, PAGE_MAIN));
        addSlot(new MachineSlot(machine.getInventory(), ModelTrainerBlockEntity.SLOT_MODEL_PLUGIN,
                80, 30, PAGE_MAIN));
        addSlot(new MachineSlot(machine.getInventory(), ModelTrainerBlockEntity.SLOT_EXP_CARD,
                98, 30, PAGE_MAIN));
        addSlot(new MachineSlot(machine.getInventory(), ModelTrainerBlockEntity.SLOT_TARGET,
                131, 30, PAGE_MAIN));

        for (int index = 0; index < ModelTrainerBlockEntity.PLUGIN_COUNT; index++) {
            int column = index % 3;
            int row = index / 3;
            addSlot(new MachineSlot(machine.getInventory(), ModelTrainerBlockEntity.PLUGIN_START + index,
                    62 + column * 18, 15 + row * 18, PAGE_PLUGIN));
        }

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 142));
        }

        addDataSlots(data);
    }

    private boolean isClientSide() {
        return machine.getLevel() != null && machine.getLevel().isClientSide();
    }

    public ModelTrainerBlockEntity getMachine() {
        return machine;
    }

    public int getData(int index) {
        return data.get(index);
    }

    public int getPage() {
        return page;
    }

    public com.ruivo.appraisal.plugin.PluginStats getPluginStats() {
        return machine.pluginStats();
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == BUTTON_START) {
            machine.startTraining();
            broadcastChanges();
            return true;
        }
        if (id == BUTTON_PAGE_MAIN || id == BUTTON_PAGE_PLUGIN) {
            page = id == BUTTON_PAGE_MAIN ? PAGE_MAIN : PAGE_PLUGIN;
            broadcastChanges();
            return true;
        }
        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(machine.getLevel(), machine.getBlockPos()),
                player, ModBlocks.MODEL_TRAINER.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        if (index < MACHINE_SLOT_COUNT) {
            if (!moveItemStackTo(stack, PLAYER_SLOT_START, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (page == PAGE_PLUGIN) {
            if (!moveItemStackTo(stack, ModelTrainerBlockEntity.PLUGIN_START,
                    MACHINE_SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, ModelTrainerBlockEntity.SLOT_TARGET + 1, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }

    private class MachineSlot extends SlotItemHandler {

        private final int visiblePage;

        MachineSlot(IItemHandler handler, int index, int x, int y, int visiblePage) {
            super(handler, index, x, y);
            this.visiblePage = visiblePage;
        }

        @Override
        public boolean isActive() {
            return page == visiblePage;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            int index = getContainerSlot();
            if (index == ModelTrainerBlockEntity.SLOT_BLOCK_CARD) {
                return stack.getItem() instanceof BlockDataCardItem;
            }
            if (index == ModelTrainerBlockEntity.SLOT_MODEL_PLUGIN) {
                return stack.is(ModTags.Items.MODEL_TRAINING_PLUGINS);
            }
            if (index == ModelTrainerBlockEntity.SLOT_EXP_CARD) {
                return stack.getItem() instanceof ExperienceDataCardItem;
            }
            if (index == ModelTrainerBlockEntity.SLOT_TARGET) {
                if (!(stack.getItem() instanceof BlockItem blockItem)) {
                    return false;
                }
                return machine.getLevel() != null && AppraisalEngine.findRecipe(
                        machine.getLevel(), blockItem.getBlock().defaultBlockState()) != null;
            }
            if (!(stack.getItem() instanceof PluginItem plugin)) {
                return false;
            }
            if (!stack.is(ModTags.Items.PLUGINS)) {
                return false;
            }
            int limit = plugin.installLimit();
            return limit == Integer.MAX_VALUE || machine.countPlugin(plugin) + stack.getCount() <= limit;
        }
    }
}
