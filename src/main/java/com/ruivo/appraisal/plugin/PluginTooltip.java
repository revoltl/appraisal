package com.ruivo.appraisal.plugin;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 插件 Tooltip 的生成。遍历 {@link PluginStat}，按参数正负决定颜色，好的排前面，
 * 坏的排后面，组内保持枚举顺序。具体插件不在这里出现。
 */
public final class PluginTooltip {

    public static final int GOOD_COLOR = 0x55FF55;
    public static final int BAD_COLOR = 0xFF5555;

    private PluginTooltip() {
    }

    public static List<Component> lines(PluginSpec spec) {
        List<Component> result = new ArrayList<>();
        int limit = spec.installLimit();
        if (limit == Integer.MAX_VALUE) {
            result.add(Component.translatable("tooltip.appraisal.plugin.install_limit.unlimited")
                    .withStyle(ChatFormatting.GRAY));
        } else {
            result.add(Component.translatable("tooltip.appraisal.plugin.install_limit", limit)
                    .withStyle(ChatFormatting.GRAY));
        }

        List<Component> good = new ArrayList<>();
        List<Component> bad = new ArrayList<>();

        for (PluginStat stat : PluginStat.values()) {
            double value = spec.get(stat);
            if (value == 0.0D) {
                continue;
            }
            if (stat.format() == PluginStat.Format.BOOLEAN) {
                if (value > 0.0D) {
                    good.add(Component.translatable(stat.tooltipKey())
                            .withStyle(style -> style.withColor(GOOD_COLOR)));
                }
                continue;
            }

            boolean beneficial = value > 0.0D ? stat.positiveIsGood() : !stat.positiveIsGood();
            Component line = Component.translatable(stat.tooltipKey(), format(stat, value))
                    .withStyle(style -> style.withColor(beneficial ? GOOD_COLOR : BAD_COLOR));
            (beneficial ? good : bad).add(line);
        }

        result.addAll(good);
        result.addAll(bad);
        return result;
    }

    private static String format(PluginStat stat, double value) {
        return switch (stat.format()) {
            case PERCENT -> String.format(Locale.ROOT, "%+.0f%%", value * 100.0D);
            case LEVEL -> String.format(Locale.ROOT, "%+d", (int) Math.round(value));
            case BOOLEAN -> "";
        };
    }
}
