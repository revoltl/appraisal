package com.ruivo.appraisal.compat.jei;

import com.ruivo.appraisal.recipe.DecayGenerationRecipe;
import com.ruivo.appraisal.registry.ModItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * 衰变发电配方的 JEI 页面。左侧输入物，右侧衰变产物，下方显示时长和固定产能。
 */
public final class DecayGenerationRecipeCategory implements IRecipeCategory<DecayGenerationRecipe> {

    public static final RecipeType<DecayGenerationRecipe> TYPE = new RecipeType<>(
            ResourceLocation.fromNamespaceAndPath("appraisal", "decay_generation"),
            DecayGenerationRecipe.class);

    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;
    private static final int INPUT_X = 5;
    private static final int INPUT_Y = 5;
    private static final int OUTPUT_X = 55;
    private static final int OUTPUT_Y = 5;

    private static final ResourceLocation ARROW_BACK =
            ResourceLocation.fromNamespaceAndPath("appraisal", "textures/gui/arrow_back.png");
    private static final ResourceLocation ARROW_FULL =
            ResourceLocation.fromNamespaceAndPath("appraisal", "textures/gui/arrow_full.png");
    private static final int ARROW_X = 28;
    private static final int ARROW_Y = 6;
    private static final int ARROW_WIDTH = 24;
    private static final int ARROW_HEIGHT = 17;

    private final IDrawable background;
    private final IDrawable icon;

    public DecayGenerationRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableItemStack(
                new ItemStack(ModItems.URANIUM_TITANIUM_THALLIUM.get()));
    }

    @Override
    public RecipeType<DecayGenerationRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.appraisal.decay_generation");
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
    public void setRecipe(IRecipeLayoutBuilder builder, DecayGenerationRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(INPUT_X, INPUT_Y).addItemStacks(List.of(recipe.input().getItems()));
        builder.addOutputSlot(OUTPUT_X, OUTPUT_Y).addItemStack(recipe.output());
    }

    @Override
    public void draw(DecayGenerationRecipe recipe, IRecipeSlotsView slotsView, GuiGraphics graphics,
                     double mouseX, double mouseY) {
        drawProgress(graphics);
        Font font = Minecraft.getInstance().font;
        graphics.drawString(font,
                Component.translatable("jei.appraisal.decay_generation.time", recipe.time()),
                5, 28, 0x404040, false);
    }

    private static void drawProgress(GuiGraphics graphics) {
        long ticks = Minecraft.getInstance().level != null
                ? Minecraft.getInstance().level.getGameTime()
                : 0L;
        float ratio = (ticks % 40L) / 40.0F;
        graphics.blit(ARROW_BACK, ARROW_X, ARROW_Y, 0, 0, ARROW_WIDTH, ARROW_HEIGHT,
                ARROW_WIDTH, ARROW_HEIGHT);
        int fillWidth = Math.round(ARROW_WIDTH * ratio);
        if (fillWidth > 0) {
            graphics.blit(ARROW_FULL, ARROW_X, ARROW_Y, 0, 0, fillWidth, ARROW_HEIGHT,
                    ARROW_WIDTH, ARROW_HEIGHT);
        }
    }
}
