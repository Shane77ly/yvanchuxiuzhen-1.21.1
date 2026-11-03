package net.yvanchuxiuzhen.system.attribute.api;

/**
 * 参与五行加成的伤害属性，对应 1.18.2 版本受击方 NBT {@code damagetype} 的取值。
 * 光、雷借用火，暗借用土，冰借用水，风借用木；阴、阳读阴阳灵根且没有功法项。
 */
public enum DamageElement {
    GOLD(Stat.ROOT_GOLD, Stat.GONGFA_GOLD),
    WATER(Stat.ROOT_WATER, Stat.GONGFA_WATER),
    TREE(Stat.ROOT_TREE, Stat.GONGFA_TREE),
    FIRE(Stat.ROOT_FIRE, Stat.GONGFA_FIRE),
    SOIL(Stat.ROOT_SOIL, Stat.GONGFA_SOIL),
    LIGHT(Stat.ROOT_FIRE, Stat.GONGFA_FIRE),
    DARK(Stat.ROOT_SOIL, Stat.GONGFA_SOIL),
    THUNDER(Stat.ROOT_FIRE, Stat.GONGFA_FIRE),
    ICE(Stat.ROOT_WATER, Stat.GONGFA_WATER),
    WIND(Stat.ROOT_TREE, Stat.GONGFA_TREE),
    YIN(Stat.YINYANG, null),
    YANG(Stat.YINYANG, null);

    private final Stat root;
    private final Stat gongfa;

    DamageElement(Stat root, Stat gongfa) {
        this.root = root;
        this.gongfa = gongfa;
    }

    /** 攻击方读取的灵根项。 */
    public Stat root() {
        return root;
    }

    /** 受击方读取的功法五行项；阴、阳为 null。 */
    public Stat gongfa() {
        return gongfa;
    }
}
