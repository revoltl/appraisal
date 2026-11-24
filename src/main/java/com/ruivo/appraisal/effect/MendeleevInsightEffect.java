package com.ruivo.appraisal.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * 门捷列夫之洞察。本身不改变任何原版属性，鉴定等级由
 * {@link com.ruivo.appraisal.appraisal.AppraisalLevels} 读取放大器后计算。
 */
public class MendeleevInsightEffect extends MobEffect {

    public MendeleevInsightEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x9A68CA);
    }
}
