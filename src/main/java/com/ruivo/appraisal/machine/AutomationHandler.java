package com.ruivo.appraisal.machine;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.List;

/**
 * 面向物流的自动化视图。
 *
 * <p>槽位零是输入槽，只能插入不能抽取。其余槽位是产物存储，只能抽取不能插入。
 * 插件槽不暴露，外部物流看不到也碰不到。</p>
 */
public class AutomationHandler implements IItemHandler {

    private final AppraisalMachineBlockEntity machine;

    public AutomationHandler(AppraisalMachineBlockEntity machine) {
        this.machine = machine;
    }

    @Override
    public int getSlots() {
        return 1 + machine.getStorageKeys().size();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        if (slot == 0) {
            return machine.getInventory().getStackInSlot(AppraisalMachineBlockEntity.INPUT_SLOT);
        }
        List<Item> keys = machine.getStorageKeys();
        int index = slot - 1;
        if (index < 0 || index >= keys.size()) {
            return ItemStack.EMPTY;
        }
        Item item = keys.get(index);
        return new ItemStack(item, machine.getStorage().getOrDefault(item, 0));
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (slot != 0) {
            return stack;
        }
        return machine.getInventory().insertItem(AppraisalMachineBlockEntity.INPUT_SLOT, stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (slot == 0) {
            return ItemStack.EMPTY;
        }
        List<Item> keys = machine.getStorageKeys();
        int index = slot - 1;
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
        if (slot == 0) {
            return 64;
        }
        List<Item> keys = machine.getStorageKeys();
        int index = slot - 1;
        if (index < 0 || index >= keys.size()) {
            return AppraisalMachineBlockEntity.STORAGE_LIMIT;
        }
        return machine.storageCapacity(keys.get(index));
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return slot == 0
                && machine.getInventory().isItemValid(AppraisalMachineBlockEntity.INPUT_SLOT, stack);
    }
}
