package com.ruivo.appraisal.compat.mekanism;

import com.ruivo.appraisal.compat.AppraisalIntegration;
import com.ruivo.appraisal.compat.OreDedup;
import com.ruivo.appraisal.recipe.AppraisalOutput;
import com.ruivo.appraisal.recipe.AppraisalRecipe;
import com.ruivo.appraisal.recipe.BlockSelector;
import com.ruivo.appraisal.recipe.OutputKind;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 通用机械的矿石增补。只在检测到 hosts/stone 或 hosts/deepslate 时追加对应矿石，
 * 已经存在的产物会跳过，所以整合包作者自行覆盖过的配方不会被重复填充。
 */
public final class MekanismIntegration implements AppraisalIntegration {

    private record OreEntry(String id, double probability, int minLevel) {
    }

    private static final List<OreEntry> STONE_ORES = List.of(
            new OreEntry("mekanism:tin_ore", 15.0D, 0),
            new OreEntry("mekanism:osmium_ore", 12.0D, 1),
            new OreEntry("mekanism:uranium_ore", 8.0D, 2),
            new OreEntry("mekanism:fluorite_ore", 10.0D, 1),
            new OreEntry("mekanism:lead_ore", 10.0D, 1)
    );

    private static final List<OreEntry> DEEPSLATE_ORES = List.of(
            new OreEntry("mekanism:deepslate_tin_ore", 15.0D, 1),
            new OreEntry("mekanism:deepslate_osmium_ore", 12.0D, 2),
            new OreEntry("mekanism:deepslate_uranium_ore", 8.0D, 3),
            new OreEntry("mekanism:deepslate_fluorite_ore", 10.0D, 2),
            new OreEntry("mekanism:deepslate_lead_ore", 10.0D, 2)
    );

    public MekanismIntegration() {
    }

    @Override
    public AppraisalRecipe augment(AppraisalRecipe recipe) {
        if (!recipe.allowsIntegration("mekanism")) {
            return recipe;
        }
        List<OreEntry> candidates;
        if (matches(recipe.input(), Blocks.STONE)) {
            candidates = STONE_ORES;
        } else if (matches(recipe.input(), Blocks.DEEPSLATE)) {
            candidates = DEEPSLATE_ORES;
        } else {
            return recipe;
        }

        Set<Item> existing = new HashSet<>();
        for (AppraisalOutput output : recipe.outputs()) {
            existing.add(output.result().item().value());
        }
        Set<Block> existingBlocks = OreDedup.collectBlocks(existing);

        List<AppraisalOutput> merged = new ArrayList<>(recipe.outputs());
        for (OreEntry entry : candidates) {
            Optional<Holder.Reference<Item>> holder = BuiltInRegistries.ITEM
                    .getHolder(ResourceLocation.parse(entry.id()));
            if (holder.isEmpty()) {
                continue;
            }
            Item item = holder.get().value();
            if (existing.contains(item)) {
                continue;
            }
            Block block = OreDedup.blockOf(item);
            if (block != null && OreDedup.conflicts(block, existingBlocks)) {
                continue;
            }
            merged.add(new AppraisalOutput(
                    new AppraisalOutput.Entry(OutputKind.BLOCK, holder.get()),
                    entry.probability(),
                    entry.minLevel()));
            existing.add(item);
            if (block != null) {
                existingBlocks.add(block);
            }
        }

        if (merged.size() == recipe.outputs().size()) {
            return recipe;
        }
        return new AppraisalRecipe(recipe.input(), merged, recipe.duration(), recipe.energy(),
                recipe.modes(), recipe.minLevel(), recipe.integrations());
    }

    private static boolean matches(BlockSelector selector, Block block) {
        if (selector.block() != null) {
            return selector.block() == block;
        }
        for (Holder<Block> holder : BuiltInRegistries.BLOCK.getTagOrEmpty(selector.tag())) {
            if (holder.value() == block) {
                return true;
            }
        }
        return false;
    }
}
