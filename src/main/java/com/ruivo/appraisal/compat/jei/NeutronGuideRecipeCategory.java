package com.ruivo.appraisal.compat.jei;

import com.ruivo.appraisal.recipe.NeutronGuideRecipe;
import com.ruivo.appraisal.recipe.WeightedResult;
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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Locale;

/**
 * 中子引导配方的 JEI 页面。左侧输入，右侧按权重列出产物，产物槽的 tooltip 显示百分比。
 */
public final class NeutronGuideRecipeCategory implements IRecipeCategory<NeutronGuideRecipe> {

    public static final RecipeType<NeutronGuideRecipe> TYPE = new RecipeType<>(
            ResourceLocation.fromNamespaceAndPath("appraisal", "neutron_guide"),
            NeutronGuideRecipe.class);

    private static final int WIDTH = 140;
    private static final int HEIGHT = 42;
    private static final int INPUT_X = 5;
    private static final int INPUT_Y = 5;
    private static final int OUTPUT_X = 55;
    private static final int OUTPUT_Y = 5;
    private static final int SLOT = 22;

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

    public NeutronGuideRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableItemStack(
                new ItemStack(ModItems.PLUTONIUM_TITANIUM_THALLIUM.get()));
    }

    @Override
    public RecipeType<NeutronGuideRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.appraisal.neutron_guide");
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
    public void setRecipe(IRecipeLayoutBuilder builder, NeutronGuideRecipe recipe, IFocusGroup focuses) {
        builder.addInputSlot(INPUT_X, INPUT_Y).addItemStacks(List.of(recipe.input().getItems()));

        int total = recipe.results().stream().mapToInt(WeightedResult::weight).sum();
        for (int index = 0; index < recipe.results().size(); index++) {
            WeightedResult result = recipe.results().get(index);
            int x = OUTPUT_X + (index % 2) * SLOT;
            int y = OUTPUT_Y + (index / 2) * SLOT;
            builder.addOutputSlot(x, y)
                    .addItemStack(result.stack())
                    .addRichTooltipCallback((slotView, tooltip) -> {
                        double chance = total > 0 ? (double) result.weight() / total * 100.0D : 0.0D;
                        tooltip.add(Component.translatable("jei.appraisal.neutron_guide.chance",
                                        String.format(Locale.ROOT, "%.0f", chance))
                                .withStyle(ChatFormatting.GOLD));
                    });
        }
    }

    @Override
    public void draw(NeutronGuideRecipe recipe, IRecipeSlotsView slotsView, GuiGraphics graphics,
                     double mouseX, double mouseY) {
        drawProgress(graphics);
        Font font = Minecraft.getInstance().font;
        graphics.drawString(font,
                Component.translatable("jei.appraisal.neutron_guide.time", recipe.time()),
                5, 30, 0x404040, false);
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
