package com.ruivo.appraisal.recipe;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * 鉴定配方的输入选择器。支持单个方块与方块标签，匹配对象是 BlockState。
 */
public record BlockSelector(@Nullable Block block, @Nullable TagKey<Block> tag) {

    public static final Codec<BlockSelector> CODEC = Codec.either(
            BuiltInRegistries.BLOCK.byNameCodec(),
            TagKey.hashedCodec(Registries.BLOCK)
    ).xmap(
            either -> either.map(
                    value -> new BlockSelector(value, null),
                    value -> new BlockSelector(null, value)
            ),
            selector -> selector.tag() != null ? Either.right(selector.tag()) : Either.left(selector.block())
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, BlockSelector> STREAM_CODEC = StreamCodec.of(
            (buf, selector) -> {
                if (selector.tag() != null) {
                    buf.writeBoolean(true);
                    buf.writeResourceLocation(selector.tag().location());
                } else {
                    buf.writeBoolean(false);
                    buf.writeResourceLocation(BuiltInRegistries.BLOCK.getKey(selector.block()));
                }
            },
            buf -> buf.readBoolean()
                    ? new BlockSelector(null, TagKey.create(Registries.BLOCK, buf.readResourceLocation()))
                    : new BlockSelector(BuiltInRegistries.BLOCK.get(buf.readResourceLocation()), null)
    );

    public BlockSelector {
        if ((block == null) == (tag == null)) {
            throw new IllegalArgumentException("鉴定输入必须且只能指定方块或方块标签其中之一");
        }
    }

    public boolean test(BlockState state) {
        return tag != null ? state.is(tag) : state.is(block);
    }

    public boolean test(ItemStack stack) {
        return stack.getItem() instanceof BlockItem blockItem && test(blockItem.getBlock().defaultBlockState());
    }

    /**
     * 配方输入的代表方块。单个方块直接返回自己，标签取标签里的第一个方块。
     * 训练结果按这个方块记录，同一配方下的所有方块共享词条。
     */
    public Block representativeBlock() {
        if (block != null) {
            return block;
        }
        var holders = BuiltInRegistries.BLOCK.getTag(tag)
                .orElseThrow(() -> new IllegalStateException(
                        "鉴定配方的方块标签为空：" + tag.location()));
        var iterator = holders.iterator();
        if (!iterator.hasNext()) {
            throw new IllegalStateException("鉴定配方的方块标签为空：" + tag.location());
        }
        return iterator.next().value();
    }
}
