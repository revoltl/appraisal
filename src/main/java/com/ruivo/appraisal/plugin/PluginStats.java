package com.ruivo.appraisal.plugin;

/**
 * 一台机器上所有插件的参数合计。派生值把原始合计换算成机器可以直接使用的倍率与概率。
 */
public record PluginStats(
        double appraisalLevel,
        double appraisalSpeed,
        double productChance,
        double blockConsumption,
        double energyConsumption,
        double capacity,
        double transfer,
        double generatorEfficiency,
        double coolantConsumption,
        boolean voidEnabled
) {

    public static final PluginStats EMPTY = new PluginStats(
            0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D, false);

    /**
     * 把若干插件参数加算成一台机器的合计。纯 Java，便于单元测试。
     */
    public static PluginStats combine(Iterable<PluginSpec> specs) {
        double level = 0.0D;
        double speed = 0.0D;
        double chance = 0.0D;
        double block = 0.0D;
        double energy = 0.0D;
        double capacity = 0.0D;
        double transfer = 0.0D;
        double generatorEfficiency = 0.0D;
        double coolant = 0.0D;
        boolean voidEnabled = false;

        for (PluginSpec spec : specs) {
            level += spec.get(PluginStat.APPRAISAL_LEVEL);
            speed += spec.get(PluginStat.APPRAISAL_SPEED);
            chance += spec.get(PluginStat.PRODUCT_CHANCE);
            block += spec.get(PluginStat.BLOCK_CONSUMPTION);
            energy += spec.get(PluginStat.ENERGY_CONSUMPTION);
            capacity += spec.get(PluginStat.CAPACITY);
            transfer += spec.get(PluginStat.TRANSFER);
            generatorEfficiency += spec.get(PluginStat.GENERATOR_EFFICIENCY);
            coolant += spec.get(PluginStat.COOLANT_CONSUMPTION);
            voidEnabled |= spec.get(PluginStat.VOID) > 0.0D;
        }

        return new PluginStats(level, speed, chance, block, energy, capacity, transfer,
                generatorEfficiency, coolant, voidEnabled);
    }

    public int levelBonus() {
        return (int) Math.round(appraisalLevel);
    }

    /** 速度倍率。加算后允许变慢，下限零点一倍，避免除零和无限拖长。 */
    public double speedMultiplier() {
        return Math.max(0.1D, 1.0D + appraisalSpeed);
    }

    /** 产物概率倍率。允许被负参数压到零。 */
    public double chanceMultiplier() {
        return Math.max(0.0D, 1.0D + productChance);
    }

    /** 待鉴定方块的消耗概率。下限零，为零时方块不消耗。 */
    public double blockConsumeChance() {
        return clamp01(1.0D + blockConsumption);
    }

    /** 总能耗倍率。允许被节能插件压到零。 */
    public double energyMultiplier() {
        return Math.max(0.0D, 1.0D + energyConsumption);
    }

    /** 能量容量倍率。下限零点一倍，避免容量被压到零。 */
    public double capacityMultiplier() {
        return Math.max(0.1D, 1.0D + capacity);
    }

    /** 能量传输倍率。允许被负参数压到零。 */
    public double transferMultiplier() {
        return Math.max(0.0D, 1.0D + transfer);
    }

    /** 发电机产能倍率。只被衰变发电机读取。 */
    public double generatorEfficiencyMultiplier() {
        return Math.max(0.0D, 1.0D + generatorEfficiency);
    }

    /** 每次鉴定消耗冷却槽容量的比例，上限百分之百。 */
    public double coolantFraction() {
        return clamp01(coolantConsumption);
    }

    public boolean hasAny() {
        return appraisalLevel != 0.0D
                || appraisalSpeed != 0.0D
                || productChance != 0.0D
                || blockConsumption != 0.0D
                || energyConsumption != 0.0D
                || capacity != 0.0D
                || transfer != 0.0D
                || generatorEfficiency != 0.0D
                || coolantConsumption != 0.0D
                || voidEnabled;
    }

    private static double clamp01(double value) {
        return Math.max(0.0D, Math.min(1.0D, value));
    }
}
