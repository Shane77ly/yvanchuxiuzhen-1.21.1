package net.yvanchuxiuzhen.system.xiulian.api;

/** 修炼系统的文本项，初始值为 1.18.2 版本 PlayerVariables 对应字段的初值；注释为来源字段名。 */
public enum CultivationText {
    /** {@code LEVELname}，境界名。 */
    LEVEL_NAME("level_name", "凡人"),
    /** {@code Race} */
    RACE("race", "人族"),
    /** {@code Race_name} */
    RACE_NAME("race_name", "人族"),
    /** {@code blood_lineage} */
    BLOOD_LINEAGE("blood_lineage", ""),
    /** {@code constitution}，体质。 */
    CONSTITUTION("constitution", ""),
    /** {@code birth}，出身。 */
    BIRTH("birth", "流浪侠客"),
    /** {@code fuhuo} */
    REVIVE("revive", "");

    private final String id;
    private final String initial;

    CultivationText(String id, String initial) {
        this.id = id;
        this.initial = initial;
    }

    public String id() {
        return id;
    }

    public String initial() {
        return initial;
    }
}
