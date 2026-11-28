package com.ruivo.appraisal.plugin.tool;

import com.ruivo.appraisal.registry.ModDataComponents;
import com.ruivo.appraisal.registry.ModTags;
import com.ruivo.appraisal.plugin.PluginItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * 鉴定工具上的九个插件槽。数据存在物品组件里，菜单打开时读取，槽位变化时写回。
 */
public final class ToolPluginInventory extends ItemStackHandler {

    public static final int SLOTS = 9;

    private final ItemStack tool;
    private boolean loading;

    public ToolPluginInventory(ItemStack tool) {
        super(SLOTS);
        this.tool = tool;
        List<ItemStack> stored = tool.getOrDefault(ModDataComponents.TOOL_PLUGINS.get(), List.of());
        loading = true;
        for (int index = 0; index < Math.min(stored.size(), SLOTS); index++) {
            setStackInSlot(index, stored.get(index).copy());
        }
        loading = false;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        if (!(stack.getItem() instanceof PluginItem plugin) || !stack.is(ModTags.Items.PLUGINS)) {
            return false;
        }
        int limit = plugin.installLimit();
        if (limit == Integer.MAX_VALUE) {
            return true;
        }
        int count = 0;
        for (int index = 0; index < SLOTS; index++) {
            if (getStackInSlot(index).getItem() == plugin) {
                count++;
            }
        }
        return count + 1 <= limit;
    }

    @Override
    protected void onContentsChanged(int slot) {
        if (!loading) {
            save();
        }
    }

    public void save() {
        List<ItemStack> stacks = new ArrayList<>(SLOTS);
        for (int index = 0; index < SLOTS; index++) {
            stacks.add(getStackInSlot(index).copy());
        }
        tool.set(ModDataComponents.TOOL_PLUGINS.get(), List.copyOf(stacks));
        if (tool.getItem() instanceof PluginConfigurableTool configurable) {
            configurable.onPluginsChanged(tool);
        }
    }
}
