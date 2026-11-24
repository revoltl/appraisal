package com.ruivo.appraisal.plugin;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 插件参数聚合的纯逻辑测试。不引用任何 Minecraft 类型。
 */
class PluginStatsTest {

    private static PluginSpec spec(PluginStat stat, double value) {
        return PluginSpec.builder().put(stat, value).build();
    }

    private static PluginSpec productivity() {
        return PluginSpec.builder()
                .put(PluginStat.PRODUCT_CHANCE, 0.1D)
                .put(PluginStat.BLOCK_CONSUMPTION, -0.05D)
                .put(PluginStat.APPRAISAL_SPEED, -0.15D)
                .put(PluginStat.ENERGY_CONSUMPTION, 0.8D)
                .build();
    }

    @Test
    void emptyPluginsGiveNeutralStats() {
        PluginStats stats = PluginStats.combine(List.of());
        assertEquals(0, stats.levelBonus());
        assertEquals(1.0D, stats.speedMultiplier(), 1.0E-9D);
        assertEquals(1.0D, stats.chanceMultiplier(), 1.0E-9D);
        assertEquals(1.0D, stats.blockConsumeChance(), 1.0E-9D);
        assertEquals(1.0D, stats.energyMultiplier(), 1.0E-9D);
        assertEquals(1.0D, stats.capacityMultiplier(), 1.0E-9D);
        assertEquals(1.0D, stats.transferMultiplier(), 1.0E-9D);
        assertEquals(0.0D, stats.coolantFraction(), 1.0E-9D);
        assertFalse(stats.voidEnabled());
        assertFalse(stats.hasAny());
    }

    @Test
    void nineProductivityPluginsLeaveFiftyFivePercentBlockCost() {
        PluginStats stats = PluginStats.combine(Collections.nCopies(9, productivity()));
        assertEquals(0.55D, stats.blockConsumeChance(), 1.0E-9D);
        assertEquals(1.9D, stats.chanceMultiplier(), 1.0E-9D);
        assertEquals(8.2D, stats.energyMultiplier(), 1.0E-9D);
    }

    @Test
    void blockCostNeverGoesBelowZero() {
        PluginStats stats = PluginStats.combine(Collections.nCopies(21, productivity()));
        assertEquals(0.0D, stats.blockConsumeChance(), 1.0E-9D);
    }

    @Test
    void coolantFractionCapsAtOne() {
        PluginSpec coolant = spec(PluginStat.COOLANT_CONSUMPTION, 0.05D);
        PluginStats stats = PluginStats.combine(Collections.nCopies(21, coolant));
        assertEquals(1.0D, stats.coolantFraction(), 1.0E-9D);
    }

    @Test
    void energyMultiplierNeverGoesBelowZero() {
        PluginSpec saving = spec(PluginStat.ENERGY_CONSUMPTION, -0.5D);
        PluginStats stats = PluginStats.combine(Collections.nCopies(3, saving));
        assertEquals(0.0D, stats.energyMultiplier(), 1.0E-9D);
    }

    @Test
    void speedMultiplierHasFloor() {
        PluginStats stats = PluginStats.combine(Collections.nCopies(9, productivity()));
        assertEquals(0.1D, stats.speedMultiplier(), 1.0E-9D);
    }

    @Test
    void voidPluginTurnsVoidOn() {
        PluginStats stats = PluginStats.combine(List.of(spec(PluginStat.VOID, 1.0D)));
        assertTrue(stats.voidEnabled());
        assertTrue(stats.hasAny());
    }

    @Test
    void levelPluginsAddUp() {
        PluginSpec level = spec(PluginStat.APPRAISAL_LEVEL, 1.0D);
        PluginStats stats = PluginStats.combine(Collections.nCopies(3, level));
        assertEquals(3, stats.levelBonus());
    }

    @Test
    void capacitorPluginsIncreaseCapacityAndTransfer() {
        PluginSpec capacitor = PluginSpec.builder()
                .put(PluginStat.CAPACITY, 0.5D)
                .put(PluginStat.TRANSFER, 0.5D)
                .build();
        PluginStats stats = PluginStats.combine(Collections.nCopies(9, capacitor));
        assertEquals(5.5D, stats.capacityMultiplier(), 1.0E-9D);
        assertEquals(5.5D, stats.transferMultiplier(), 1.0E-9D);
    }

    @Test
    void capacityMultiplierHasFloor() {
        PluginStats stats = PluginStats.combine(List.of(spec(PluginStat.CAPACITY, -1.5D)));
        assertEquals(0.1D, stats.capacityMultiplier(), 1.0E-9D);
    }
}
