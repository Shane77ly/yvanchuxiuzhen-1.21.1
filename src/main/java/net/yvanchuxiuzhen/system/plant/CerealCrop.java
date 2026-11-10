package net.yvanchuxiuzhen.system.plant;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.yvanchuxiuzhen.platform.ModId;

/** 两种已核同规则谷物的来源编号；阶段属性和表现分别由各自内容表保存。 */
enum CerealCrop {
    SHICAI_001("001"),
    SHICAI_002("002");

    private final String number;

    CerealCrop(String number) {
        this.number = number;
    }

    Block block(int stage) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE,
                "shicai_" + number + "_plant_0" + stage));
    }

    Item seed() {
        return BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE,
                "shicai_" + number + "seed"));
    }
}
