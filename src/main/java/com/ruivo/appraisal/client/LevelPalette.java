package com.ruivo.appraisal.client;

import net.minecraft.util.Mth;

/**
 * 鉴定等级的配色表。零到五级是预设色，六级以上进入彩虹渐变，作为越界等级的最终形态。
 * JEI 等级框与世界标签共用这张表。
 */
public final class LevelPalette {

    private static final int[] COLORS = {
            0x9E9E9E, 0x55C455, 0x5B8DEF, 0xA05BD6, 0xE0A93B, 0xD84C4C
    };
    private static final long RAINBOW_PERIOD = 3000L;

    private LevelPalette() {
    }

    public static int color(int level) {
        if (level > COLORS.length - 1) {
            float hue = (System.currentTimeMillis() % RAINBOW_PERIOD) / (float) RAINBOW_PERIOD;
            return Mth.hsvToRgb(hue, 1.0F, 1.0F);
        }
        return COLORS[Math.max(level, 0)];
    }

    public static float red(int color) {
        return ((color >> 16) & 0xFF) / 255.0F;
    }

    public static float green(int color) {
        return ((color >> 8) & 0xFF) / 255.0F;
    }

    public static float blue(int color) {
        return (color & 0xFF) / 255.0F;
    }
}
