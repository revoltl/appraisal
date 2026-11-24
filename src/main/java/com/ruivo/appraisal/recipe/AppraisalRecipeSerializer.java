package com.ruivo.appraisal.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class AppraisalRecipeSerializer implements RecipeSerializer<AppraisalRecipe> {

    @Override
    public MapCodec<AppraisalRecipe> codec() {
        return AppraisalRecipe.CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, AppraisalRecipe> streamCodec() {
        return AppraisalRecipe.STREAM_CODEC;
    }
}
