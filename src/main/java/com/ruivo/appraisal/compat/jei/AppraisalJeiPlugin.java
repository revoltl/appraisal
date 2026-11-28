package com.ruivo.appraisal.compat.jei;

import com.ruivo.appraisal.appraisal.AppraisalEngine;
import com.ruivo.appraisal.recipe.AppraisalRecipe;
import com.ruivo.appraisal.recipe.DecayGenerationRecipe;
import com.ruivo.appraisal.recipe.NeutronGuideRecipe;
import com.ruivo.appraisal.registry.ModItems;
import com.ruivo.appraisal.registry.ModRecipes;
import com.ruivo.appraisal.registry.ModTags;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@JeiPlugin
public class AppraisalJeiPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath("appraisal", "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(
                new AppraisalRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new PluginJeiCategory(registration.getJeiHelpers().getGuiHelper()),
                new NeutronGuideRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
                new DecayGenerationRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(PluginJeiCategory.TYPE, List.of(pluginRecipe()));

        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        List<AppraisalRecipe> recipes = level.getRecipeManager()
                .getAllRecipesFor(ModRecipes.APPRAISAL_TYPE.get())
                .stream()
                .sorted(Comparator
                        .comparingInt((RecipeHolder<AppraisalRecipe> holder) -> holder.value().minLevel())
                        .thenComparing(holder -> holder.id().toString()))
                .map(holder -> AppraisalEngine.augment(holder.value()))
                .toList();
        registration.addRecipes(AppraisalRecipeCategory.TYPE, recipes);

        List<NeutronGuideRecipe> neutronRecipes = level.getRecipeManager()
                .getAllRecipesFor(ModRecipes.NEUTRON_GUIDE_TYPE.get())
                .stream()
                .map(RecipeHolder::value)
                .toList();
        registration.addRecipes(NeutronGuideRecipeCategory.TYPE, neutronRecipes);

        List<DecayGenerationRecipe> decayRecipes = level.getRecipeManager()
                .getAllRecipesFor(ModRecipes.DECAY_GENERATION_TYPE.get())
                .stream()
                .map(RecipeHolder::value)
                .toList();
        registration.addRecipes(DecayGenerationRecipeCategory.TYPE, decayRecipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(ModTags.Items.APPRAISAL_TOOLS)) {
            registration.addRecipeCatalyst(holder.value(), AppraisalRecipeCategory.TYPE);
        }
        registration.addRecipeCatalyst(ModItems.APPRAISAL_MACHINE.get(), AppraisalRecipeCategory.TYPE);

        registration.addRecipeCatalyst(ModItems.APPRAISAL_MACHINE.get(), PluginJeiCategory.TYPE);
        registration.addRecipeCatalyst(ModItems.MODEL_TRAINER.get(), PluginJeiCategory.TYPE);
        registration.addRecipeCatalyst(ModItems.DECAY_GENERATOR.get(), PluginJeiCategory.TYPE);
        registration.addRecipeCatalyst(ModItems.NEUTRON_GUIDE_TOWER.get(), NeutronGuideRecipeCategory.TYPE);
        registration.addRecipeCatalyst(ModItems.DECAY_GENERATOR.get(), DecayGenerationRecipeCategory.TYPE);
    }

    private static PluginJeiRecipe pluginRecipe() {
        List<ItemStack> plugins = new ArrayList<>();
        for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(ModTags.Items.PLUGINS)) {
            plugins.add(new ItemStack(holder.value()));
        }
        return new PluginJeiRecipe(plugins);
    }
}
