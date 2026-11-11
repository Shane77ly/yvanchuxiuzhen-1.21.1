package net.yvanchuxiuzhen.system.plant;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.yvanchuxiuzhen.platform.ModId;

/** 046-056豆科作物的来源编号、阶段分支、掉落与采摘结果。 */
public enum LegumeCrop {
    SHICAI_046("046", 12, 5, 4, 5, 3, 46,
            new int[] {0, 2, 3, 5, 0, 4}, new int[] {0, 4, 4, 4, 0, 4},
            new int[] {0, 1, 3, 1, 3, 1}),
    SHICAI_047("047", 4, 5, 4, 5, 3, 46,
            new int[] {0, 2, 3, 5, 0, 4}, new int[] {0, 4, 4, 4, 0, 4},
            new int[] {0, 1, 3, 1, 3, 1}),
    SHICAI_048("048", 4, 5, 4, 5, 3, 46,
            new int[] {0, 2, 3, 5, 0, 4}, new int[] {0, 4, 4, 4, 0, 4},
            new int[] {0, 1, 3, 1, 3, 1}),
    SHICAI_049("049", 4, 5, 4, 5, 3, 46,
            new int[] {0, 2, 3, 5, 0, 4}, new int[] {0, 4, 4, 4, 0, 4},
            new int[] {0, 1, 3, 1, 3, 1}),
    SHICAI_050("050", 0, 5, 4, 5, 3, 46,
            new int[] {0, 2, 3, 5, 0, 4}, new int[] {0, 4, 4, 4, 0, 4},
            new int[] {0, 1, 1, 1, 3, 1}),
    SHICAI_051("051", 0, 5, 4, 5, 3, 46,
            new int[] {0, 2, 3, 5, 0, 4}, new int[] {0, 4, 4, 4, 0, 4},
            new int[] {0, 1, 1, 1, 3, 1}),
    SHICAI_052("052", 0, 5, 5, 4, 3, 30,
            new int[] {0, 2, 3, 4, 5, 0}, new int[] {0, 5, 5, 5, 5, 0},
            new int[] {0, 1, 1, 1, 1, 4}),
    SHICAI_053("053", 0, 5, 5, 4, 3, 30,
            new int[] {0, 2, 3, 4, 5, 0}, new int[] {0, 5, 5, 5, 5, 0},
            new int[] {0, 1, 1, 1, 0, 3}),
    SHICAI_054("054", 0, 5, 4, 5, 3, 46,
            new int[] {0, 2, 5, 4, 0, 3}, new int[] {0, 4, 4, 4, 0, 4},
            new int[] {0, 1, 1, 1, 3, 1}),
    SHICAI_055("055", 0, 4, 0, 0, 0, 14,
            new int[] {0, 2, 3, 4, 0}, new int[] {0, 4, 4, 4, 0},
            new int[] {0, 1, 1, 1, 4}),
    SHICAI_056("056", 0, 5, 5, 4, 3, 30,
            new int[] {0, 2, 3, 4, 5, 0}, new int[] {0, 5, 5, 5, 5, 0},
            new int[] {0, 1, 1, 1, 1, 3});

    private final String number;
    private final int sproutDropStageMask;
    private final int stageCount;
    private final int harvestStage;
    private final int harvestedStage;
    private final int harvestEntityCount;
    private final int bonemealStageMask;
    private final int[] tickTargets;
    private final int[] bonemealTargets;
    private final int[] dropCounts;

    LegumeCrop(String number, int sproutDropStageMask, int stageCount, int harvestStage,
            int harvestedStage, int harvestEntityCount, int bonemealStageMask,
            int[] tickTargets, int[] bonemealTargets, int[] dropCounts) {
        this.number = number;
        this.sproutDropStageMask = sproutDropStageMask;
        this.stageCount = stageCount;
        this.harvestStage = harvestStage;
        this.harvestedStage = harvestedStage;
        this.harvestEntityCount = harvestEntityCount;
        this.bonemealStageMask = bonemealStageMask;
        this.tickTargets = tickTargets;
        this.bonemealTargets = bonemealTargets;
        this.dropCounts = dropCounts;
    }

    public String number() {
        return number;
    }

    public boolean supportCrop() {
        return this == SHICAI_052 || this == SHICAI_053;
    }

    public Block block(int stage) {
        return BuiltInRegistries.BLOCK.get(id("shicai_" + number + "_plant_0" + stage));
    }

    public static LegumeCrop fromBlockId(String blockId) {
        return valueOf("SHICAI_" + blockId.substring("shicai_".length(), blockId.indexOf("_plant_")));
    }

    public int stageFromBlockId(String blockId) {
        String prefix = "shicai_" + number + "_plant_";
        if (!blockId.startsWith(prefix)) {
            throw new IllegalArgumentException("Block ID does not belong to " + number + ": " + blockId);
        }
        return Integer.parseInt(blockId.substring(prefix.length()));
    }

    public int tickTarget(int stage) {
        return tickTargets[stage];
    }

    public boolean hasBonemeal(int stage) {
        return (bonemealStageMask & (1 << stage)) != 0;
    }

    public int bonemealTarget(int stage) {
        return bonemealTargets[stage];
    }

    public boolean harvests(int stage) {
        return stage == harvestStage;
    }

    public int harvestedStage() {
        return harvestedStage;
    }

    public int harvestEntityCount() {
        return harvestEntityCount;
    }

    public int stageCount() {
        return stageCount;
    }

    public ItemStack produce() {
        return new ItemStack(BuiltInRegistries.ITEM.get(id("shicai_" + number)));
    }

    ItemStack drop(int stage) {
        int count = dropCounts[stage];
        if (count == 0) return ItemStack.EMPTY;
        String item = "shicai_" + number + ((sproutDropStageMask & (1 << stage)) != 0 ? "01" : "");
        return new ItemStack(BuiltInRegistries.ITEM.get(id(item)), count);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
