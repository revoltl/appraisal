package com.ruivo.appraisal.machine;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.IntSupplier;

/**
 * 分页存储的容器视图。页内序号加上页偏移得到真实条目，读取与取出都走方块实体。
 */
public class StorageContainer implements Container {

    private final AppraisalMachineBlockEntity machine;
    private final IntSupplier page;

    public StorageContainer(AppraisalMachineBlockEntity machine, IntSupplier page) {
        this.machine = machine;
        this.page = page;
    }

    private int absolute(int slot) {
        return page.getAsInt() * StorageHandler.VISIBLE + slot;
    }

    @Override
    public int getContainerSize() {
        return StorageHandler.VISIBLE;
    }

    @Override
    public boolean isEmpty() {
        return machine.getStorage().isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        List<Item> keys = machine.getStorageKeys();
        int index = absolute(slot);
        if (index < 0 || index >= keys.size()) {
            return ItemStack.EMPTY;
        }
        Item item = keys.get(index);
        return new ItemStack(item, machine.getStorage().getOrDefault(item, 0));
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return machine.takeStorage(absolute(slot), amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return machine.takeStorage(absolute(slot), AppraisalMachineBlockEntity.STORAGE_LIMIT);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        // 只读视图，回写忽略。
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
    }
}
