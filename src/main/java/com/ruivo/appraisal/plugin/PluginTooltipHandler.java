package com.ruivo.appraisal.plugin;

import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * 插件 Tooltip 的事件入口。物品只要是 {@link PluginItem}，就按它的参数追加说明。
 */
public final class PluginTooltipHandler {

    private PluginTooltipHandler() {
    }

    public static void onTooltip(ItemTooltipEvent event) {
        if (event.getItemStack().getItem() instanceof PluginItem plugin) {
            event.getToolTip().addAll(PluginTooltip.lines(plugin.spec()));
        }
    }
}
