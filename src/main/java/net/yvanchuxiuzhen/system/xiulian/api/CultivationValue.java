package net.yvanchuxiuzhen.system.xiulian.api;

/**
 * 修炼系统的数值项，初始值为 1.18.2 版本 PlayerVariables 对应字段的初值；注释为来源字段名。
 * 灵根与阴阳在变化后作为基础层贡献提交给 attribute（五行系数由 attribute 计算）。
 */
public enum CultivationValue {
    /** {@code LEVELnumber}，境界层数。 */
    LEVEL_NUMBER("level_number", 0),
    /** {@code experience_Max}，突破所需修为。 */
    EXPERIENCE_MAX("experience_max", 100),
    /** {@code experience}，已有修为。 */
    EXPERIENCE("experience", 0),
    /** {@code experience_Stand}，待吸收修为。 */
    EXPERIENCE_STAND("experience_stand", 0),
    /** {@code experience_rate} */
    EXPERIENCE_RATE("experience_rate", 1),
    /** {@code experience_hinder} */
    EXPERIENCE_HINDER("experience_hinder", 1),
    /** {@code power}，当前灵力。 */
    POWER("power", 20),
    /** {@code power_display}，每秒恢复前复制的面板灵力。 */
    POWER_DISPLAY("power_display", 0),
    /** {@code soul}，当前神识（精力）。 */
    SOUL("soul", 20),
    /** {@code soul_display}，每秒恢复前复制的面板精力。 */
    SOUL_DISPLAY("soul_display", 0),
    /** {@code skill_points} */
    SKILL_POINTS("skill_points", 0),
    /** {@code body_Gold} */
    ROOT_GOLD("root_gold", 0),
    /** {@code body_Water} */
    ROOT_WATER("root_water", 0),
    /** {@code body_Tree} */
    ROOT_TREE("root_tree", 0),
    /** {@code body_Fire} */
    ROOT_FIRE("root_fire", 0),
    /** {@code body_Soil} */
    ROOT_SOIL("root_soil", 0),
    /** {@code yinyang}，小于等于 -1 为阴，大于等于 1 为阳。 */
    YINYANG("yinyang", 0),
    /** {@code vigorous}，大于 0 为罡气，小于 0 为煞气。 */
    VIGOROUS("vigorous", 0),
    /** {@code merits}，大于 0 为功德，小于 0 为业力。 */
    MERITS("merits", 0),
    /** {@code thunder_Max} */
    THUNDER_MAX("thunder_max", 0),
    /** {@code thunder} */
    THUNDER("thunder", 0),
    /** {@code thunder_Att} */
    THUNDER_ATT("thunder_att", 0),
    /** {@code blood_lineage_LEVEL} */
    BLOOD_LINEAGE_LEVEL("blood_lineage_level", 0),
    /** {@code blood_lineage_Max} */
    BLOOD_LINEAGE_MAX("blood_lineage_max", 100),
    /** {@code blood_lineage_num} */
    BLOOD_LINEAGE_NUM("blood_lineage_num", 0),
    /** {@code skill_points_of_health_Max} */
    SKILL_POINTS_HEALTH_MAX("skill_points_health_max", 0),
    /** {@code skill_points_of_power_Max} */
    SKILL_POINTS_POWER_MAX("skill_points_power_max", 0),
    /** {@code skill_points_of_soul_Max} */
    SKILL_POINTS_SOUL_MAX("skill_points_soul_max", 0),
    /** {@code skill_points_of_attack} */
    SKILL_POINTS_ATTACK("skill_points_attack", 0),
    /** {@code skill_points_of_mana} */
    SKILL_POINTS_MANA("skill_points_mana", 0),
    /** {@code skill_points_of_soul} */
    SKILL_POINTS_SOUL("skill_points_soul", 0),
    /** {@code lifetime}，寿元（天）。 */
    LIFETIME("lifetime", 25550),
    /** {@code liftlock} */
    LIFTLOCK("liftlock", 1),
    /** {@code randomlift} */
    RANDOMLIFT("randomlift", 10),
    /** {@code kaishixiulian}，闭关剩余天数。 */
    CULTIVATION_DAYS("cultivation_days", 0),
    /** {@code xiuliancolddown} */
    CULTIVATION_COOLDOWN("cultivation_cooldown", 0);

    private final String id;
    private final double initial;

    CultivationValue(String id, double initial) {
        this.id = id;
        this.initial = initial;
    }

    public String id() {
        return id;
    }

    public double initial() {
        return initial;
    }
}
