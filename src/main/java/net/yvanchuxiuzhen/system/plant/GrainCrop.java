package net.yvanchuxiuzhen.system.plant;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.yvanchuxiuzhen.platform.ModId;

/** 四种可重复采摘食材的来源编号；各阶段身份和物理属性仍取自独立内容行。 */
enum GrainCrop {
    SHICAI_007("007"),
    SHICAI_025("025"),
    SHICAI_026("026"),
    SHICAI_027("027");

    private final String number;

    GrainCrop(String number) {
        this.number = number;
    }

    Block block(int stage) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE,
                "shicai_" + number + "_plant_0" + stage));
    }

    int nextStage(int stage) {
        return switch (stage) {
            case 1 -> 2;
            case 2 -> this == SHICAI_025 ? 3 : 5;
            case 5 -> this == SHICAI_025 ? 6 : 3;
            default -> 4;
        };
    }

    ItemStack seed() {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE,
                "shicai_" + number + "01")));
    }

    ItemStack produce() {
        return new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE,
                "shicai_" + number)));
    }

    ItemStack drop(boolean mature) {
        if (!mature || this == SHICAI_007) return seed();
        return produce().copyWithCount(2);
    }
}
