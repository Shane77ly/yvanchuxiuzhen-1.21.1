package net.yvanchuxiuzhen.system.plant;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.yvanchuxiuzhen.platform.ModId;

/** 040–045 菌类的来源编号、阶段、贴附方式与逐阶段掉落。 */
public enum FungusCrop {
    SHICAI_040("040", 4, true, 1, 2, 3, 5),
    SHICAI_041("041", 4, true, 1, 2, 3, 5),
    SHICAI_042("042", 5, false, 1, 2, 3, 4, 5),
    SHICAI_043("043", 4, false, 1, 1, 1, 3),
    SHICAI_044("044", 4, false, 1, 1, 1, 3),
    SHICAI_045("045", 4, false, 1, 1, 1, 3);

    private final String number;
    private final int lastStage;
    private final boolean attached;
    private final int[] stageDrops;

    FungusCrop(String number, int lastStage, boolean attached, int... stageDrops) {
        this.number = number;
        this.lastStage = lastStage;
        this.attached = attached;
        this.stageDrops = stageDrops;
    }

    public Block block(int stage) {
        return BuiltInRegistries.BLOCK.get(id("shicai_" + number + "_plant_0" + stage));
    }

    public int lastStage() {
        return lastStage;
    }

    public boolean attached() {
        return attached;
    }

    ItemStack drop(int stage) {
        return new ItemStack(item(), stageDrops[stage - 1]);
    }

    private Item item() {
        return BuiltInRegistries.ITEM.get(id("shicai_" + number));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
