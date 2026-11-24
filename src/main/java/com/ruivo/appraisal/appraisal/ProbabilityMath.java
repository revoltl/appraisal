package com.ruivo.appraisal.appraisal;

import java.util.function.DoubleSupplier;

/**
 * 概率计算的纯函数核心。不引用任何 Minecraft 类型，可以直接单元测试。
 */
public final class ProbabilityMath {

    private ProbabilityMath() {
    }

    /**
     * 加权随机选择。返回选中的下标，全部权重为零时返回 -1。
     */
    public static int weightedIndex(double[] weights, DoubleSupplier random) {
        double total = 0.0D;
        for (double weight : weights) {
            if (weight > 0.0D) {
                total += weight;
            }
        }
        if (total <= 0.0D) {
            return -1;
        }

        double cursor = random.getAsDouble() * total;
        int last = -1;
        for (int i = 0; i < weights.length; i++) {
            if (weights[i] <= 0.0D) {
                continue;
            }
            cursor -= weights[i];
            if (cursor < 0.0D) {
                return i;
            }
            last = i;
        }
        return last;
    }

    /**
     * 把百分数概率换算成产出份数。整数部分必定产出，小数部分按概率多产出一份。
     *
     * <p>350 的期望份数是 3.5：必定三份，另有五成概率产出第四份。</p>
     */
    public static int independentCount(double probability, DoubleSupplier random) {
        if (probability <= 0.0D) {
            return 0;
        }
        int guaranteed = (int) Math.floor(probability / 100.0D);
        double remainder = probability / 100.0D - guaranteed;
        return guaranteed + (random.getAsDouble() < remainder ? 1 : 0);
    }
}
