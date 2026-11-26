package com.ruivo.appraisal.compat;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 矿石去重。按通用矿物标签判断，而不是按物品 ID。
 *
 * <p>不同模组提供的同一种矿物会挂在同一个 c:ores 标签下，比如通用机械的锇矿与
 * AllTheOres 的锇矿都在 c:ores/osmium 里。只要候选矿石与配方里已有的矿石共享任一
 * ores 标签，就视为同一种矿物，跳过追加。</p>
 */
public final class OreDedup {

    private OreDedup() {
    }

    public static boolean conflicts(Block candidate, Set<Block> existing) {
        Set<TagKey<Block>> candidateTags = oreTags(candidate);
        if (candidateTags.isEmpty()) {
            return false;
        }
        for (Block block : existing) {
            for (TagKey<Block> tag : oreTags(block)) {
                if (candidateTags.contains(tag)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Block blockOf(Item item) {
        return item instanceof BlockItem blockItem ? blockItem.getBlock() : null;
    }

    public static Set<Block> collectBlocks(Iterable<Item> items) {
        Set<Block> blocks = new HashSet<>();
        for (Item item : items) {
            Block block = blockOf(item);
            if (block != null) {
                blocks.add(block);
            }
        }
        return blocks;
    }

    private static Set<TagKey<Block>> oreTags(Block block) {
        return BuiltInRegistries.BLOCK.wrapAsHolder(block).tags()
                .filter(tag -> tag.location().getPath().startsWith("ores/"))
                .collect(Collectors.toSet());
    }
}
