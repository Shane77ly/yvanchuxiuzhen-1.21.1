package net.yvanchuxiuzhen.system.gongfa.api;

import net.yvanchuxiuzhen.system.attribute.api.Stat;

/**
 * 功法的十三项属性加成（来源功法书的 {@code Technique*} 档位与 {@code Exercises*} 数值），顺序同来源。
 * 数值 = 基础值(功法层数) × 档位系数，除移速外向上取整；基础值按层数 1–10、11–20、21–30、31 以上分段线性
 * （来源 {@code GongfaHealthUp} 等十三个过程；研习时的初值 {@code Gongfashuxingjisuan} 即层数 1 的值）。
 */
public enum TechniqueStat {
    HEALTH_MAX("TechniqueHealth", "ExercisesHeath", Stat.HEALTH_MAX, 60, 4, 108, 12, 252, 16, 452, 20),
    HEALTH_REC("TechniqueHeathRec", "ExercisesHeathRec", Stat.HEALTH_REC, 1, 0.5),
    POWER_MAX("Techniquepower", "Exercisespower", Stat.POWER_MAX, 25, 25, 310, 40, 770, 60, 1470, 80),
    POWER_REC("TechniquepowerRec", "ExercisespowerRec", Stat.POWER_REC, 0, 3),
    SOUL_MAX("TechniqueSoul", "ExercisesSoul", Stat.SOUL_MAX, 25, 25, 310, 40, 770, 60, 1470, 80),
    SOUL_REC("TechniqueSoulRec", "ExercisesSoulRec", Stat.SOUL_REC, 0, 3),
    ATTACK("TechniqueAttack", "ExercisesAttack", Stat.ATTACK, 4.5, 0.3, 8.1, 0.9, 18.9, 1.2, 33.9, 1.5),
    ATTACK_DEF("TechniqueAttackDef", "ExercisesAttackDef", Stat.ATTACK_DEF, 6, 0.4, 10.8, 1.2, 25.2, 1.6, 45.2, 2),
    MANA_ATT("TechniqueMana", "ExercisesMana", Stat.MANA_ATT, 4.5, 0.3, 8.1, 0.9, 18.9, 1.2, 33.9, 1.5),
    MANA_DEF("TechniqueManaDef", "ExercisesManaDef", Stat.MANA_DEF, 6, 0.4, 10.8, 1.2, 25.2, 1.6, 45.2, 2),
    SOUL_ATT("TechniqueSouatt", "ExercisesSouatt", Stat.SOUL_ATT, 4.5, 0.3, 8.1, 0.9, 18.9, 1.2, 33.9, 1.5),
    SOUL_DEF("TechniqueSouDef", "ExercisesSouDef", Stat.SOUL_DEF, 3, 0.2, 5.4, 0.6, 12.6, 0.8, 22.6, 1),
    MOVE_SPEED("TechniqueSpeed", "ExercisesSpeed", Stat.MOVE_SPEED, 0.03, 0.01);

    private final String tierKey;
    private final String valueKey;
    private final Stat stat;
    /** 分段的起点与斜率：层数 1–10、11–20、21–30、31 以上；只有两个数时为全程线性（起点 + 层数 × 斜率）。 */
    private final double[] segments;

    TechniqueStat(String tierKey, String valueKey, Stat stat, double... segments) {
        this.tierKey = tierKey;
        this.valueKey = valueKey;
        this.stat = stat;
        this.segments = segments;
    }

    public String tierKey() {
        return tierKey;
    }

    public String valueKey() {
        return valueKey;
    }

    public Stat stat() {
        return stat;
    }

    /** 档位系数：最下档 0.6、下档 0.8、中档 1、上档 1.2、最上档 1.4，其他文字为 0。 */
    public static double tierFactor(String tier) {
        return switch (tier) {
            case "最下档" -> 0.6;
            case "下档" -> 0.8;
            case "中档" -> 1;
            case "上档" -> 1.2;
            case "最上档" -> 1.4;
            default -> 0;
        };
    }

    /** 该项在给定层数与档位下的数值，运算次序与来源一致。 */
    public double value(double level, String tier) {
        double base;
        if (segments.length == 2) {
            base = segments[0];
            base = base + level * segments[1];
        } else if (level >= 31) {
            base = segments[6];
            base = base + (level - 30) * segments[7];
        } else if (level >= 21) {
            base = segments[4];
            base = base + (level - 20) * segments[5];
        } else if (level >= 11) {
            base = segments[2];
            base = base + (level - 10) * segments[3];
        } else {
            base = segments[0];
            base = base + level * segments[1];
        }
        double scaled = base * tierFactor(tier);
        return this == MOVE_SPEED ? scaled : Math.ceil(scaled);
    }
}
