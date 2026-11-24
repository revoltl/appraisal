package com.ruivo.appraisal.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;

/**
 * 配方中的一个产物定义。
 *
 * <p>probability 的单位是百分数，同时决定产出份数。直接鉴定把它当作权重，选中的产物产出
 * 一份；进阶鉴定把它当作独立产出概率，整数部分必定产出，小数部分按概率多产出一份。
 * 例如 350 表示必定三份，另有五成概率产出第四份。</p>
 *
 * <p>minLevel 是领取这份产物所需的最低鉴定等级。等级不足的产物不参与权重分配，也不参与
 * 进阶判定。</p>
 */
public record AppraisalOutput(Entry result, double probability, int minLevel) {

    public record Entry(OutputKind kind, Holder<Item> item) {

        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                OutputKind.CODEC.fieldOf("type").forGetter(Entry::kind),
                BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("id").forGetter(Entry::item)
        ).apply(instance, Entry::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(
                OutputKind.STREAM_CODEC, Entry::kind,
                ByteBufCodecs.holderRegistry(Registries.ITEM), Entry::item,
                Entry::new
        );
    }

    public static final Codec<AppraisalOutput> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Entry.CODEC.fieldOf("result").forGetter(AppraisalOutput::result),
            Codec.DOUBLE.fieldOf("probability").forGetter(AppraisalOutput::probability),
            Codec.INT.optionalFieldOf("min_level", -1).forGetter(AppraisalOutput::minLevel)
    ).apply(instance, AppraisalOutput::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AppraisalOutput> STREAM_CODEC = StreamCodec.composite(
            Entry.STREAM_CODEC, AppraisalOutput::result,
            ByteBufCodecs.DOUBLE, AppraisalOutput::probability,
            ByteBufCodecs.VAR_INT, AppraisalOutput::minLevel,
            AppraisalOutput::new
    );

    public AppraisalOutput {
        if (probability < 0.0D) {
            throw new IllegalArgumentException("鉴定产物的概率不能为负");
        }
        if (minLevel < -1) {
            throw new IllegalArgumentException("鉴定产物的最低等级不能小于负一");
        }
    }
}
