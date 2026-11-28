package com.ruivo.appraisal.machine;

import com.ruivo.appraisal.plugin.PluginStats;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

/**
 * 光伏发电的日照效率。按太阳角度计算，正午为 1，日出与日落约 0.39，夜晚为 0。
 * 天空不可见时为零，雨天与雷雨再乘折减系数。
 */
public final class SolarGeneration {

    private SolarGeneration() {
    }

    public static float sunlight(Level level, BlockPos pos) {
        if (!level.canSeeSky(pos.above())) {
            return 0.0F;
        }
        float sunAngle = level.getSunAngle(1.0F);
        if (sunAngle > Math.PI) {
            sunAngle = (float) (Math.PI * 2.0D - sunAngle);
        }
        float factor = Mth.clamp(1.5F * Mth.cos(sunAngle / 1.2F), 0.0F, 1.0F);
        float rain = level.getRainLevel(1.0F);
        float thunder = level.getThunderLevel(1.0F);
        factor *= 1.0F - rain * 0.5F;
        factor *= 1.0F - thunder * 0.75F;
        return Mth.clamp(factor, 0.0F, 1.0F);
    }

    public static int fePerTick(PluginStats stats, Level level, BlockPos pos) {
        if (stats.solarGeneration() <= 0.0D) {
            return 0;
        }
        double generation = stats.solarGeneration()
                * stats.generatorEfficiencyMultiplier()
                * sunlight(level, pos);
        return (int) Math.round(generation);
    }
}
