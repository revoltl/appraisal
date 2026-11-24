package com.ruivo.appraisal.plugin;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 插件参数聚合。机器与界面都从这里取合计值，不直接判断具体插件。
 */
public final class PluginAggregator {

    private PluginAggregator() {
    }

    public static PluginStats aggregate(Iterable<ItemStack> stacks) {
        List<PluginSpec> specs = new ArrayList<>();
        for (ItemStack stack : stacks) {
            if (!stack.isEmpty() && stack.getItem() instanceof PluginItem plugin) {
                specs.add(plugin.spec());
            }
        }
        return aggregateSpecs(specs);
    }

    public static PluginStats aggregateSpecs(Iterable<PluginSpec> specs) {
        return PluginStats.combine(specs);
    }
}
