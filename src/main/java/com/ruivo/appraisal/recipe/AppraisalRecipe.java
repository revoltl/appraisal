package com.ruivo.appraisal.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.ruivo.appraisal.appraisal.AppraisalMode;
import com.ruivo.appraisal.registry.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * 鉴定配方。
 *
 * <p>duration 的单位是游戏刻，二十刻为一秒。加工进度按游戏刻推进，所以 {@code /tick rate}
 * 会改变它对应的现实时间，服务器卡顿时实际耗时也会变长。energy 的单位是 FE，即 NeoForge
 * 的能量单位，原版没有能量概念。</p>
 *
 * <p>这两个字段供鉴定台与鉴定机读取。放大镜执行的是瞬时鉴定，不读取 duration 与 energy。</p>
 *
 * <p>integrations 列出这份配方允许参与的内置联动。整合包覆盖配方时不写这个字段，
 * 联动整条跳过，配方以数据包里的为准。</p>
 */
public record AppraisalRecipe(
        BlockSelector input,
        List<AppraisalOutput> outputs,
        int duration,
        int energy,
        List<AppraisalMode> modes,
        int minLevel,
        List<String> integrations
) implements Recipe<AppraisalRecipeInput> {

    public static final MapCodec<AppraisalRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BlockSelector.CODEC.fieldOf("input").forGetter(AppraisalRecipe::input),
            AppraisalOutput.CODEC.listOf().fieldOf("outputs").forGetter(AppraisalRecipe::outputs),
            Codec.INT.optionalFieldOf("duration", 20).forGetter(AppraisalRecipe::duration),
            Codec.INT.optionalFieldOf("energy", 0).forGetter(AppraisalRecipe::energy),
            AppraisalMode.CODEC.listOf()
                    .optionalFieldOf("modes", List.of(AppraisalMode.DIRECT, AppraisalMode.ADVANCED))
                    .forGetter(AppraisalRecipe::modes),
            Codec.INT.optionalFieldOf("min_level", 0).forGetter(AppraisalRecipe::minLevel),
            Codec.STRING.listOf().optionalFieldOf("integrations", List.of()).forGetter(AppraisalRecipe::integrations)
    ).apply(instance, AppraisalRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AppraisalRecipe> STREAM_CODEC =
            StreamCodec.of(AppraisalRecipe::encode, AppraisalRecipe::decode);

    private static void encode(RegistryFriendlyByteBuf buf, AppraisalRecipe recipe) {
        BlockSelector.STREAM_CODEC.encode(buf, recipe.input());
        writeList(buf, recipe.outputs(), AppraisalOutput.STREAM_CODEC);
        buf.writeVarInt(recipe.duration());
        buf.writeVarInt(recipe.energy());
        writeList(buf, recipe.modes(), AppraisalMode.STREAM_CODEC);
        buf.writeVarInt(recipe.minLevel());
        writeList(buf, recipe.integrations(), ByteBufCodecs.STRING_UTF8);
    }

    private static AppraisalRecipe decode(RegistryFriendlyByteBuf buf) {
        BlockSelector input = BlockSelector.STREAM_CODEC.decode(buf);
        List<AppraisalOutput> outputs = readList(buf, AppraisalOutput.STREAM_CODEC);
        int duration = buf.readVarInt();
        int energy = buf.readVarInt();
        List<AppraisalMode> modes = readList(buf, AppraisalMode.STREAM_CODEC);
        int minLevel = buf.readVarInt();
        List<String> integrations = readList(buf, ByteBufCodecs.STRING_UTF8);
        return new AppraisalRecipe(input, outputs, duration, energy, modes, minLevel, integrations);
    }

    private static <T> void writeList(RegistryFriendlyByteBuf buf, List<T> list,
                                      StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
        buf.writeVarInt(list.size());
        for (T value : list) {
            codec.encode(buf, value);
        }
    }

    private static <T> List<T> readList(RegistryFriendlyByteBuf buf,
                                        StreamCodec<? super RegistryFriendlyByteBuf, T> codec) {
        int size = buf.readVarInt();
        List<T> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(codec.decode(buf));
        }
        return List.copyOf(list);
    }

    public AppraisalRecipe {
        if (outputs.isEmpty()) {
            throw new IllegalArgumentException("鉴定配方至少需要一个产物");
        }
        if (duration < 0) {
            throw new IllegalArgumentException("鉴定配方的加工时间不能为负");
        }
        if (energy < 0) {
            throw new IllegalArgumentException("鉴定配方的能源消耗不能为负");
        }
        if (minLevel < 0) {
            throw new IllegalArgumentException("鉴定配方的最低等级不能为负");
        }
        outputs = List.copyOf(outputs);
        modes = List.copyOf(modes);
        integrations = List.copyOf(integrations);
        if (modes.isEmpty()) {
            throw new IllegalArgumentException("鉴定配方至少需要允许一种鉴定模式");
        }

        List<AppraisalOutput> resolved = new ArrayList<>(outputs.size());
        for (AppraisalOutput output : outputs) {
            if (output.minLevel() < 0) {
                resolved.add(new AppraisalOutput(output.result(), output.probability(), minLevel));
            } else {
                resolved.add(output);
            }
        }
        outputs = List.copyOf(resolved);
    }

    public boolean allows(AppraisalMode mode) {
        return modes.contains(mode);
    }

    public boolean allowsIntegration(String integration) {
        return integrations.contains(integration);
    }

    public double totalWeight() {
        double total = 0.0D;
        for (AppraisalOutput output : outputs) {
            if (output.probability() > 0.0D) {
                total += output.probability();
            }
        }
        return total;
    }

    @Override
    public boolean matches(AppraisalRecipeInput input, Level level) {
        return this.input.test(input.state());
    }

    @Override
    public ItemStack assemble(AppraisalRecipeInput input, HolderLookup.Provider registries) {
        return new ItemStack(outputs.getFirst().result().item().value());
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return new ItemStack(outputs.getFirst().result().item().value());
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.APPRAISAL_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.APPRAISAL_TYPE.get();
    }
}
