package net.yvanchuxiuzhen.system.attribute.api;

/**
 * 贡献所在的层，对应 1.18.2 版本基础字段与 {@code _display} 字段的区分。
 * 战斗中有几处只读基础值（如法术伤害读攻击方 {@code mana_Att}），因此两层分开汇总。
 */
public enum Layer {
    /** 计入基础值与最终值：修炼成长、单项提升、丹药与重置等写来源基础字段的贡献。 */
    BASE("base"),
    /** 只计入最终值：防具与武器的炼器数值、饰品、主修功法加成、附魔。 */
    BONUS("bonus");

    private final String id;

    Layer(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }
}
