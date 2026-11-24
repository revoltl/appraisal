package com.ruivo.appraisal.compat;

import com.ruivo.appraisal.recipe.AppraisalRecipe;

/**
 * 内置联动的统一接口。实现方在配方基础上增补产物，返回新的配方。
 *
 * <p>联动必须按需增补：已经在配方里的产物不再追加，整合包作者自行覆盖过的配方也不会被
 * 重复填充。</p>
 */
public interface AppraisalIntegration {

    AppraisalRecipe augment(AppraisalRecipe recipe);
}
