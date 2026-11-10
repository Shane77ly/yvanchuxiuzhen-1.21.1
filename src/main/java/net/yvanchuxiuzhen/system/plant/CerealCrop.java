package net.yvanchuxiuzhen.system.plant;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.yvanchuxiuzhen.platform.ModId;

/** 五种已核八阶段谷物的来源编号、播种物及成熟产物；阶段属性由各自内容表保存。 */
enum CerealCrop {
    SHICAI_001("001", "shicai_001seed", "shicai_001seed", 6),
    SHICAI_002("002", "shicai_002seed", "shicai_002seed", 6),
    SHICAI_004("004", "shicai_004", "shicai_004", 6),
    SHICAI_005("005", "shicai_005", "shicai_005", 6),
    SHICAI_006("006", "shicai_006seed", "shicai_006", 2);

    private final String number;
    private final String plantingItem;
    private final String matureItem;
    private final int matureCount;

    CerealCrop(String number, String plantingItem, String matureItem, int matureCount) {
        this.number = number;
        this.plantingItem = plantingItem;
        this.matureItem = matureItem;
        this.matureCount = matureCount;
    }

    Block block(int stage) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE,
                "shicai_" + number + "_plant_0" + stage));
    }

    ItemStack drop(int stage) {
        if (stage == 8) return new ItemStack(item(matureItem), matureCount);
        if (this == SHICAI_004 && stage == 1) return new ItemStack(item("shicai_002seed"));
        return new ItemStack(item(plantingItem));
    }

    private static Item item(String path) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path));
    }
}
