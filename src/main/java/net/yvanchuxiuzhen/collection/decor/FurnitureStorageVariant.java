package net.yvanchuxiuzhen.collection.decor;

/** 储物家具保留八个方块实体身份，共用来源的三种库存布局。 */
public enum FurnitureStorageVariant {
    CABINET_13(13, Layout.LARGE),
    CABINET_14(14, Layout.SMALL),
    CABINET_18(18, Layout.MEDIUM),
    CABINET_19(19, Layout.SMALL),
    CABINET_20(20, Layout.LARGE),
    CABINET_21(21, Layout.SMALL),
    CABINET_22(22, Layout.SMALL),
    CABINET_24(24, Layout.SMALL);

    private final int number;
    private final Layout layout;

    FurnitureStorageVariant(int number, Layout layout) {
        this.number = number;
        this.layout = layout;
    }

    public String id() {
        return "beechfurniture_" + number;
    }

    public String factory() {
        return "decor:storage_" + number;
    }

    public Layout layout() {
        return layout;
    }

    /** 槽位坐标与背景大小取自各自来源菜单，不套用箱子界面尺寸。 */
    public enum Layout {
        LARGE(13, 54, 9, 123, 181, 204),
        SMALL(14, 27, 17, 84, 142, 166),
        MEDIUM(18, 36, 8, 84, 142, 166);

        private final int number;
        private final int slots;
        private final int storageY;
        private final int inventoryY;
        private final int hotbarY;
        private final int height;

        Layout(int number, int slots, int storageY, int inventoryY, int hotbarY, int height) {
            this.number = number;
            this.slots = slots;
            this.storageY = storageY;
            this.inventoryY = inventoryY;
            this.hotbarY = hotbarY;
            this.height = height;
        }

        public String menuId() { return "beechfurniture_" + number + "_gui"; }
        public int slots() { return slots; }
        public int storageY() { return storageY; }
        public int inventoryY() { return inventoryY; }
        public int hotbarY() { return hotbarY; }
        public int height() { return height; }
    }
}
