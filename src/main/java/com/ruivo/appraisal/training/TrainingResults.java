package com.ruivo.appraisal.training;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 分类模型插件上的训练结果。按待鉴定方块分组，记录每个产物对应的倍率。
 */
public final class TrainingResults {

    private static final String ROOT = "training";
    private static final String SUPPRESS = "suppress_others";

    private TrainingResults() {
    }

    public static Map<ResourceLocation, Map<ResourceLocation, Double>> read(ItemStack stack) {
        Map<ResourceLocation, Map<ResourceLocation, Double>> results = new LinkedHashMap<>();
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return results;
        }
        CompoundTag root = data.copyTag();
        if (!root.contains(ROOT)) {
            return results;
        }
        CompoundTag training = root.getCompound(ROOT);
        for (String blockKey : training.getAllKeys()) {
            ResourceLocation blockId = ResourceLocation.tryParse(blockKey);
            if (blockId == null) {
                continue;
            }
            CompoundTag items = training.getCompound(blockKey);
            Map<ResourceLocation, Double> entries = new LinkedHashMap<>();
            for (String itemKey : items.getAllKeys()) {
                ResourceLocation itemId = ResourceLocation.tryParse(itemKey);
                if (itemId != null) {
                    entries.put(itemId, items.getDouble(itemKey));
                }
            }
            if (!entries.isEmpty()) {
                results.put(blockId, entries);
            }
        }
        return results;
    }

    public static boolean isSuppressing(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return false;
        }
        CompoundTag root = data.copyTag();
        return root.contains(ROOT) && root.getCompound(ROOT).getBoolean(SUPPRESS);
    }

    public static void write(ItemStack stack, Block block, Map<ResourceLocation, Double> entries,
                             boolean suppress) {
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> {
            CompoundTag root = new CompoundTag();
            CompoundTag items = new CompoundTag();
            for (Map.Entry<ResourceLocation, Double> entry : entries.entrySet()) {
                items.putDouble(entry.getKey().toString(), entry.getValue());
            }
            root.put(BuiltInRegistries.BLOCK.getKey(block).toString(), items);
            if (suppress) {
                root.putBoolean(SUPPRESS, true);
            }
            tag.put(ROOT, root);
        });
    }
}
