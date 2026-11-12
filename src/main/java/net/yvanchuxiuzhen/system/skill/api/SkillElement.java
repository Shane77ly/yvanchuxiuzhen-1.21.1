package net.yvanchuxiuzhen.system.skill.api;

/**
 * 技能冷却分组：金、水、木、火、土、阴阳各有一个玩家冷却计数。技能书用 {@code <前缀>colddown} 记下施放后给该组增加的秒数
 * （负数表示给该组减少），用 {@code <前缀>coldignore} 记下该组冷却不超过多少秒时仍可施放。
 */
public enum SkillElement {
    GOLD("Gold"),
    WATER("Water"),
    TREE("Tree"),
    FIRE("Fire"),
    SOIL("Soil"),
    YINYANG("yinyang");

    private final String prefix;

    SkillElement(String prefix) {
        this.prefix = prefix;
    }

    /** 书上记录施放增加冷却秒数的键。 */
    public String cooldownKey() {
        return prefix + "colddown";
    }

    /** 书上记录可施放的冷却上限秒数的键。 */
    public String ignoreKey() {
        return prefix + "coldignore";
    }
}
