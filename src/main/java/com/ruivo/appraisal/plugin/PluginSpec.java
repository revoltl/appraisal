package com.ruivo.appraisal.plugin;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * 单个插件携带的参数集合。百分比参数用小数表示，0.5 就是 50%。
 * 安装上限默认是无限，具体插件可以单独收窄。
 */
public final class PluginSpec {

    private final Map<PluginStat, Double> values;
    private final int installLimit;

    private PluginSpec(Map<PluginStat, Double> values, int installLimit) {
        this.values = Collections.unmodifiableMap(values);
        this.installLimit = installLimit;
    }

    public static Builder builder() {
        return new Builder();
    }

    public double get(PluginStat stat) {
        return values.getOrDefault(stat, 0.0D);
    }

    public Map<PluginStat, Double> values() {
        return values;
    }

    public int installLimit() {
        return installLimit;
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public static final class Builder {

        private final Map<PluginStat, Double> values = new EnumMap<>(PluginStat.class);
        private int installLimit = Integer.MAX_VALUE;

        public Builder put(PluginStat stat, double value) {
            if (value != 0.0D) {
                values.put(stat, value);
            }
            return this;
        }

        public Builder installLimit(int limit) {
            this.installLimit = Math.max(1, limit);
            return this;
        }

        public PluginSpec build() {
            return new PluginSpec(new EnumMap<>(values), installLimit);
        }
    }
}
