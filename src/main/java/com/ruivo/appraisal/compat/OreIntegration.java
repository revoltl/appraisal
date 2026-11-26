package com.ruivo.appraisal.compat;

import com.ruivo.appraisal.recipe.AppraisalOutput;
import com.ruivo.appraisal.recipe.AppraisalRecipe;
import com.ruivo.appraisal.recipe.BlockSelector;
import com.ruivo.appraisal.recipe.OutputKind;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * 矿石联动的公共模板。子类只提供联动 ID、产物类型和每种输入方块对应的矿石列表，
 * 匹配输入、去重、等级叠加和配方重建都在这里完成。
 */
public abstract class OreIntegration implements AppraisalIntegration {

    public record OreEntry(String id, double probability, int toolLevel) {
    }

    private final String integrationId;
    private final OutputKind kind;
    private final Map<Block, List<OreEntry>> entries;

    protected OreIntegration(String integrationId, OutputKind kind,
                             Map<Block, List<OreEntry>> entries) {
        this.integrationId = integrationId;
        this.kind = kind;
        this.entries = Map.copyOf(entries);
    }

    @Override
    public final AppraisalRecipe augment(AppraisalRecipe recipe) {
        if (!recipe.allowsIntegration(integrationId)) {
            return recipe;
        }
        Block input = matchedInput(recipe.input());
        if (input == null) {
            return recipe;
        }
        List<OreEntry> candidates = entries.get(input);
        if (candidates == null || candidates.isEmpty()) {
            return recipe;
        }

        Set<Item> existing = new HashSet<>();
        for (AppraisalOutput output : recipe.outputs()) {
            existing.add(output.result().item().value());
        }
        Set<Block> existingBlocks = kind == OutputKind.BLOCK
                ? OreDedup.collectBlocks(existing)
                : Set.of();

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
            Block block = null;
            if (kind == OutputKind.BLOCK) {
                block = OreDedup.blockOf(item);
                if (block != null && OreDedup.conflicts(block, existingBlocks)) {
                    continue;
                }
            }
            merged.add(new AppraisalOutput(
                    new AppraisalOutput.Entry(kind, holder.get()),
                    entry.probability(),
                    recipe.minLevel() + entry.toolLevel()));
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

    @Nullable
    private Block matchedInput(BlockSelector selector) {
        if (selector.block() != null) {
            return entries.containsKey(selector.block()) ? selector.block() : null;
        }
        for (Holder<Block> holder : BuiltInRegistries.BLOCK.getTagOrEmpty(selector.tag())) {
            if (entries.containsKey(holder.value())) {
                return holder.value();
            }
        }
        return null;
    }
}
