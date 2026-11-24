package com.ruivo.appraisal.appraisal;

import com.ruivo.appraisal.recipe.AppraisalOutput;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;

/**
 * 把配方的产物列表接到概率核心上。世界交互与机器加工都从这里结算。
 */
public final class ProbabilityCalculator {

    private ProbabilityCalculator() {
    }

    /**
     * 直接鉴定：把 probability 当作权重，按权重选出一个产物，产出一份。
     */
    public static List<AppraisalDrop> rollWeighted(List<AppraisalOutput> outputs, RandomSource random) {
        double[] weights = new double[outputs.size()];
        for (int i = 0; i < outputs.size(); i++) {
            weights[i] = outputs.get(i).probability();
        }
        int index = ProbabilityMath.weightedIndex(weights, random::nextDouble);
        if (index < 0) {
            return List.of();
        }
        AppraisalOutput output = outputs.get(index);
        return List.of(new AppraisalDrop(output.result().item(), output.result().kind(), 1));
    }

    /**
     * 进阶鉴定：每个产物独立判断，产出份数由概率换算。
     */
    public static List<AppraisalDrop> rollIndependent(List<AppraisalOutput> outputs, RandomSource random) {
        List<AppraisalDrop> drops = new ArrayList<>();
        for (AppraisalOutput output : outputs) {
            int count = ProbabilityMath.independentCount(output.probability(), random::nextDouble);
            if (count > 0) {
                drops.add(new AppraisalDrop(output.result().item(), output.result().kind(), count));
            }
        }
        return drops;
    }
}
