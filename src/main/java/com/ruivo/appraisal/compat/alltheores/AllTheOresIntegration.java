package com.ruivo.appraisal.compat.alltheores;

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
 * AllTheOres 的矿石增补。权重按各矿石的生成丰度换算，丰度取自模组自带的
 * placed_feature 与 configured_feature：count 乘以 size，再除以十取整。
 * 常见矿物落在低等级，铱、铂、宝石与硫落在高一级。
 */
public final class AllTheOresIntegration implements AppraisalIntegration {

    private record OreEntry(String id, double probability, int minLevel) {
    }

    private static final List<OreEntry> STONE_ORES = List.of(
            new OreEntry("alltheores:aluminum_ore", 10.0D, 0),
            new OreEntry("alltheores:tin_ore", 10.0D, 0),
            new OreEntry("alltheores:cinnabar_ore", 8.0D, 0),
            new OreEntry("alltheores:fluorite_ore", 8.0D, 0),
            new OreEntry("alltheores:salt_ore", 8.0D, 0),
            new OreEntry("alltheores:lead_ore", 7.0D, 0),
            new OreEntry("alltheores:nickel_ore", 7.0D, 0),
            new OreEntry("alltheores:osmium_ore", 7.0D, 0),
            new OreEntry("alltheores:silver_ore", 7.0D, 0),
            new OreEntry("alltheores:zinc_ore", 7.0D, 0),
            new OreEntry("alltheores:uranium_ore", 5.0D, 0),
            new OreEntry("alltheores:iridium_ore", 4.0D, 1),
            new OreEntry("alltheores:platinum_ore", 2.0D, 1),
            new OreEntry("alltheores:peridot_ore", 2.0D, 1),
            new OreEntry("alltheores:ruby_ore", 2.0D, 1),
            new OreEntry("alltheores:sapphire_ore", 2.0D, 1),
            new OreEntry("alltheores:sulfur_ore", 2.0D, 1)
    );

    private static final List<OreEntry> DEEPSLATE_ORES = List.of(
            new OreEntry("alltheores:deepslate_aluminum_ore", 10.0D, 1),
            new OreEntry("alltheores:deepslate_tin_ore", 10.0D, 1),
            new OreEntry("alltheores:deepslate_cinnabar_ore", 8.0D, 1),
            new OreEntry("alltheores:deepslate_fluorite_ore", 8.0D, 1),
            new OreEntry("alltheores:deepslate_salt_ore", 8.0D, 1),
            new OreEntry("alltheores:deepslate_lead_ore", 7.0D, 1),
            new OreEntry("alltheores:deepslate_nickel_ore", 7.0D, 1),
            new OreEntry("alltheores:deepslate_osmium_ore", 7.0D, 1),
            new OreEntry("alltheores:deepslate_silver_ore", 7.0D, 1),
            new OreEntry("alltheores:deepslate_zinc_ore", 7.0D, 1),
            new OreEntry("alltheores:deepslate_uranium_ore", 5.0D, 1),
            new OreEntry("alltheores:deepslate_iridium_ore", 4.0D, 2),
            new OreEntry("alltheores:deepslate_platinum_ore", 2.0D, 2),
            new OreEntry("alltheores:deepslate_peridot_ore", 2.0D, 2),
            new OreEntry("alltheores:deepslate_ruby_ore", 2.0D, 2),
            new OreEntry("alltheores:deepslate_sapphire_ore", 2.0D, 2),
            new OreEntry("alltheores:deepslate_sulfur_ore", 2.0D, 2)
    );

    private static final List<OreEntry> NETHER_ORES = List.of(
            new OreEntry("alltheores:nether_aluminum_ore", 10.0D, 1),
            new OreEntry("alltheores:nether_tin_ore", 10.0D, 1),
            new OreEntry("alltheores:nether_cinnabar_ore", 8.0D, 1),
            new OreEntry("alltheores:nether_fluorite_ore", 8.0D, 1),
            new OreEntry("alltheores:nether_salt_ore", 8.0D, 1),
            new OreEntry("alltheores:nether_lead_ore", 7.0D, 1),
            new OreEntry("alltheores:nether_nickel_ore", 7.0D, 1),
            new OreEntry("alltheores:nether_osmium_ore", 7.0D, 1),
            new OreEntry("alltheores:nether_silver_ore", 7.0D, 1),
            new OreEntry("alltheores:nether_zinc_ore", 7.0D, 1),
            new OreEntry("alltheores:nether_uranium_ore", 5.0D, 1),
            new OreEntry("alltheores:nether_iridium_ore", 4.0D, 2),
            new OreEntry("alltheores:nether_platinum_ore", 2.0D, 2),
            new OreEntry("alltheores:nether_peridot_ore", 2.0D, 2),
            new OreEntry("alltheores:nether_ruby_ore", 2.0D, 2),
            new OreEntry("alltheores:nether_sapphire_ore", 2.0D, 2),
            new OreEntry("alltheores:nether_sulfur_ore", 2.0D, 2)
    );

    private static final List<OreEntry> END_ORES = List.of(
            new OreEntry("alltheores:end_aluminum_ore", 10.0D, 2),
            new OreEntry("alltheores:end_tin_ore", 10.0D, 2),
            new OreEntry("alltheores:end_cinnabar_ore", 8.0D, 2),
            new OreEntry("alltheores:end_fluorite_ore", 8.0D, 2),
            new OreEntry("alltheores:end_salt_ore", 8.0D, 2),
            new OreEntry("alltheores:end_lead_ore", 7.0D, 2),
            new OreEntry("alltheores:end_nickel_ore", 7.0D, 2),
            new OreEntry("alltheores:end_osmium_ore", 7.0D, 2),
            new OreEntry("alltheores:end_silver_ore", 7.0D, 2),
            new OreEntry("alltheores:end_zinc_ore", 7.0D, 2),
            new OreEntry("alltheores:end_uranium_ore", 5.0D, 2),
            new OreEntry("alltheores:end_iridium_ore", 4.0D, 3),
            new OreEntry("alltheores:end_platinum_ore", 2.0D, 3),
            new OreEntry("alltheores:end_peridot_ore", 2.0D, 3),
            new OreEntry("alltheores:end_ruby_ore", 2.0D, 3),
            new OreEntry("alltheores:end_sapphire_ore", 2.0D, 3),
            new OreEntry("alltheores:end_sulfur_ore", 2.0D, 3)
    );

    public AllTheOresIntegration() {
    }

    @Override
    public AppraisalRecipe augment(AppraisalRecipe recipe) {
        if (!recipe.allowsIntegration("alltheores")) {
            return recipe;
        }

        List<OreEntry> candidates;
        if (matches(recipe.input(), Blocks.STONE)) {
            candidates = STONE_ORES;
        } else if (matches(recipe.input(), Blocks.DEEPSLATE)) {
            candidates = DEEPSLATE_ORES;
        } else if (matches(recipe.input(), Blocks.NETHERRACK)) {
            candidates = NETHER_ORES;
        } else if (matches(recipe.input(), Blocks.END_STONE)) {
            candidates = END_ORES;
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
