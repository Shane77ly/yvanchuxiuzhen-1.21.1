package net.yvanchuxiuzhen.system.plant;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.yvanchuxiuzhen.platform.ModId;

/** 011–016号四阶段蔬菜的来源身份和Java固定掉落；成熟自定义loot仍由资源承担。 */
enum VegetableCrop {
    SHICAI_011("011", "shicai_01101"),
    SHICAI_012("012", "shicai_01201"),
    SHICAI_013("013", "shicai_01301"),
    SHICAI_014("014", "shicai_01401"),
    SHICAI_015("015", "shicai_01502"),
    SHICAI_016("016", "shicai_01601");

    private final String number;
    private final String plantingItem;

    VegetableCrop(String number, String plantingItem) {
        this.number = number;
        this.plantingItem = plantingItem;
    }

    Block block(int stage) {
        return BuiltInRegistries.BLOCK.get(id("shicai_" + number + "_plant_0" + stage));
    }

    ItemStack fixedDrop(int stage) {
        if (this == SHICAI_015) {
            return switch (stage) {
                case 2 -> item("shicai_015").copyWithCount(2);
                case 3 -> item("shicai_01501").copyWithCount(2);
                case 4 -> item("shicai_01502").copyWithCount(4);
                default -> item(plantingItem);
            };
        }
        return item(stage == 4 ? "shicai_" + number : plantingItem);
    }

    static ItemStack item(String path) {
        return new ItemStack(BuiltInRegistries.ITEM.get(id(path)));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
