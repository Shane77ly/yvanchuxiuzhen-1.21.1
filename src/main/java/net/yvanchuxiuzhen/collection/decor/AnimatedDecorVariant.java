package net.yvanchuxiuzhen.collection.decor;

/** 签筒和装饰梯子各自的来源身份、库存与三维资源。 */
public enum AnimatedDecorVariant {
    FORTUNE("zhuangshiqiantong", "fortune_container", "zhuangshi_qiantong", 0, 1),
    LADDER("zhuangshitizi", "relocation_ladder", "zhuangshi_tizi", 9, 64);

    private final String id;
    private final String factory;
    private final String asset;
    private final int slots;
    private final int stackLimit;

    AnimatedDecorVariant(String id, String factory, String asset, int slots, int stackLimit) {
        this.id = id;
        this.factory = factory;
        this.asset = asset;
        this.slots = slots;
        this.stackLimit = stackLimit;
    }

    public String id() { return id; }
    public String factory() { return "decor:" + factory; }
    public String asset() { return asset; }
    public int slots() { return slots; }
    public int stackLimit() { return stackLimit; }
}
