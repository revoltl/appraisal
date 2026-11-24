package com.ruivo.appraisal.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.ruivo.appraisal.registry.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

/**
 * 衰变发电配方。输入物只决定衰变时长，产能由发电机自身的固定速率和插件决定。
 */
public record DecayGenerationRecipe(
        Ingredient input,
        ItemStack output,
        int time
) implements Recipe<SingleRecipeInput> {

    public static final MapCodec<DecayGenerationRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("input").forGetter(DecayGenerationRecipe::input),
            ItemStack.CODEC.fieldOf("output").forGetter(DecayGenerationRecipe::output),
            Codec.INT.optionalFieldOf("time", 200).forGetter(DecayGenerationRecipe::time)
    ).apply(instance, DecayGenerationRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DecayGenerationRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    Ingredient.CONTENTS_STREAM_CODEC, DecayGenerationRecipe::input,
                    ItemStack.STREAM_CODEC, DecayGenerationRecipe::output,
                    ByteBufCodecs.VAR_INT, DecayGenerationRecipe::time,
                    DecayGenerationRecipe::new);

    public DecayGenerationRecipe {
        if (output.isEmpty()) {
            throw new IllegalArgumentException("衰变配方至少需要一个产物");
        }
        if (time < 0) {
            throw new IllegalArgumentException("衰变配方的时长不能为负");
        }
        output = output.copy();
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return this.input.test(input.getItem(0));
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return output.copy();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.DECAY_GENERATION_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.DECAY_GENERATION_TYPE.get();
    }
}
