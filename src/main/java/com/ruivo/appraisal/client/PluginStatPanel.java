package com.ruivo.appraisal.client;

import com.ruivo.appraisal.plugin.PluginStat;
import com.ruivo.appraisal.plugin.PluginStats;
import com.ruivo.appraisal.plugin.PluginTooltip;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.Locale;
import java.util.Set;

/**
 * 插件属性九宫格。左栏放能量三项，右栏放鉴定六项，左栏右对齐、右栏左对齐。
 * 鉴定机、训练机和工具插件面板共用这一套绘制。
 */
public final class PluginStatPanel {

    private PluginStatPanel() {
    }

    public static void render(GuiGraphics graphics, Font font, PluginStats stats,
                              Set<PluginStat> visible, int left, int top) {
        int leftAnchor = left + 60;
        int rightX = left + 118;
        int leftY = top + 16;
        int rightY = top + 16;

        boolean any = false;
        for (PluginStat stat : PluginStat.values()) {
            if (visible.contains(stat) && statValue(stats, stat) != null) {
                any = true;
                break;
            }
        }
        if (!any) {
            graphics.drawString(font, Component.translatable("gui.appraisal.plugin.none"),
                    rightX, rightY, 0x808080, false);
            return;
        }

        for (PluginStat stat : PluginStat.values()) {
            if (!visible.contains(stat)) {
                continue;
            }
            String value = statValue(stats, stat);
            if (value == null) {
                continue;
            }
            boolean leftGroup = stat.group() == PluginStat.Group.LEFT;
            Component line = Component.translatable("gui.appraisal.plugin.stat.line",
                    Component.translatable(stat.labelKey()), value);
            int x = leftGroup ? leftAnchor - font.width(line) : rightX;
            int y = leftGroup ? leftY : rightY;
            graphics.drawString(font, line, x, y, statColor(stats, stat), false);
            if (leftGroup) {
                leftY += 9;
            } else {
                rightY += 9;
            }
        }
    }

    private static String statValue(PluginStats stats, PluginStat stat) {
        return switch (stat) {
            case APPRAISAL_LEVEL -> stats.levelBonus() == 0
                    ? null
                    : String.format(Locale.ROOT, "%+d", stats.levelBonus());
            case APPRAISAL_SPEED -> stats.appraisalSpeed() == 0.0D
                    ? null
                    : percent(stats.appraisalSpeed(), true);
            case PRODUCT_CHANCE -> stats.productChance() == 0.0D
                    ? null
                    : percent(stats.productChance(), true);
            case BLOCK_CONSUMPTION -> stats.blockConsumption() == 0.0D
                    ? null
                    : percent(stats.blockConsumeChance(), false);
            case ENERGY_CONSUMPTION -> stats.energyConsumption() == 0.0D
                    ? null
                    : percent(stats.energyConsumption(), true);
            case CAPACITY -> stats.capacity() == 0.0D
                    ? null
                    : percent(stats.capacityMultiplier() - 1.0D, true);
            case TRANSFER -> stats.transfer() == 0.0D
                    ? null
                    : percent(stats.transferMultiplier() - 1.0D, true);
            case COOLANT_CONSUMPTION -> stats.coolantConsumption() == 0.0D
                    ? null
                    : percent(stats.coolantFraction(), false);
            case GENERATOR_EFFICIENCY -> stats.generatorEfficiency() == 0.0D
                    ? null
                    : percent(stats.generatorEfficiency(), true);
            case SOLAR_GENERATION -> stats.solarGeneration() == 0.0D
                    ? null
                    : String.format(Locale.ROOT, "%+.0f FE/t", stats.solarGeneration());
            case VOID -> stats.voidEnabled()
                    ? Component.translatable("gui.appraisal.plugin.void_on").getString()
                    : null;
        };
    }

    private static String percent(double value, boolean signed) {
        return String.format(Locale.ROOT, signed ? "%+.0f%%" : "%.0f%%", value * 100.0D);
    }

    private static int statColor(PluginStats stats, PluginStat stat) {
        return switch (stat) {
            case APPRAISAL_LEVEL -> stats.levelBonus() > 0
                    ? PluginTooltip.GOOD_COLOR
                    : stats.levelBonus() < 0 ? PluginTooltip.BAD_COLOR : 0x808080;
            case APPRAISAL_SPEED -> stats.appraisalSpeed() > 0
                    ? PluginTooltip.GOOD_COLOR : PluginTooltip.BAD_COLOR;
            case PRODUCT_CHANCE -> stats.productChance() > 0
                    ? PluginTooltip.GOOD_COLOR : PluginTooltip.BAD_COLOR;
            case BLOCK_CONSUMPTION -> stats.blockConsumption() < 0
                    ? PluginTooltip.GOOD_COLOR : PluginTooltip.BAD_COLOR;
            case ENERGY_CONSUMPTION -> stats.energyConsumption() < 0
                    ? PluginTooltip.GOOD_COLOR : PluginTooltip.BAD_COLOR;
            case CAPACITY -> stats.capacity() > 0
                    ? PluginTooltip.GOOD_COLOR : PluginTooltip.BAD_COLOR;
            case TRANSFER -> stats.transfer() > 0
                    ? PluginTooltip.GOOD_COLOR : PluginTooltip.BAD_COLOR;
            case COOLANT_CONSUMPTION -> stats.coolantConsumption() > 0
                    ? PluginTooltip.BAD_COLOR : PluginTooltip.GOOD_COLOR;
            case GENERATOR_EFFICIENCY -> stats.generatorEfficiency() > 0
                    ? PluginTooltip.GOOD_COLOR : PluginTooltip.BAD_COLOR;
            case SOLAR_GENERATION -> stats.solarGeneration() > 0
                    ? PluginTooltip.GOOD_COLOR : PluginTooltip.BAD_COLOR;
            case VOID -> PluginTooltip.GOOD_COLOR;
        };
    }
}
