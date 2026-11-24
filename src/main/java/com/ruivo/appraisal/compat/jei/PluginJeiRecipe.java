package com.ruivo.appraisal.compat.jei;

import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * 插件页的展示数据。只有一条记录，里面是当前所有带插件标签的物品。
 */
public record PluginJeiRecipe(List<ItemStack> plugins) {
}
