package net.yvanchuxiuzhen.system.attribute.api;

import java.util.Map;

/**
 * 一个来源对各属性项的加值，全部为加算。未列出的属性项视为 0。
 */
public record Contribution(Layer layer, Map<Stat, Double> values) {
    public Contribution {
        values = Map.copyOf(values);
    }

    public double get(Stat stat) {
        return values.getOrDefault(stat, 0.0);
    }
}
