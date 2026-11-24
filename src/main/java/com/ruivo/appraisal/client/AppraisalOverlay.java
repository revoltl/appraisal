package com.ruivo.appraisal.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.ruivo.appraisal.appraisal.AppraisalEngine;
import com.ruivo.appraisal.appraisal.AppraisalLevels;
import com.ruivo.appraisal.item.MagnifyingGlassItem;
import com.ruivo.appraisal.recipe.AppraisalOutput;
import com.ruivo.appraisal.recipe.AppraisalRecipe;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 手持鉴定工具对准可鉴定方块时，用折线引出面板显示鉴定等级与产物列表。
 *
 * <p>世界渲染阶段只做投影，文字与图标由 GUI 层绘制。产物超过面板容量时，鼠标滚轮在
 * 产物区滚动，等级行固定不动。</p>
 */
public final class AppraisalOverlay {

    private static final int VISIBLE_ROWS = 7;
    private static final int ROW_HEIGHT = 18;
    private static final int PANEL_PADDING = 5;
    private static final int ICON_SIZE = 16;
    private static final int LEADER = 24;
    private static final int PANEL_COLOR = 0xC0101010;

    private static final ResourceLocation LEVEL_FRAME =
            ResourceLocation.fromNamespaceAndPath("appraisal", "textures/gui/level_frame.png");

    private record OutputLine(ItemStack stack, Component text, int level) {
    }

    private static final List<OutputLine> OUTPUTS = new ArrayList<>();
    private static Component levelLine = null;
    private static boolean visible = false;
    private static float anchorX = 0.0F;
    private static float anchorY = 0.0F;
    private static int currentLevel = 0;
    private static int scrollOffset = 0;
    private static BlockPos lastPos = null;

    private AppraisalOverlay() {
    }

    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        visible = false;
        levelLine = null;
        OUTPUTS.clear();

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        Level level = minecraft.level;
        if (player == null || level == null || minecraft.screen != null) {
            return;
        }
        if (!(minecraft.hitResult instanceof BlockHitResult hit)) {
            return;
        }

        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        ItemStack tool = mainHand.getItem() instanceof MagnifyingGlassItem ? mainHand
                : offHand.getItem() instanceof MagnifyingGlassItem ? offHand : ItemStack.EMPTY;
        if (tool.isEmpty()) {
            return;
        }

        BlockPos pos = hit.getBlockPos();
        BlockState state = level.getBlockState(pos);
        AppraisalRecipe recipe = AppraisalEngine.findRecipe(level, state);
        if (recipe == null) {
            return;
        }
        if (!pos.equals(lastPos)) {
            lastPos = pos;
            scrollOffset = 0;
        }

        int playerLevel = AppraisalLevels.of(player, tool);
        List<AppraisalOutput> available = new ArrayList<>();
        double total = 0.0D;
        for (AppraisalOutput output : recipe.outputs()) {
            if (output.minLevel() <= playerLevel) {
                available.add(output);
                total += output.probability();
            }
        }
        if (available.isEmpty()) {
            return;
        }

        int levelColor = LevelPalette.color(playerLevel);
        levelLine = Component.translatable("hud.appraisal.level.prefix")
                .withStyle(ChatFormatting.WHITE)
                .append(Component.literal(String.valueOf(playerLevel))
                        .withStyle(style -> style.withColor(levelColor)));
        currentLevel = playerLevel;
        for (AppraisalOutput output : available) {
            double percent = total > 0.0D ? output.probability() / total * 100.0D : 0.0D;
            ItemStack stack = new ItemStack(output.result().item().value());
            Component text = stack.getHoverName().copy()
                    .append(Component.literal(" "))
                    .append(Component.literal(String.format(Locale.ROOT, "%.1f%%", percent))
                            .withStyle(ChatFormatting.GRAY));
            OUTPUTS.add(new OutputLine(stack, text, output.minLevel()));
        }

        Vec3 anchorWorld = Vec3.atCenterOf(pos);
        Vec3 camera = event.getCamera().getPosition();
        Vector4f point = new Vector4f(
                (float) (anchorWorld.x - camera.x),
                (float) (anchorWorld.y - camera.y),
                (float) (anchorWorld.z - camera.z),
                1.0F);
        point.mul(event.getModelViewMatrix());
        point.mul(event.getProjectionMatrix());
        if (point.w() <= 0.0F) {
            return;
        }

        float ndcX = point.x() / point.w();
        float ndcY = point.y() / point.w();
        anchorX = (ndcX * 0.5F + 0.5F) * minecraft.getWindow().getGuiScaledWidth();
        anchorY = (-ndcY * 0.5F + 0.5F) * minecraft.getWindow().getGuiScaledHeight();
        visible = true;
    }

    public static void onScroll(InputEvent.MouseScrollingEvent event) {
        if (!visible || OUTPUTS.size() <= VISIBLE_ROWS) {
            return;
        }
        scrollOffset -= (int) Math.signum(event.getScrollDeltaY());
        scrollOffset = Mth.clamp(scrollOffset, 0, OUTPUTS.size() - VISIBLE_ROWS);
        event.setCanceled(true);
    }

    private static void drawLine(GuiGraphics graphics, int x1, int y1, int x2, int y2, int color, int width) {
        int dx = x2 - x1;
        int dy = y2 - y1;
        int steps = Math.max(Math.abs(dx), Math.abs(dy));
        int half = Math.max(1, width / 2);
        if (steps <= 0) {
            graphics.fill(x1 - half, y1 - half, x1 + half, y1 + half, color);
            return;
        }
        for (int step = 0; step <= steps; step++) {
            int x = x1 + Math.round((float) dx * step / steps);
            int y = y1 + Math.round((float) dy * step / steps);
            graphics.fill(x - half, y - half, x + half, y + half, color);
        }
    }

    private static void drawAnchor(GuiGraphics graphics, int x, int y, int color, int unit) {
        int radius = 4 * unit;
        int dot = 2 * unit;
        int stroke = 2 * unit;
        graphics.fill(x - dot, y - dot, x + dot, y + dot, 0xFFFFFFFF);
        for (int dy = -radius; dy <= radius; dy += unit) {
            int half = radius - Math.abs(dy);
            graphics.fill(x - half, y + dy, x - half + stroke, y + dy + unit, color);
            graphics.fill(x + half - stroke, y + dy, x + half, y + dy + unit, color);
        }
    }

    private static void drawIconFrame(GuiGraphics graphics, int x, int y, int level) {
        long ticks = Minecraft.getInstance().level != null
                ? Minecraft.getInstance().level.getGameTime()
                : 0L;
        int frame = (int) (ticks % 15L);
        int color = LevelPalette.color(level);
        float red = LevelPalette.red(color);
        float green = LevelPalette.green(color);
        float blue = LevelPalette.blue(color);

        graphics.setColor(red, green, blue, 1.0F);
        graphics.blit(LEVEL_FRAME, x, y, 0, frame * 16, 16, 16, 16, 240);
        graphics.setColor(red, green, blue, 0.55F);
        graphics.blit(LEVEL_FRAME, x, y, 0, frame * 16, 16, 16, 16, 240);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        if (!visible || levelLine == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;

        int contentWidth = font.width(levelLine);
        for (OutputLine line : OUTPUTS) {
            contentWidth = Math.max(contentWidth, ICON_SIZE + 4 + font.width(line.text()));
        }
        int panelWidth = contentWidth + PANEL_PADDING * 2;
        int rows = Math.min(OUTPUTS.size(), VISIBLE_ROWS);
        int panelHeight = PANEL_PADDING * 2 + 11 + rows * ROW_HEIGHT;

        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();

        int panelX = (int) anchorX + LEADER;
        int panelY = (int) anchorY - LEADER - panelHeight / 2;
        boolean onRight = true;

        if (panelX + panelWidth > screenWidth - 4) {
            panelX = (int) anchorX - LEADER - panelWidth;
            onRight = false;
        }
        panelX = Mth.clamp(panelX, 4, Math.max(4, screenWidth - panelWidth - 4));
        panelY = Mth.clamp(panelY, 4, Math.max(4, screenHeight - panelHeight - 4));

        int lineColor = 0xFF000000 | LevelPalette.color(currentLevel);
        int panelEdgeX = onRight ? panelX : panelX + panelWidth;
        int panelEdgeY = Mth.clamp((int) anchorY, panelY + 12, panelY + panelHeight - 12);
        int bendX = panelEdgeX + (onRight ? -14 : 14);
        int bendY = panelEdgeY;

        // 放大十六倍绘制再缩回，斜线与菱形边缘落在亚像素上，不会出现锯齿台阶。
        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.scale(0.0625F, 0.0625F, 1.0F);
        drawLine(graphics, (int) anchorX * 16, (int) anchorY * 16, bendX * 16, bendY * 16, lineColor, 16);
        drawLine(graphics, bendX * 16, bendY * 16, panelEdgeX * 16, panelEdgeY * 16, lineColor, 16);
        drawAnchor(graphics, (int) anchorX * 16, (int) anchorY * 16, lineColor, 16);
        pose.popPose();

        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, PANEL_COLOR);
        graphics.fill(panelX, panelY, panelX + panelWidth, panelY + 1, lineColor);
        graphics.fill(panelX, panelY + panelHeight - 1, panelX + panelWidth, panelY + panelHeight, lineColor);
        graphics.fill(panelX, panelY, panelX + 1, panelY + panelHeight, lineColor);
        graphics.fill(panelX + panelWidth - 1, panelY, panelX + panelWidth, panelY + panelHeight, lineColor);

        graphics.drawString(font, levelLine, panelX + PANEL_PADDING, panelY + PANEL_PADDING,
                0xFFFFFF, false);

        int y = panelY + PANEL_PADDING + 11;
        int end = Math.min(scrollOffset + VISIBLE_ROWS, OUTPUTS.size());
        for (int index = scrollOffset; index < end; index++) {
            OutputLine line = OUTPUTS.get(index);
            graphics.renderItem(line.stack(), panelX + PANEL_PADDING, y);
            drawIconFrame(graphics, panelX + PANEL_PADDING, y, line.level());
            graphics.drawString(font, line.text(), panelX + PANEL_PADDING + ICON_SIZE + 4, y + 4,
                    0xFFFFFF, false);
            y += ROW_HEIGHT;
        }

        if (OUTPUTS.size() > VISIBLE_ROWS) {
            int trackX = panelX + panelWidth - 3;
            int trackTop = panelY + PANEL_PADDING + 11;
            int trackHeight = rows * ROW_HEIGHT;
            graphics.fill(trackX, trackTop, trackX + 1, trackTop + trackHeight, 0x40FFFFFF);
            int thumbHeight = Math.max(6, trackHeight * VISIBLE_ROWS / OUTPUTS.size());
            int thumbOffset = (trackHeight - thumbHeight) * scrollOffset
                    / Math.max(1, OUTPUTS.size() - VISIBLE_ROWS);
            graphics.fill(trackX, trackTop + thumbOffset, trackX + 1,
                    trackTop + thumbOffset + thumbHeight, lineColor);
        }
    }
}
