package com.ruivo.appraisal.plugin.tool;

import com.ruivo.appraisal.item.AppraisalTool;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/**
 * 可配置插件的鉴定工具。工具自己的物品类实现这个接口，
 * 面板、等级、能耗和产物权重都通过这里读取插件数据。
 */
public interface PluginConfigurableTool extends AppraisalTool {

    ToolPluginInventory toolPlugins(ItemStack stack);

    default ToolPluginStats toolPluginStats(ItemStack stack, Block block) {
        return ToolPluginStats.of(stack, block);
    }

    default void onPluginsChanged(ItemStack stack) {
    }

    @Override
    default int pluginLevelBonus(ItemStack stack) {
        return toolPluginStats(stack, null).general().levelBonus();
    }

    @Override
    default int effectiveAppraisalLevel(ItemStack stack) {
        return appraisalLevel(stack) + pluginLevelBonus(stack);
    }
}
