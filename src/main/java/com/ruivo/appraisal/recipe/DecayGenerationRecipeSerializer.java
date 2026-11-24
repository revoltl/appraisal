package com.ruivo.appraisal.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class DecayGenerationRecipeSerializer implements RecipeSerializer<DecayGenerationRecipe> {

    @Override
    public MapCodec<DecayGenerationRecipe> codec() {
        return DecayGenerationRecipe.CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, DecayGenerationRecipe> streamCodec() {
        return DecayGenerationRecipe.STREAM_CODEC;
    }
}
