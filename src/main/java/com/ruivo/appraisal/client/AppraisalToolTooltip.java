package com.ruivo.appraisal.client;

import com.ruivo.appraisal.compat.ftbultimine.FTBUltimineCompat;
import com.ruivo.appraisal.item.AppraisalTool;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;

/**
 * 鉴定工具的扩展信息。按住 Shift 展开，未按住时只提示一次。
 *
 * <p>两行说明都由工具接口驱动：等级来自 {@link AppraisalTool#appraisalLevel}，
 * 连锁说明只在检测到连锁模组时追加。</p>
 */
public final class AppraisalToolTooltip {

    private static final int LEVEL_COLOR = 0x55FFFF;
    private static final int USAGE_COLOR = 0xFFAA00;

    private AppraisalToolTooltip() {
    }

    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof AppraisalTool tool)) {
            return;
        }

        List<Component> lines = event.getToolTip();
        if (!Screen.hasShiftDown()) {
            lines.add(Component.translatable("tooltip.appraisal.shift").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }

        lines.add(Component.translatable("tooltip.appraisal.tool.level", tool.appraisalLevel(stack))
                .withStyle(style -> style.withColor(LEVEL_COLOR)));

        if (tool.supportsRightClickAppraisal()) {
            lines.add(Component.translatable("tooltip.appraisal.tool.usage")
                    .withStyle(style -> style.withColor(USAGE_COLOR)));
        }
        if (tool.supportsChainedAppraisal() && FTBUltimineCompat.isLoaded()) {
            lines.add(Component.translatable("tooltip.appraisal.tool.chained")
                    .withStyle(style -> style.withColor(USAGE_COLOR)));
        }
    }
}
