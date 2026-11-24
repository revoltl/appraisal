package com.ruivo.appraisal.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.ruivo.appraisal.registry.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * 中子引导配方。输入一件物品，按权重产出一件产物。
 * 权重总和不必等于一百，rollResult 会按总和归一化。
 */
public record NeutronGuideRecipe(
        Ingredient input,
        List<WeightedResult> results,
        int time
) implements Recipe<SingleRecipeInput> {

    public static final MapCodec<NeutronGuideRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("input").forGetter(NeutronGuideRecipe::input),
            WeightedResult.CODEC.listOf().fieldOf("results").forGetter(NeutronGuideRecipe::results),
            Codec.INT.optionalFieldOf("time", 200).forGetter(NeutronGuideRecipe::time)
    ).apply(instance, NeutronGuideRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, NeutronGuideRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    Ingredient.CONTENTS_STREAM_CODEC, NeutronGuideRecipe::input,
                    WeightedResult.STREAM_CODEC.apply(ByteBufCodecs.list()), NeutronGuideRecipe::results,
                    ByteBufCodecs.VAR_INT, NeutronGuideRecipe::time,
                    NeutronGuideRecipe::new);

    public NeutronGuideRecipe {
        if (results.isEmpty()) {
            throw new IllegalArgumentException("中子引导配方至少需要一个产物");
        }
        if (time < 0) {
            throw new IllegalArgumentException("中子引导配方的加工时间不能为负");
        }
        results = List.copyOf(results);
    }

    public ItemStack rollResult(RandomSource random) {
        int total = 0;
        for (WeightedResult result : results) {
            total += result.weight();
        }
        int roll = random.nextInt(total);
        for (WeightedResult result : results) {
            roll -= result.weight();
            if (roll < 0) {
                return result.stack();
            }
        }
        return results.getFirst().stack();
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        return this.input.test(input.getItem(0));
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        return results.getFirst().stack();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return results.getFirst().stack();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.NEUTRON_GUIDE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.NEUTRON_GUIDE_TYPE.get();
    }
}
