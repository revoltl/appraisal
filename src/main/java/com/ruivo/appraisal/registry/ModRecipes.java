package com.ruivo.appraisal.registry;

import com.ruivo.appraisal.AppraisalMod;
import com.ruivo.appraisal.recipe.AppraisalRecipe;
import com.ruivo.appraisal.recipe.AppraisalRecipeSerializer;
import com.ruivo.appraisal.recipe.DecayGenerationRecipe;
import com.ruivo.appraisal.recipe.DecayGenerationRecipeSerializer;
import com.ruivo.appraisal.recipe.NeutronGuideRecipe;
import com.ruivo.appraisal.recipe.NeutronGuideRecipeSerializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRecipes {

    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, AppraisalMod.MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, AppraisalMod.MOD_ID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<AppraisalRecipe>> APPRAISAL_TYPE =
            TYPES.register("appraisal", () -> RecipeType.simple(
                    ResourceLocation.fromNamespaceAndPath(AppraisalMod.MOD_ID, "appraisal")));

    public static final DeferredHolder<RecipeSerializer<?>, AppraisalRecipeSerializer> APPRAISAL_SERIALIZER =
            SERIALIZERS.register("appraisal", AppraisalRecipeSerializer::new);

    public static final DeferredHolder<RecipeType<?>, RecipeType<NeutronGuideRecipe>> NEUTRON_GUIDE_TYPE =
            TYPES.register("neutron_guide", () -> RecipeType.simple(
                    ResourceLocation.fromNamespaceAndPath(AppraisalMod.MOD_ID, "neutron_guide")));

    public static final DeferredHolder<RecipeSerializer<?>, NeutronGuideRecipeSerializer> NEUTRON_GUIDE_SERIALIZER =
            SERIALIZERS.register("neutron_guide", NeutronGuideRecipeSerializer::new);

    public static final DeferredHolder<RecipeType<?>, RecipeType<DecayGenerationRecipe>> DECAY_GENERATION_TYPE =
            TYPES.register("decay_generation", () -> RecipeType.simple(
                    ResourceLocation.fromNamespaceAndPath(AppraisalMod.MOD_ID, "decay_generation")));

    public static final DeferredHolder<RecipeSerializer<?>, DecayGenerationRecipeSerializer> DECAY_GENERATION_SERIALIZER =
            SERIALIZERS.register("decay_generation", DecayGenerationRecipeSerializer::new);

    private ModRecipes() {
    }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
        SERIALIZERS.register(modBus);
    }
}
