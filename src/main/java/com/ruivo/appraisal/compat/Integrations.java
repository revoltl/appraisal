package com.ruivo.appraisal.compat;

import com.ruivo.appraisal.recipe.AppraisalRecipe;

import java.util.ArrayList;
import java.util.List;

/**
 * 内置联动的注册表。每个联动只在对应模组存在时注册，按注册顺序依次增补。
 */
public final class Integrations {

    private static final List<AppraisalIntegration> ALL = new ArrayList<>();

    private Integrations() {
    }

    public static void register(AppraisalIntegration integration) {
        ALL.add(integration);
    }

    public static AppraisalRecipe augment(AppraisalRecipe recipe) {
        AppraisalRecipe result = recipe;
        for (AppraisalIntegration integration : ALL) {
            result = integration.augment(result);
        }
        return result;
    }
}
