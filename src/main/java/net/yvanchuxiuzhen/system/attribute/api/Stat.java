package net.yvanchuxiuzhen.system.attribute.api;

/**
 * 玩家属性项。初始值是 1.18.2 版本 PlayerVariables 对应基础字段的初值；移速取原版玩家的 0.1
 * （来源 {@code walkspeed} = 0.1 + 主修功法移速）。注释中为对应的来源字段。
 */
public enum Stat {
    /** {@code health_Max}，写入原版最大生命。 */
    HEALTH_MAX("health_max", 20),
    /** {@code health_Rec} */
    HEALTH_REC("health_rec", 0),
    /** {@code power_Max}，灵力上限。 */
    POWER_MAX("power_max", 20),
    /** {@code power_Rec} */
    POWER_REC("power_rec", 0),
    /** {@code soul_Max}，神识上限。 */
    SOUL_MAX("soul_max", 20),
    /** {@code soul_Rec} */
    SOUL_REC("soul_rec", 0),
    /** {@code attack_Def}，物理防御。 */
    ATTACK_DEF("attack_def", 0),
    /** {@code mana_Def}，法术防御。 */
    MANA_DEF("mana_def", 0),
    /** {@code soul_Def}，神识防御。 */
    SOUL_DEF("soul_def", 0),
    /** {@code attack_Num}，物理攻击，写入原版攻击伤害。 */
    ATTACK("attack", 1),
    /** {@code mana_Att}，法术攻击。 */
    MANA_ATT("mana_att", 1),
    /** {@code soul_Att}，神识攻击。 */
    SOUL_ATT("soul_att", 1),
    /** {@code Cooling_reduction}，冷却缩减比例。 */
    COOLDOWN_REDUCTION("cooldown_reduction", 0),
    /** {@code walkspeed}，写入原版移动速度。 */
    MOVE_SPEED("move_speed", 0.1),
    /** 灵根 {@code body_Gold}。 */
    ROOT_GOLD("root_gold", 0),
    /** 灵根 {@code body_Water}。 */
    ROOT_WATER("root_water", 0),
    /** 灵根 {@code body_Tree}。 */
    ROOT_TREE("root_tree", 0),
    /** 灵根 {@code body_Fire}。 */
    ROOT_FIRE("root_fire", 0),
    /** 灵根 {@code body_Soil}。 */
    ROOT_SOIL("root_soil", 0),
    /** 阴阳灵根 {@code yinyang}。 */
    YINYANG("yinyang", 0),
    /** 主修功法 {@code ExercisesGold}。 */
    GONGFA_GOLD("gongfa_gold", 0),
    /** 主修功法 {@code ExercisesWater}。 */
    GONGFA_WATER("gongfa_water", 0),
    /** 主修功法 {@code ExercisesTree}。 */
    GONGFA_TREE("gongfa_tree", 0),
    /** 主修功法 {@code ExercisesFire}。 */
    GONGFA_FIRE("gongfa_fire", 0),
    /** 主修功法 {@code ExercisesSoil}。 */
    GONGFA_SOIL("gongfa_soil", 0),
    /** 主修功法层数 {@code ExercisesLevel}。 */
    GONGFA_LEVEL("gongfa_level", 0);

    private final String id;
    private final double initial;

    Stat(String id, double initial) {
        this.id = id;
        this.initial = initial;
    }

    /** 存档与网络中使用的名称。 */
    public String id() {
        return id;
    }

    public double initial() {
        return initial;
    }
}
