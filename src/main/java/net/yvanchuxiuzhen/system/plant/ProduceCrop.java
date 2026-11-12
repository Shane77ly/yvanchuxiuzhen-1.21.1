package net.yvanchuxiuzhen.system.plant;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.yvanchuxiuzhen.platform.ModId;

/** 四阶段普通作物的来源编号、播种物与成熟产量；024第三阶段另产三颗种子。 */
public enum ProduceCrop {
    SHICAI_017("017", "shicai_01701", 2),
    SHICAI_018("018", "shicai_01801", 4),
    SHICAI_019("019", "shicai_01901", 4),
    SHICAI_020("020", "shicai_02001", 1),
    SHICAI_021("021", "shicai_02101", 4),
    SHICAI_022("022", "shicai_02201", 1),
    SHICAI_023("023", "shicai_02301", 1),
    SHICAI_024("024", "shicai_02401", 1),
    SHICAI_028("028", "shicai_028", 3),
    SHICAI_029("029", "shicai_02901", 3),
    SHICAI_030("030", "shicai_03001", 6),
    SHICAI_031("031", "shicai_03101", 3),
    SHICAI_032("032", "shicai_032", 4),
    SHICAI_033("033", "shicai_033", 3);

    private final String number;
    private final String plantingItem;
    private final int matureCount;

    ProduceCrop(String number, String plantingItem, int matureCount) {
        this.number = number;
        this.plantingItem = plantingItem;
        this.matureCount = matureCount;
    }

    Block block(int stage) {
        return BuiltInRegistries.BLOCK.get(id("shicai_" + number + "_plant_0" + stage));
    }

    ItemStack drop(int stage) {
        if (stage == 4) return new ItemStack(item("shicai_" + number), matureCount);
        return new ItemStack(item(plantingItem), this == SHICAI_024 && stage == 3 ? 3 : 1);
    }

    private static Item item(String path) {
        return BuiltInRegistries.ITEM.get(id(path));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
