package net.yvanchuxiuzhen.system.plant;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.yvanchuxiuzhen.platform.ModId;

/** 009花椒树与010八角树的来源身份、两个独立模板和生长后补写树干高度。 */
enum FruitTree {
    SHICAI_009("shicai_00902", "shicai00908", "shicai00907", 1),
    SHICAI_010("shicai_01001", "shicai01005", "shicai01004", 3);

    private final String log;
    private final String growthTemplate;
    private final String naturalTemplate;
    private final int rootHeight;

    FruitTree(String log, String growthTemplate, String naturalTemplate, int rootHeight) {
        this.log = log;
        this.growthTemplate = growthTemplate;
        this.naturalTemplate = naturalTemplate;
        this.rootHeight = rootHeight;
    }

    Block log() {
        return BuiltInRegistries.BLOCK.get(id(log));
    }

    ResourceLocation growthTemplate() {
        return id(growthTemplate);
    }

    ResourceLocation naturalTemplate() {
        return id(naturalTemplate);
    }

    int rootHeight() {
        return rootHeight;
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
