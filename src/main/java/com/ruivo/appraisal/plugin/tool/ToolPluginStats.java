package com.ruivo.appraisal.plugin.tool;

import com.ruivo.appraisal.plugin.ModelPluginItem;
import com.ruivo.appraisal.plugin.PluginSpec;
import com.ruivo.appraisal.plugin.PluginItem;
import com.ruivo.appraisal.plugin.PluginStats;
import com.ruivo.appraisal.registry.ModDataComponents;
import com.ruivo.appraisal.training.TrainingResults;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 工具插件参数合计。普通插件汇总成 {@link PluginStats}，分类模型插件按当前方块
 * 提供产物权重增幅和抑制白名单。
 */
public record ToolPluginStats(PluginStats general, Map<Item, Double> bonusChances,
                              boolean suppressOthers, Set<Item> whitelist) {

    public static final ToolPluginStats EMPTY = new ToolPluginStats(
            PluginStats.EMPTY, Map.of(), false, Set.of());

    public static PluginStats general(ItemStack tool) {
        List<ItemStack> plugins = tool.getOrDefault(ModDataComponents.TOOL_PLUGINS.get(), List.of());
        List<PluginSpec> specs = new ArrayList<>(plugins.size());
        for (ItemStack pluginStack : plugins) {
            if (pluginStack.getItem() instanceof PluginItem plugin) {
                specs.add(plugin.spec());
            }
        }
        return PluginStats.combine(specs);
    }

    public static ToolPluginStats of(ItemStack tool, Block block) {
        List<ItemStack> plugins = tool.getOrDefault(ModDataComponents.TOOL_PLUGINS.get(), List.of());
        Map<Item, Double> bonusChances = new HashMap<>();
        Set<Item> whitelist = new HashSet<>();
        boolean suppressOthers = false;

        for (ItemStack pluginStack : plugins) {
            if (!(pluginStack.getItem() instanceof ModelPluginItem) || block == null) {
                continue;
            }
            Map<ResourceLocation, Double> trained = TrainingResults.read(pluginStack)
                    .get(BuiltInRegistries.BLOCK.getKey(block));
            if (trained != null) {
                for (Map.Entry<ResourceLocation, Double> entry : trained.entrySet()) {
                    Item item = BuiltInRegistries.ITEM.get(entry.getKey());
                    bonusChances.merge(item, entry.getValue(), Double::sum);
                    whitelist.add(item);
                }
            }
            if (TrainingResults.isSuppressing(pluginStack)) {
                suppressOthers = true;
            }
        }

        return new ToolPluginStats(general(tool), bonusChances, suppressOthers, whitelist);
    }
}
