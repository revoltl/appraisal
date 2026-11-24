package com.ruivo.appraisal.plugin;

import net.minecraft.util.RandomSource;

import java.util.List;
import java.util.Map;

/**
 * 分类模型插件的名称池。存的是语言键，训练完成时随机取一个，由客户端按当前语言解析。
 */
public final class ModelNames {

    private static final Map<ModelFamily, List<String>> POOLS = Map.of(
            ModelFamily.STATISTICS, List.of(
                    "model.appraisal.statistics.naive_bayes",
                    "model.appraisal.statistics.knn",
                    "model.appraisal.statistics.logistic_regression",
                    "model.appraisal.statistics.decision_tree",
                    "model.appraisal.statistics.svm"),
            ModelFamily.INTEGRATION, List.of(
                    "model.appraisal.integration.random_forest",
                    "model.appraisal.integration.adaboost",
                    "model.appraisal.integration.xgboost",
                    "model.appraisal.integration.lightgbm",
                    "model.appraisal.integration.catboost"),
            ModelFamily.LAYER_FEATURE, List.of(
                    "model.appraisal.layer_feature.mlp",
                    "model.appraisal.layer_feature.cnn",
                    "model.appraisal.layer_feature.rnn",
                    "model.appraisal.layer_feature.lstm",
                    "model.appraisal.layer_feature.gru"),
            ModelFamily.ATTENTION, List.of(
                    "model.appraisal.attention.visual_attention",
                    "model.appraisal.attention.shifted_window",
                    "model.appraisal.attention.multi_head",
                    "model.appraisal.attention.graph_attention")
    );

    private ModelNames() {
    }

    public static String random(ModelFamily family, RandomSource random) {
        List<String> pool = POOLS.get(family);
        return pool.get(random.nextInt(pool.size()));
    }
}
