package net.yvanchuxiuzhen.system.attribute.rule;

import java.util.Collection;
import net.yvanchuxiuzhen.system.attribute.api.Contribution;
import net.yvanchuxiuzhen.system.attribute.api.Layer;
import net.yvanchuxiuzhen.system.attribute.api.Stat;

/**
 * 属性汇总与五行公式。1.18.2 版本的属性计算全部是加算，没有乘算、上限与取整：
 * 基础值 = 初始值 + 基础层贡献之和；最终值 = 基础值 + 加成层贡献之和（来源的 {@code _display}）。
 */
public final class AttributeTotals {
    private static final Stat[] STATS = Stat.values();
    private final double[] base = new double[STATS.length];
    private final double[] value = new double[STATS.length];

    public AttributeTotals(Collection<Contribution> contributions) {
        for (Stat stat : STATS) {
            base[stat.ordinal()] = stat.initial();
        }
        for (Contribution contribution : contributions) {
            if (contribution.layer() == Layer.BASE) {
                contribution.values().forEach((stat, amount) -> base[stat.ordinal()] += amount);
            }
        }
        System.arraycopy(base, 0, value, 0, base.length);
        for (Contribution contribution : contributions) {
            if (contribution.layer() == Layer.BONUS) {
                contribution.values().forEach((stat, amount) -> value[stat.ordinal()] += amount);
            }
        }
    }

    public double base(Stat stat) {
        return base[stat.ordinal()];
    }

    public double value(Stat stat) {
        return value[stat.ordinal()];
    }

    /**
     * 攻击方的五行系数项：灵根值 × 0.1。战斗把它与受击方的 {@code Damage_multiplier} 相加后乘到法术伤害上。
     */
    public static double elementCoefficient(double root) {
        return root * 0.1;
    }

    /**
     * 受击方的功法五行项：功法五行值 × 功法层数 / 4，加到伤害上。
     * 来源的功法五行值全为正，所以受击方主修与伤害同属性的功法时受到的伤害增加（Q18，负责人 2026-11-03 指示保持来源行为）。
     */
    public static double receivedElementBonus(double gongfaElement, double gongfaLevel) {
        return gongfaElement * gongfaLevel / 4;
    }
}
