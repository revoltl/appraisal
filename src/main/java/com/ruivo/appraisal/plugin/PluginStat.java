package com.ruivo.appraisal.plugin;

/**
 * 插件参数的种类。枚举顺序同时是 Tooltip 与属性面板的组内顺序。
 *
 * <p>{@code positiveIsGood} 表示正数对玩家有利。能量、方块、冷却液三项的消耗
 * 以负数为好，所以标记为假。</p>
 *
 * <p>{@code group} 决定属性面板把它画在九宫格左侧还是右侧。左侧放能量相关，
 * 右侧放鉴定相关。</p>
 */
public enum PluginStat {

    APPRAISAL_LEVEL("appraisal_level", Format.LEVEL, true, Group.RIGHT),
    APPRAISAL_SPEED("appraisal_speed", Format.PERCENT, true, Group.RIGHT),
    PRODUCT_CHANCE("product_chance", Format.PERCENT, true, Group.RIGHT),
    BLOCK_CONSUMPTION("block_consumption", Format.PERCENT, false, Group.RIGHT),
    ENERGY_CONSUMPTION("energy_consumption", Format.PERCENT, false, Group.LEFT),
    COOLANT_CONSUMPTION("coolant_consumption", Format.PERCENT, false, Group.RIGHT),
    VOID("void", Format.BOOLEAN, true, Group.RIGHT),
    CAPACITY("capacity", Format.PERCENT, true, Group.LEFT),
    TRANSFER("transfer", Format.PERCENT, true, Group.LEFT),
    GENERATOR_EFFICIENCY("generator_efficiency", Format.PERCENT, true, Group.LEFT),
    SOLAR_GENERATION("solar_generation", Format.FLAT, true, Group.LEFT);

    public enum Format {
        LEVEL,
        PERCENT,
        FLAT,
        BOOLEAN
    }

    public enum Group {
        LEFT,
        RIGHT
    }

    private final String id;
    private final Format format;
    private final boolean positiveIsGood;
    private final Group group;

    PluginStat(String id, Format format, boolean positiveIsGood, Group group) {
        this.id = id;
        this.format = format;
        this.positiveIsGood = positiveIsGood;
        this.group = group;
    }

    public String id() {
        return id;
    }

    public Format format() {
        return format;
    }

    public boolean positiveIsGood() {
        return positiveIsGood;
    }

    public Group group() {
        return group;
    }

    public String tooltipKey() {
        return "tooltip.appraisal.plugin." + id;
    }

    public String labelKey() {
        return "gui.appraisal.plugin.stat." + id;
    }
}
