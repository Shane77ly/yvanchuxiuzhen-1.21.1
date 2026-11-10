package net.yvanchuxiuzhen.system.plant;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.yvanchuxiuzhen.platform.ModId;

/** 蔓生作物保留来源编号与阶段范围，035的首阶段从02开始。 */
public enum VineCrop {
    SHICAI_034("034", 1, 5),
    SHICAI_035("035", 2, 4),
    SHICAI_036("036", 1, 5),
    SHICAI_037("037", 1, 3),
    SHICAI_038("038", 1, 3),
    SHICAI_039("039", 1, 4);

    private final String number;
    private final int firstStage;
    private final int lastStage;

    VineCrop(String number, int firstStage, int lastStage) {
        this.number = number;
        this.firstStage = firstStage;
        this.lastStage = lastStage;
    }

    public Block block(int stage) {
        return BuiltInRegistries.BLOCK.get(resource("shicai_" + number + "_plant_0" + stage));
    }

    public int firstStage() {
        return firstStage;
    }

    ItemStack drop(int stage) {
        String id = stage == firstStage ? "shicai_" + number + "01"
                : stage == lastStage ? "shicai_" + number : "minecraft:vine";
        return new ItemStack(BuiltInRegistries.ITEM.get(id.contains(":") ? ResourceLocation.parse(id) : resource(id)));
    }

    ItemStack produce() {
        return new ItemStack(BuiltInRegistries.ITEM.get(resource("shicai_" + number)));
    }

    boolean hasTick(int stage) {
        return switch (this) {
            case SHICAI_034, SHICAI_036 -> stage < 5;
            case SHICAI_035 -> stage < 4;
            case SHICAI_037, SHICAI_038 -> stage < 3;
            case SHICAI_039 -> stage < 4;
        };
    }

    private static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
