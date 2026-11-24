package com.ruivo.appraisal.plugin;

import com.ruivo.appraisal.training.TrainingResults;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 分类模型插件。除普通插件参数外，带一个固定的模型强度，训练结果写在物品组件里。
 */
public class ModelPluginItem extends PluginItem {

    private final ModelFamily family;
    private final double modelStrength;

    public ModelPluginItem(Properties properties, PluginSpec spec, ChatFormatting nameColor,
                           ModelFamily family, double modelStrength) {
        super(properties, spec, nameColor);
        this.family = family;
        this.modelStrength = modelStrength;
    }

    public ModelFamily family() {
        return family;
    }

    public double modelStrength() {
        return modelStrength;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip,
                                TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.appraisal.model_plugin.strength",
                        String.format(Locale.ROOT, "%.2f", modelStrength))
                .withStyle(ChatFormatting.GRAY));

        Map<ResourceLocation, Map<ResourceLocation, Double>> results = TrainingResults.read(stack);
        boolean suppress = TrainingResults.isSuppressing(stack);
        if (!results.isEmpty() || suppress) {
            tooltip.add(Component.translatable("tooltip.appraisal.model_plugin.training")
                    .withStyle(ChatFormatting.GOLD));
        }
        if (suppress) {
            tooltip.add(Component.translatable("tooltip.appraisal.model_plugin.suppress")
                    .withStyle(ChatFormatting.RED));
        }
        if (results.isEmpty()) {
            return;
        }
        int shown = 0;
        for (Map.Entry<ResourceLocation, Map<ResourceLocation, Double>> blockEntry : results.entrySet()) {
            var block = BuiltInRegistries.BLOCK.get(blockEntry.getKey());
            for (Map.Entry<ResourceLocation, Double> itemEntry : blockEntry.getValue().entrySet()) {
                if (shown >= 5) {
                    tooltip.add(Component.translatable("tooltip.appraisal.model_plugin.training.more")
                            .withStyle(ChatFormatting.DARK_GRAY));
                    return;
                }
                var item = BuiltInRegistries.ITEM.get(itemEntry.getKey());
                tooltip.add(Component.translatable("tooltip.appraisal.model_plugin.training.line",
                                block.getName(), item.getDescription(), percent(itemEntry.getValue()))
                        .withStyle(ChatFormatting.GRAY));
                shown++;
            }
        }
    }

    private static String percent(double value) {
        double percent = value * 100.0D;
        if (Math.abs(percent - Math.rint(percent)) < 0.05D) {
            return String.format(Locale.ROOT, "%.0f%%", percent);
        }
        return String.format(Locale.ROOT, "%.1f%%", percent);
    }
}
