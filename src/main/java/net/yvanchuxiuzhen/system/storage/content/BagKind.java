package net.yvanchuxiuzhen.system.storage.content;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.yvanchuxiuzhen.platform.ModId;

/** 四档乾坤袋的容量与来源槽位布局；大袋下方两侧的附加槽不占玩家背包区域。 */
public enum BagKind {
    SMALL("01", 27, 176, 166, 8, 84),
    MEDIUM("02", 54, 176, 208, 8, 125),
    LARGE("03", 108, 304, 202, 72, 122),
    HUGE("04", 220, 435, 244, 137, 163);

    private final String number;
    private final int size;
    private final int width;
    private final int height;
    private final int inventoryX;
    private final int inventoryY;

    BagKind(String number, int size, int width, int height, int inventoryX, int inventoryY) {
        this.number = number;
        this.size = size;
        this.width = width;
        this.height = height;
        this.inventoryX = inventoryX;
        this.inventoryY = inventoryY;
    }

    public String id() {
        return "qiankundai_" + number;
    }

    public String menuId() {
        return id() + "_gui";
    }

    public Item item() {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id()));
    }

    public int size() {
        return size;
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public int inventoryX() {
        return inventoryX;
    }

    public int inventoryY() {
        return inventoryY;
    }

    public int slotX(int slot) {
        return switch (this) {
            case SMALL, MEDIUM -> 7 + slot % 9 * 18;
            case LARGE -> slot < 96 ? 8 + slot % 16 * 18
                    : 8 + ((slot - 96) % 6 < 3 ? (slot - 96) % 6 : (slot - 96) % 6 + 10) * 18;
            case HUGE -> slot < 184 ? 11 + slot % 23 * 18
                    : 11 + ((slot - 184) % 12 < 6 ? (slot - 184) % 12 : (slot - 184) % 12 + 11) * 18;
        };
    }

    public int slotY(int slot) {
        return switch (this) {
            case SMALL -> 17 + slot / 9 * 18;
            case MEDIUM -> 11 + slot / 9 * 18;
            case LARGE -> slot < 96 ? 8 + slot / 16 * 18 : 116 + (slot - 96) / 6 * 18;
            case HUGE -> slot < 184 ? 11 + slot / 23 * 18 : 155 + (slot - 184) / 12 * 18;
        };
    }
}
