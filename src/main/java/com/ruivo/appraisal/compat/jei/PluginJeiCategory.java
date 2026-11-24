package com.ruivo.appraisal.compat.jei;

import com.ruivo.appraisal.registry.ModItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * 插件页。只有一面，按九列一行铺开所有带插件标签的物品。
 * 头图是空白插件，催化剂是鉴定机与插件本身。
 */
public final class PluginJeiCategory implements IRecipeCategory<PluginJeiRecipe> {

    public static final RecipeType<PluginJeiRecipe> TYPE = new RecipeType<>(
            ResourceLocation.fromNamespaceAndPath("appraisal", "plugin"), PluginJeiRecipe.class);

    private static final int WIDTH = 176;
    private static final int HEIGHT = 40;
    private static final int SLOT = 18;
    private static final int COLUMNS = 9;
    private static final int START_X = 8;
    private static final int START_Y = 8;

    private final IDrawable background;
    private final IDrawable icon;

    public PluginJeiCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createBlankDrawable(WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableItemStack(new ItemStack(ModItems.BLANK_PLUGIN.get()));
    }

    @Override
    public RecipeType<PluginJeiRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.appraisal.plugin.category");
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
    public void setRecipe(IRecipeLayoutBuilder builder, PluginJeiRecipe recipe, IFocusGroup focuses) {
        for (int index = 0; index < recipe.plugins().size(); index++) {
            int x = START_X + (index % COLUMNS) * SLOT;
            int y = START_Y + (index / COLUMNS) * SLOT;
            // 放在输入位，按 U 在插件上才能命中这一页。催化剂只有鉴定机。
            builder.addInputSlot(x, y).addItemStack(recipe.plugins().get(index));
        }
    }
}
