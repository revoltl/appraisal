package com.ruivo.appraisal.machine;

import com.ruivo.appraisal.appraisal.AppraisalEngine;
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
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/**
 * 鉴定机菜单。四个页面共用一个菜单，槽位全部注册，非当前页面的槽位通过 isActive 隐藏。
 */
public class AppraisalMachineMenu extends AbstractContainerMenu {

    public static final int PAGE_MAIN = 0;
    public static final int PAGE_STORAGE = 1;
    public static final int PAGE_PLUGIN = 2;

    public static final int DATA_ENERGY = 0;
    public static final int DATA_ENERGY_MAX = 1;
    public static final int DATA_COOLANT = 2;
    public static final int DATA_COOLANT_MAX = 3;
    public static final int DATA_PROGRESS = 4;
    public static final int DATA_PROGRESS_TOTAL = 5;
    public static final int DATA_LEVEL = 6;
    public static final int DATA_HAS_COOLANT = 7;
    public static final int DATA_PAGE = 8;
    public static final int DATA_STORAGE_PAGE = 9;
    public static final int DATA_REQUIRED_LEVEL = 10;
    public static final int DATA_COUNT = 11;

    public static final int BUTTON_STORAGE_BASE = 100;
    public static final int BUTTON_PAGE_PREV = 200;
    public static final int BUTTON_PAGE_NEXT = 201;

    /** 菜单槽位下标。注册顺序是输入槽、存储槽、插件槽、玩家背包。 */
    public static final int INPUT_SLOT_INDEX = 0;
    public static final int STORAGE_SLOT_START = INPUT_SLOT_INDEX + 1;
    public static final int STORAGE_SLOT_COUNT = StorageHandler.VISIBLE;
    public static final int STORAGE_SLOT_END = STORAGE_SLOT_START + STORAGE_SLOT_COUNT;
    public static final int PLUGIN_SLOT_START = STORAGE_SLOT_END;
    public static final int PLUGIN_SLOT_END = PLUGIN_SLOT_START + AppraisalMachineBlockEntity.PLUGIN_COUNT;
    public static final int PLAYER_SLOT_START = PLUGIN_SLOT_END;

    private final AppraisalMachineBlockEntity machine;
    private final ContainerData data;
    private final StorageHandler storageHandler;
    private final StorageContainer storageContainer;
    private final int[] cached = new int[DATA_COUNT];
    private int page = PAGE_MAIN;
    private int storagePage;

    public AppraisalMachineMenu(int containerId, Inventory inventory, FriendlyByteBuf buf) {
        this(containerId, inventory, (AppraisalMachineBlockEntity) inventory.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public AppraisalMachineMenu(int containerId, Inventory inventory, AppraisalMachineBlockEntity machine) {
        super(ModMenus.APPRAISAL_MACHINE.get(), containerId);
        this.machine = machine;
        this.storageHandler = new StorageHandler(machine);
        this.storageContainer = new StorageContainer(machine, () -> storagePage);
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
                    case DATA_HAS_COOLANT -> machine.hasCoolant() ? 1 : 0;
                    case DATA_PAGE -> page;
                    case DATA_STORAGE_PAGE -> storagePage;
                    case DATA_REQUIRED_LEVEL -> machine.getRequiredLevel();
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
                if (index == DATA_STORAGE_PAGE) {
                    storagePage = value;
                }
            }

            @Override
            public int getCount() {
                return DATA_COUNT;
            }
        };

        addSlot(new PageSlot(machine.getInventory(), AppraisalMachineBlockEntity.INPUT_SLOT, 52, 33, PAGE_MAIN, true));

        for (int i = 0; i < StorageHandler.VISIBLE; i++) {
            int column = i % 9;
            int row = i / 9;
            addSlot(new StorageSlot(storageContainer, i, 8 + column * 18, 15 + row * 18));
        }

        for (int i = 0; i < AppraisalMachineBlockEntity.PLUGIN_COUNT; i++) {
            int column = i % 3;
            int row = i / 3;
            addSlot(new PageSlot(machine.getInventory(), AppraisalMachineBlockEntity.PLUGIN_START + i,
                    62 + column * 18, 15 + row * 18, PAGE_PLUGIN, false));
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

    public AppraisalMachineBlockEntity getMachine() {
        return machine;
    }

    public com.ruivo.appraisal.plugin.PluginStats getPluginStats() {
        return machine.pluginStats();
    }

    public int getData(int index) {
        return data.get(index);
    }

    public int getPage() {
        return page;
    }


    public int getStoragePages() {
        int size = machine.getStorageKeys().size();
        return Math.max(1, (size + StorageHandler.VISIBLE - 1) / StorageHandler.VISIBLE);
    }

    public void handleStoragePage(int target) {
        storagePage = net.minecraft.util.Mth.clamp(target, 0, getStoragePages() - 1);
        broadcastChanges();
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id >= PAGE_MAIN && id <= PAGE_PLUGIN) {
            page = id;
            broadcastChanges();
            return true;
        }
        if (id == BUTTON_PAGE_PREV || id == BUTTON_PAGE_NEXT) {
            int pages = getStoragePages();
            storagePage = net.minecraft.util.Mth.clamp(
                    storagePage + (id == BUTTON_PAGE_PREV ? -1 : 1), 0, pages - 1);
            broadcastChanges();
            return true;
        }
        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(machine.getLevel(), machine.getBlockPos()),
                player, ModBlocks.APPRAISAL_MACHINE.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        if (index >= STORAGE_SLOT_START && index < STORAGE_SLOT_END) {
            // 存储页只读，shift 点击不取出。
            return ItemStack.EMPTY;
        }

        if (index >= PLUGIN_SLOT_START && index < PLUGIN_SLOT_END) {
            if (!moveItemStackTo(stack, PLAYER_SLOT_START, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (index == INPUT_SLOT_INDEX) {
            if (!moveItemStackTo(stack, PLAYER_SLOT_START, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (page == PAGE_PLUGIN) {
            if (!moveItemStackTo(stack, PLUGIN_SLOT_START, PLUGIN_SLOT_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (page == PAGE_MAIN) {
            if (!moveItemStackTo(stack, INPUT_SLOT_INDEX, INPUT_SLOT_INDEX + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            // 存储页只处理取出，背包物品不在这里进入机器。
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }


    private class StorageSlot extends Slot {

        StorageSlot(StorageContainer container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean isActive() {
            return page == PAGE_STORAGE;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }

        @Override
        public boolean allowModification(Player player) {
            return false;
        }

        @Override
        public void set(ItemStack stack) {
            // 只读槽位，任何写入都忽略。
        }
    }

    private class PageSlot extends SlotItemHandler {

        private final int visiblePage;
        private final boolean inputSlot;

        PageSlot(IItemHandler handler, int index, int x, int y, int visiblePage, boolean inputSlot) {
            super(handler, index, x, y);
            this.visiblePage = visiblePage;
            this.inputSlot = inputSlot;
        }

        @Override
        public boolean isActive() {
            return page == visiblePage;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            if (inputSlot) {
                if (!(stack.getItem() instanceof net.minecraft.world.item.BlockItem blockItem)) {
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
            if (limit != Integer.MAX_VALUE && machine.countPlugin(plugin) + stack.getCount() > limit) {
                return false;
            }
            return true;
        }
    }
}
