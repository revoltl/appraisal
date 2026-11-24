package com.ruivo.appraisal.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 带权重的配方产物。权重按整数计，总权重不必等于一百，内部会按总和归一化。
 */
public record WeightedResult(Item item, int count, int weight) {

    public static final Codec<WeightedResult> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(WeightedResult::item),
            Codec.INT.optionalFieldOf("count", 1).forGetter(WeightedResult::count),
            Codec.INT.optionalFieldOf("weight", 1).forGetter(WeightedResult::weight)
    ).apply(instance, WeightedResult::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, WeightedResult> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.registry(Registries.ITEM), WeightedResult::item,
                    ByteBufCodecs.VAR_INT, WeightedResult::count,
                    ByteBufCodecs.VAR_INT, WeightedResult::weight,
                    WeightedResult::new);

    public WeightedResult {
        if (count < 1) {
            throw new IllegalArgumentException("产物数量至少为一");
        }
        if (weight < 1) {
            throw new IllegalArgumentException("权重至少为一");
        }
    }

    public ItemStack stack() {
        return new ItemStack(item, count);
    }
}
