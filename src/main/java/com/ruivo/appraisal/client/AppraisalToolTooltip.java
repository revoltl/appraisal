package com.ruivo.appraisal.client;

import com.ruivo.appraisal.compat.ftbultimine.FTBUltimineCompat;
import com.ruivo.appraisal.item.AppraisalTool;
import com.ruivo.appraisal.plugin.tool.PluginConfigurableTool;
import com.ruivo.appraisal.registry.ModEnchantments;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;
import java.util.Locale;

/**
 * 鉴定工具的通用说明。顺序固定为等级、用法、插件入口和电量，
 * 每一项都由 {@link AppraisalTool} 的能力方法决定是否出现。
 */
public final class AppraisalToolTooltip {

    private static final int USAGE_COLOR = 0xFFAA00;

    private AppraisalToolTooltip() {
    }

    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof AppraisalTool tool)) {
            return;
        }

        List<Component> lines = event.getToolTip();

        int enchantLevel = 0;
        Player player = event.getEntity();
        if (player != null) {
            Holder<Enchantment> enchantment = player.level().registryAccess()
                    .holderOrThrow(ModEnchantments.LEEUWENHOEK);
            enchantLevel = stack.getEnchantmentLevel(enchantment);
        }

        int level = tool.effectiveAppraisalLevel(stack) + enchantLevel;
        int bonus = tool.pluginLevelBonus(stack) + enchantLevel;
        MutableComponent levelLine = bonus == 0
                ? Component.translatable("tooltip.appraisal.tool.level", level)
                : Component.translatable("tooltip.appraisal.tool.level.bonus", level,
                        String.format(Locale.ROOT, "%+d", bonus));
        lines.add(levelLine.withStyle(style -> style.withColor(LevelPalette.color(level))));

        if (tool.supportsRightClickAppraisal()) {
            lines.add(Component.translatable("tooltip.appraisal.tool.usage")
                    .withStyle(style -> style.withColor(USAGE_COLOR)));
        }
        if (tool.supportsChainedAppraisal() && FTBUltimineCompat.isLoaded()) {
            lines.add(Component.translatable("tooltip.appraisal.tool.chained")
                    .withStyle(style -> style.withColor(USAGE_COLOR)));
        }
        if (tool instanceof PluginConfigurableTool) {
            lines.add(Component.translatable("tooltip.appraisal.tool.plugins")
                    .withStyle(style -> style.withColor(USAGE_COLOR)));
        }
        if (tool.hasEnergyDisplay(stack)) {
            int energy = tool.energyStored(stack);
            int capacity = tool.energyCapacity(stack);
            ChatFormatting color = energy < capacity / 10
                    ? ChatFormatting.RED
                    : ChatFormatting.GRAY;
            lines.add(Component.translatable("tooltip.appraisal.energy",
                    formatEnergy(energy), formatEnergy(capacity)).withStyle(color));
        }
    }

    private static String formatEnergy(int value) {
        return Integer.toString(value);
    }
}
