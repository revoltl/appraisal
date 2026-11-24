package com.ruivo.appraisal.machine;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

import java.util.List;

/**
 * 产物存储的槽位视图。只读、只能取出，滚动偏移决定当前显示的窗口。
 */
public class StorageHandler implements IItemHandlerModifiable {

    public static final int VISIBLE = 27;

    private final AppraisalMachineBlockEntity machine;
    private int offset;

    public StorageHandler(AppraisalMachineBlockEntity machine) {
        this.machine = machine;
    }

    public void setOffset(int offset) {
        this.offset = Math.max(0, offset);
    }

    public int getOffset() {
        return offset;
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        // 只读视图，客户端同步写入一律忽略，内容以方块实体为准。
    }

    @Override
    public int getSlots() {
        return VISIBLE;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        List<Item> keys = machine.getStorageKeys();
        int index = offset + slot;
        if (index < 0 || index >= keys.size()) {
            return ItemStack.EMPTY;
        }
        Item item = keys.get(index);
        return new ItemStack(item, machine.getStorage().getOrDefault(item, 0));
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        return stack;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        List<Item> keys = machine.getStorageKeys();
        int index = offset + slot;
        if (index < 0 || index >= keys.size()) {
            return ItemStack.EMPTY;
        }
        Item item = keys.get(index);
        int stored = machine.getStorage().getOrDefault(item, 0);
        int taken = Math.min(stored, amount);
        if (taken <= 0) {
            return ItemStack.EMPTY;
        }
        if (!simulate) {
            machine.removeStorage(item, taken);
        }
        return new ItemStack(item, taken);
    }

    @Override
    public int getSlotLimit(int slot) {
        return AppraisalMachineBlockEntity.STORAGE_LIMIT;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return false;
    }
}
