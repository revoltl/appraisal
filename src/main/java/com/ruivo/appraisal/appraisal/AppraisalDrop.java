package com.ruivo.appraisal.appraisal;

import com.ruivo.appraisal.recipe.OutputKind;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;

/**
 * 一次鉴定结算得到的逻辑产出。此时还未按堆叠上限拆分。
 */
public record AppraisalDrop(Holder<Item> item, OutputKind kind, int count) {
}
