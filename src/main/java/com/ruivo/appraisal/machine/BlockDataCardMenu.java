package com.ruivo.appraisal.machine;

import com.ruivo.appraisal.item.BlockDataCardItem;
import com.ruivo.appraisal.registry.ModMenus;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * 方块数据卡的存储面板。二十七格，内容写回物品的容器组件。
 */
public class BlockDataCardMenu extends AbstractContainerMenu {

    private final Inventory playerInventory;
    private final ItemStack cardStack;
    private final ItemStackHandler handler;
    private boolean loading;

    public BlockDataCardMenu(int containerId, Inventory inventory, FriendlyByteBuf buffer) {
        this(containerId, inventory,
                ItemStack.OPTIONAL_STREAM_CODEC.decode((RegistryFriendlyByteBuf) buffer));
    }

    public BlockDataCardMenu(int containerId, Inventory inventory, ItemStack cardStack) {
        super(ModMenus.BLOCK_DATA_CARD.get(), containerId);
        this.playerInventory = inventory;
        this.cardStack = cardStack;
        this.handler = new ItemStackHandler(BlockDataCardItem.SLOTS) {
            @Override
            protected void onContentsChanged(int slot) {
                if (!loading) {
                    saveContents();
                }
            }
        };
        loadContents();

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new SlotItemHandler(handler, column + row * 9,
                        8 + column * 18, 18 + row * 18) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return !(stack.getItem() instanceof BlockDataCardItem);
                    }
                });
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9,
                        8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 142));
        }
    }

    private void loadContents() {
        loading = true;
        ItemContainerContents contents = cardStack.getOrDefault(
                DataComponents.CONTAINER, ItemContainerContents.EMPTY);
        List<ItemStack> items = contents.stream().toList();
        for (int index = 0; index < Math.min(items.size(), handler.getSlots()); index++) {
            handler.setStackInSlot(index, items.get(index).copy());
        }
        loading = false;
    }

    private void saveContents() {
        List<ItemStack> items = new ArrayList<>();
        for (int index = 0; index < handler.getSlots(); index++) {
            items.add(handler.getStackInSlot(index).copy());
        }
        cardStack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(items));
        playerInventory.player.getInventory().setChanged();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        if (index < BlockDataCardItem.SLOTS) {
            if (!moveItemStackTo(stack, BlockDataCardItem.SLOTS, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, BlockDataCardItem.SLOTS, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void removed(Player player) {
        saveContents();
        super.removed(player);
    }
}
