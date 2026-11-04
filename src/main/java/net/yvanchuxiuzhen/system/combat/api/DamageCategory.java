package net.yvanchuxiuzhen.system.combat.api;

import java.util.List;
import net.yvanchuxiuzhen.system.attribute.api.DamageElement;

/**
 * 灵力与神识伤害的类别，名称是 {@code /shanghai} 第一个参数（来源的中文字符串）。每个类别决定攻击方读取的灵根与体质键、
 * 受击方的五行相克标签、手持武器附魔的加成、护甲附魔的防御项。无法识别的名称与“无”一样没有任何属性项。
 */
public enum DamageCategory {
    GOLD("金", DamageElement.GOLD, "Gold", Family.GOLD, List.of("shuxingatt_01"), 2, List.of(new Term("golddef", 2)), List.of()),
    WATER("水", DamageElement.WATER, "Water", Family.WATER, List.of("shuxingatt_02"), 2, List.of(new Term("waterdef", 2)), List.of()),
    TREE("木", DamageElement.TREE, "Tree", Family.TREE, List.of("shuxingatt_03"), 2, List.of(new Term("treedef", 2)), List.of()),
    FIRE("火", DamageElement.FIRE, "Fire", Family.FIRE, List.of("shuxingatt_04"), 2, List.of(new Term("firedef", 2)), List.of()),
    SOIL("土", DamageElement.SOIL, "Soil", Family.SOIL, List.of("shuxingatt_05"), 2, List.of(new Term("soildef", 2)), List.of()),
    LIGHT("光", DamageElement.LIGHT, "Light", Family.FIRE, List.of("shuxingatt_04", "shuxingatt_06"), 2,
            List.of(new Term("firedef", 1)), List.of("lightdef")),
    DARK("暗", DamageElement.DARK, "Dark", Family.SOIL, List.of("shuxingatt_05", "shuxingatt_07"), 2, List.of(new Term("soildef", 1)), List.of()),
    THUNDER("雷", DamageElement.THUNDER, "Thunder", Family.FIRE, List.of("shuxingatt_04", "shuxingatt_08"), 2,
            List.of(new Term("firedef", 1)), List.of("thunderdef")),
    ICE("冰", DamageElement.ICE, "Ice", Family.WATER, List.of("shuxingatt_02", "shuxingatt_09"), 2,
            List.of(new Term("waterdef", 1)), List.of("icedef")),
    WIND("风", DamageElement.WIND, "Wind", Family.TREE, List.of("shuxingatt_03", "shuxingatt_10"), 2,
            List.of(new Term("treedef", 1)), List.of("winddef")),
    YIN("阴", DamageElement.YIN, "Yin", null, List.of("shuxingatt_11"), 4, List.of(), List.of()),
    YANG("阳", DamageElement.YANG, "Yang", null, List.of("shuxingatt_12"), 4, List.of(), List.of()),
    SOUND("音波", null, null, null, List.of(), 2, List.of(new Term("souldef", 2), new Term("sounddef", 2)), List.of()),
    SOUL("灵魂", null, null, null, List.of(), 2, List.of(new Term("souldef", 2)), List.of()),
    NONE("无", null, null, null, List.of(), 2, List.of(), List.of());

    /** 五行相克所用的受击者标签族：金水木火土。 */
    public enum Family {
        GOLD, WATER, TREE, FIRE, SOIL
    }

    /** 防御项：护甲附魔等级之和乘以系数。 */
    public record Term(String enchantment, int factor) {
    }

    private final String label;
    private final DamageElement element;
    private final String tizhiPrefix;
    private final Family family;
    private final List<String> attackEnchantments;
    private final int attackExponent;
    private final List<Term> defenseSums;
    private final List<String> defenseSquares;

    DamageCategory(String label, DamageElement element, String tizhiPrefix, Family family, List<String> attackEnchantments,
            int attackExponent, List<Term> defenseSums, List<String> defenseSquares) {
        this.label = label;
        this.element = element;
        this.tizhiPrefix = tizhiPrefix;
        this.family = family;
        this.attackEnchantments = attackEnchantments;
        this.attackExponent = attackExponent;
        this.defenseSums = defenseSums;
        this.defenseSquares = defenseSquares;
    }

    public static DamageCategory byLabel(String label) {
        for (DamageCategory category : values()) {
            if (category.label.equals(label)) {
                return category;
            }
        }
        return NONE;
    }

    public String label() {
        return label;
    }

    /** 音波与灵魂走神识攻防，其余走法术攻防。 */
    public boolean isSoul() {
        return this == SOUND || this == SOUL;
    }

    /** 参与五行加成的元素；音波、灵魂、无为 null。 */
    public DamageElement element() {
        return element;
    }

    /** 体质键的类别前缀（{@code <前缀>AttCorrect}、{@code <前缀>AttCorrectPercentage}），音波、灵魂、无为 null。 */
    public String tizhiPrefix() {
        return tizhiPrefix;
    }

    public Family family() {
        return family;
    }

    /** 手持武器上对本类别加成的附魔，等级的 {@link #attackExponent()} 次方加到攻击量上。 */
    public List<String> attackEnchantments() {
        return attackEnchantments;
    }

    public int attackExponent() {
        return attackExponent;
    }

    /** 四件护甲上等级之和乘系数加到防御上的附魔。 */
    public List<Term> defenseSums() {
        return defenseSums;
    }

    /** 四件护甲上每件等级的平方加到防御上的附魔。 */
    public List<String> defenseSquares() {
        return defenseSquares;
    }
}
