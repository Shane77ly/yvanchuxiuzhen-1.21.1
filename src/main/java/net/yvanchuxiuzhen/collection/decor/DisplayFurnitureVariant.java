package net.yvanchuxiuzhen.collection.decor;

/** 展示柜与五种博古架保留各自的方块实体身份及来源槽数。 */
public enum DisplayFurnitureVariant {
    CABINET("03", 3),
    STAND_1("1501", 1),
    STAND_2("1502", 1),
    STAND_3("1503", 1),
    STAND_4("1504", 1),
    STAND_5("1505", 1);

    private final String suffix;
    private final int slots;

    DisplayFurnitureVariant(String suffix, int slots) { this.suffix = suffix; this.slots = slots; }
    public String id() { return "beechfurniture_" + suffix; }
    public String factory() { return "decor:display_" + suffix; }
    public int slots() { return slots; }
    public boolean cabinet() { return this == CABINET; }
    public DisplayFurnitureVariant next() { return this == STAND_5 ? STAND_1 : values()[ordinal() + 1]; }
}
