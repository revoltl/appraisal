package com.ruivo.appraisal.machine;

import com.ruivo.appraisal.plugin.PluginItem;
import com.ruivo.appraisal.plugin.PluginSpec;
import com.ruivo.appraisal.plugin.PluginStats;
import com.ruivo.appraisal.plugin.tool.PluginConfigurableTool;
import com.ruivo.appraisal.plugin.tool.ToolPluginInventory;
import com.ruivo.appraisal.registry.ModMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.SlotItemHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * 鉴定工具的插件配置面板。九个插件槽，布局与鉴定机的插件页一致。
 */
public class ToolPluginMenu extends AbstractContainerMenu {

    public static final int PLUGIN_SLOTS = ToolPluginInventory.SLOTS;

    private final ToolPluginInventory plugins;

    public ToolPluginMenu(int id, Inventory playerInventory) {
        super(ModMenus.TOOL_PLUGINS.get(), id);
        ItemStack tool = playerInventory.player.getMainHandItem();
        this.plugins = new ToolPluginInventory(tool);

        for (int index = 0; index < PLUGIN_SLOTS; index++) {
            int column = index % 3;
            int row = index / 3;
            int pluginIndex = index;
            addSlot(new SlotItemHandler(plugins, index, 62 + column * 18, 15 + row * 18) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return plugins.isItemValid(pluginIndex, stack);
                }
            });
        }

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 8 + column * 18, 142));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return player.getMainHandItem().getItem() instanceof PluginConfigurableTool;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index < PLUGIN_SLOTS) {
            if (!moveItemStackTo(stack, PLUGIN_SLOTS, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, PLUGIN_SLOTS, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        plugins.save();
    }

    public PluginStats getPluginStats() {
        List<PluginSpec> specs = new ArrayList<>(PLUGIN_SLOTS);
        for (int index = 0; index < PLUGIN_SLOTS; index++) {
            ItemStack stack = plugins.getStackInSlot(index);
            if (stack.getItem() instanceof PluginItem plugin) {
                specs.add(plugin.spec());
            }
        }
        return PluginStats.combine(specs);
    }
}
