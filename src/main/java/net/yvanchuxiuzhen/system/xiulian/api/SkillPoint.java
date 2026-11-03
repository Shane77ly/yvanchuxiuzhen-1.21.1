package net.yvanchuxiuzhen.system.xiulian.api;

import net.yvanchuxiuzhen.system.attribute.api.Stat;

/**
 * 属性点可分配的六项（来源面板第 2 页的 {@code HealthMaxUp}、{@code PowerMaxUp}、{@code SoulMaxUp}、{@code AttackUp}、
 * {@code ManaUp}、{@code SoulUp}）：成长的属性、每点的基础增量、体质修正键（{@code tizhiitem} 中加到每点上的值）与已分配点数。
 */
public enum SkillPoint {
    HEALTH_MAX(Stat.HEALTH_MAX, 4, "HealthAddSkillCorrect", CultivationValue.SKILL_POINTS_HEALTH_MAX),
    POWER_MAX(Stat.POWER_MAX, 40, "PowerAddSkillCorrect", CultivationValue.SKILL_POINTS_POWER_MAX),
    SOUL_MAX(Stat.SOUL_MAX, 40, "JingliAddSkillCorrect", CultivationValue.SKILL_POINTS_SOUL_MAX),
    ATTACK(Stat.ATTACK, 1, "AttackAddSkillCorrect", CultivationValue.SKILL_POINTS_ATTACK),
    MANA(Stat.MANA_ATT, 1, "ManaattAddSkillCorrect", CultivationValue.SKILL_POINTS_MANA),
    SOUL(Stat.SOUL_ATT, 1, "SoulattAddSkillCorrect", CultivationValue.SKILL_POINTS_SOUL);

    private final Stat stat;
    private final double amount;
    private final String correctionKey;
    private final CultivationValue spent;

    SkillPoint(Stat stat, double amount, String correctionKey, CultivationValue spent) {
        this.stat = stat;
        this.amount = amount;
        this.correctionKey = correctionKey;
        this.spent = spent;
    }

    public Stat stat() {
        return stat;
    }

    public double amount() {
        return amount;
    }

    public String correctionKey() {
        return correctionKey;
    }

    public CultivationValue spent() {
        return spent;
    }
}
