package com.ruivo.appraisal.recipe;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class NeutronGuideRecipeSerializer implements RecipeSerializer<NeutronGuideRecipe> {

    @Override
    public MapCodec<NeutronGuideRecipe> codec() {
        return NeutronGuideRecipe.CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, NeutronGuideRecipe> streamCodec() {
        return NeutronGuideRecipe.STREAM_CODEC;
    }
}
