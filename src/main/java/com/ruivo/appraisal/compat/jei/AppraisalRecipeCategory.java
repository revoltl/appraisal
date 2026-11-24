package com.ruivo.appraisal.compat.jei;

import com.ruivo.appraisal.appraisal.AppraisalMode;
import com.ruivo.appraisal.client.LevelPalette;
import com.ruivo.appraisal.recipe.AppraisalOutput;
import com.ruivo.appraisal.recipe.AppraisalRecipe;
import com.ruivo.appraisal.recipe.BlockSelector;
import com.ruivo.appraisal.registry.ModItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 鉴定配方的 JEI 页面。
 *
 * <p>上方一个输入槽显示待鉴定方块，下方三行九列显示产物，鼠标悬停时同时给出直接鉴定
 * 的归一化概率与进阶鉴定的原始概率。产物超过二十七种时只显示前二十七种，并在页面底部
 * 提示剩余数量。</p>
 */
public final class AppraisalRecipeCategory implements IRecipeCategory<AppraisalRecipe> {

    public static final RecipeType<AppraisalRecipe> TYPE = new RecipeType<>(
            ResourceLocation.fromNamespaceAndPath("appraisal", "appraisal"), AppraisalRecipe.class);

    private static final int WIDTH = 176;
    private static final int HEIGHT = 140;
    private static final int INPUT_X = 7;
    private static final int INPUT_Y = 6;
    private static final int INFO_X = 30;
    private static final int INFO_Y = 8;
    private static final int OUTPUT_X = 7;
    private static final int OUTPUT_Y = 58;
    private static final int COLUMNS = 9;
    private static final int ROWS = 4;
    private static final int SLOT = 18;
    private static final int CAPACITY = COLUMNS * ROWS;

    private static final ResourceLocation LEVEL_FRAME =
            ResourceLocation.fromNamespaceAndPath("appraisal", "textures/gui/level_frame.png");

    private static final int LEVEL_FRAMES = 15;
    private static final int LEVEL_FRAME_HEIGHT = 240;

    private final IDrawable background;
    private final IDrawable icon;

    public AppraisalRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModItems.MAGNIFYING_GLASS.get()));
    }

    @Override
    public RecipeType<AppraisalRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.appraisal.category");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AppraisalRecipe recipe, IFocusGroup focuses) {
        List<ItemStack> inputs = inputStacks(recipe.input());
        if (!inputs.isEmpty()) {
            builder.addInputSlot(INPUT_X, INPUT_Y).addItemStacks(inputs);
        }

        double total = recipe.totalWeight();
        List<AppraisalOutput> outputs = recipe.outputs();
        int fixed = Math.min(outputs.size(), CAPACITY - 1);
        for (int index = 0; index < fixed; index++) {
            AppraisalOutput output = outputs.get(index);
            int x = OUTPUT_X + (index % COLUMNS) * SLOT;
            int y = OUTPUT_Y + (index / COLUMNS) * SLOT;
            builder.addOutputSlot(x, y)
                    .addItemStack(new ItemStack(output.result().item().value()))
                    .addRichTooltipCallback((slotView, tooltip) -> {
                        tooltip.add(Component.translatable("jei.appraisal.output_min_level",
                                        formatNumber(output.minLevel()))
                                .withStyle(ChatFormatting.GRAY));
                        tooltip.add(Component.translatable("jei.appraisal.weight",
                                        formatNumber(output.probability()))
                                .withStyle(ChatFormatting.GOLD));
                        if (recipe.allows(AppraisalMode.DIRECT) && total > 0.0D) {
                            double percent = output.probability() / total * 100.0D;
                            tooltip.add(Component.translatable("jei.appraisal.direct",
                                    String.format(Locale.ROOT, "%.1f", percent)).withStyle(ChatFormatting.GRAY));
                        }
                        if (recipe.allows(AppraisalMode.ADVANCED)) {
                            tooltip.add(Component.translatable("jei.appraisal.advanced",
                                    String.format(Locale.ROOT, "%.1f", output.probability())).withStyle(ChatFormatting.GRAY));
                        }
                    });
        }

        if (outputs.size() > CAPACITY - 1) {
            int slotIndex = CAPACITY - 1;
            int x = OUTPUT_X + (slotIndex % COLUMNS) * SLOT;
            int y = OUTPUT_Y + (slotIndex / COLUMNS) * SLOT;
            AppraisalOutput first = outputs.get(slotIndex);
            builder.addOutputSlot(x, y)
                    .addItemStack(new ItemStack(first.result().item().value()));
        }
    }

    @Override
    public void draw(AppraisalRecipe recipe, IRecipeSlotsView slotsView, GuiGraphics graphics,
                     double mouseX, double mouseY) {
        // 抬高 z 层级，保证等级框画在物品图标之上，同时不影响槽位的鼠标交互。
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 300.0F);

        drawLevelFrames(graphics, recipe);

        Font font = Minecraft.getInstance().font;
        int color = 0x404040;

        graphics.drawString(font, Component.translatable("jei.appraisal.min_level", recipe.minLevel()),
                INFO_X, INFO_Y, color, false);
        graphics.drawString(font, Component.translatable("jei.appraisal.time", recipe.duration()),
                INFO_X, INFO_Y + 11, color, false);
        graphics.drawString(font, Component.translatable("jei.appraisal.total_energy", recipe.energy()),
                INFO_X, INFO_Y + 22, color, false);

        double perTick = recipe.duration() > 0 ? (double) recipe.energy() / recipe.duration() : 0.0D;
        graphics.drawString(font, Component.translatable("jei.appraisal.tick_energy",
                        String.format(Locale.ROOT, "%.1f", perTick)),
                INFO_X, INFO_Y + 33, color, false);

        drawRotatingSlot(graphics, recipe);

        graphics.pose().popPose();

        drawRotatingTooltip(graphics, recipe, mouseX, mouseY);
    }

    private void drawRotatingTooltip(GuiGraphics graphics, AppraisalRecipe recipe, double mouseX, double mouseY) {
        int overflowStart = CAPACITY - 1;
        int total = recipe.outputs().size();
        if (total <= overflowStart) {
            return;
        }
        int x = OUTPUT_X + (overflowStart % COLUMNS) * SLOT;
        int y = OUTPUT_Y + (overflowStart / COLUMNS) * SLOT;
        if (mouseX < x || mouseX >= x + SLOT || mouseY < y || mouseY >= y + SLOT) {
            return;
        }

        long ticks = Minecraft.getInstance().level != null
                ? Minecraft.getInstance().level.getGameTime()
                : 0L;
        int cycle = (int) ((ticks / 20L) % (total - overflowStart));
        AppraisalOutput output = recipe.outputs().get(overflowStart + cycle);
        double totalWeight = recipe.totalWeight();

        List<Component> lines = new ArrayList<>();
        lines.add(output.result().item().value().getDescription().copy()
                .withStyle(ChatFormatting.WHITE));
        lines.add(Component.translatable("jei.appraisal.output_min_level",
                        formatNumber(output.minLevel()))
                .withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("jei.appraisal.weight",
                        formatNumber(output.probability()))
                .withStyle(ChatFormatting.GOLD));
        if (recipe.allows(AppraisalMode.DIRECT) && totalWeight > 0.0D) {
            double percent = output.probability() / totalWeight * 100.0D;
            lines.add(Component.translatable("jei.appraisal.direct",
                            String.format(Locale.ROOT, "%.1f", percent))
                    .withStyle(ChatFormatting.GRAY));
        }
        if (recipe.allows(AppraisalMode.ADVANCED)) {
            lines.add(Component.translatable("jei.appraisal.advanced",
                            String.format(Locale.ROOT, "%.1f", output.probability()))
                    .withStyle(ChatFormatting.GRAY));
        }
        lines.add(Component.translatable("jei.appraisal.rotating")
                .withStyle(ChatFormatting.DARK_GRAY));

        Font font = Minecraft.getInstance().font;
        graphics.renderComponentTooltip(font, lines, (int) mouseX, (int) mouseY);
    }

    private void drawRotatingSlot(GuiGraphics graphics, AppraisalRecipe recipe) {
        int overflowStart = CAPACITY - 1;
        int total = recipe.outputs().size();
        if (total <= overflowStart) {
            return;
        }
        long ticks = Minecraft.getInstance().level != null
                ? Minecraft.getInstance().level.getGameTime()
                : 0L;
        int frame = (int) (ticks % LEVEL_FRAMES);
        int cycle = (int) ((ticks / 20L) % (total - overflowStart));
        int index = overflowStart + cycle;
        AppraisalOutput output = recipe.outputs().get(index);
        int x = OUTPUT_X + (overflowStart % COLUMNS) * SLOT;
        int y = OUTPUT_Y + (overflowStart / COLUMNS) * SLOT;
        graphics.renderItem(new ItemStack(output.result().item().value()), x, y);
        drawFrame(graphics, x, y, frame, output.minLevel());
    }

    private void drawLevelFrames(GuiGraphics graphics, AppraisalRecipe recipe) {
        long ticks = Minecraft.getInstance().level != null
                ? Minecraft.getInstance().level.getGameTime()
                : 0L;
        int frame = (int) (ticks % LEVEL_FRAMES);

        drawFrame(graphics, INPUT_X, INPUT_Y, frame, recipe.minLevel());

        int shown = Math.min(recipe.outputs().size(), CAPACITY - 1);
        for (int index = 0; index < shown; index++) {
            int x = OUTPUT_X + (index % COLUMNS) * SLOT;
            int y = OUTPUT_Y + (index / COLUMNS) * SLOT;
            drawFrame(graphics, x, y, frame, recipe.outputs().get(index).minLevel());
        }
    }

    private void drawFrame(GuiGraphics graphics, int x, int y, int frame, int level) {
        int color = LevelPalette.color(level);
        float red = LevelPalette.red(color);
        float green = LevelPalette.green(color);
        float blue = LevelPalette.blue(color);

        graphics.setColor(red, green, blue, 1.0F);
        graphics.blit(LEVEL_FRAME, x, y, 0, frame * 16, 16, 16, 16, LEVEL_FRAME_HEIGHT);
        // 贴图本体是灰色，乘算一次会发暗。同色再叠一层把颜色拉浓。
        graphics.setColor(red, green, blue, 0.55F);
        graphics.blit(LEVEL_FRAME, x, y, 0, frame * 16, 16, 16, 16, LEVEL_FRAME_HEIGHT);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static String formatNumber(double value) {
        if (value == Math.rint(value)) {
            return Long.toString((long) value);
        }
        return String.format(Locale.ROOT, "%.1f", value);
    }

    private static List<ItemStack> inputStacks(BlockSelector selector) {        List<ItemStack> stacks = new ArrayList<>();
        if (selector.block() != null) {
            Item item = selector.block().asItem();
            if (item != Items.AIR) {
                stacks.add(new ItemStack(item));
            }
            return stacks;
        }
        if (selector.tag() != null) {
            for (Holder<Block> holder : BuiltInRegistries.BLOCK.getTagOrEmpty(selector.tag())) {
                Item item = holder.value().asItem();
                if (item != Items.AIR) {
                    stacks.add(new ItemStack(item));
                }
            }
        }
        return stacks;
    }
}
