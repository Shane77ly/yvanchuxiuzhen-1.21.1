package net.yvanchuxiuzhen.collection.decor;

/** 都承盘、衣架和紫砂壶分别保存来源的槽数、堆叠上限和方向取出规则。 */
public enum UtilityFurnitureVariant {
    TRAY("beechfurniture_23", 1, 64),
    RACK("beechfurniture_29", 3, 64),
    TEAPOT("zisha_01", 4, 1);

    private final String id;
    private final int slots;
    private final int stackLimit;

    UtilityFurnitureVariant(String id, int slots, int stackLimit) {
        this.id = id;
        this.slots = slots;
        this.stackLimit = stackLimit;
    }

    public String id() { return id; }
    public String factory() { return "decor:utility_" + id; }
    public int slots() { return slots; }
    public int stackLimit() { return stackLimit; }
}
