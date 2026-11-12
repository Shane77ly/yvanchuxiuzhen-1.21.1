package net.yvanchuxiuzhen.system.plant;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.yvanchuxiuzhen.platform.ModId;

/** 棉花与麻的来源编号、阶段数及产物映射。 */
public enum FiberCrop {
    COTTON("mian", 5, "mianseed", "mianitem"),
    HEMP("ma", 4, "maseed", "maitem");

    private final String id;
    private final int stages;
    private final String seedId;
    private final String fiberId;

    FiberCrop(String id, int stages, String seedId, String fiberId) {
        this.id = id;
        this.stages = stages;
        this.seedId = seedId;
        this.fiberId = fiberId;
    }

    public Block block(int stage) {
        return BuiltInRegistries.BLOCK.get(location(id + "_plant_0" + stage));
    }

    public int stages() {
        return stages;
    }

    public static FiberCrop fromBlockId(String blockId) {
        if (blockId.startsWith("mian_plant_")) return COTTON;
        if (blockId.startsWith("ma_plant_")) return HEMP;
        throw new IllegalArgumentException("未知纤维作物方块：" + blockId);
    }

    public static FiberCrop fromSeedId(String itemId) {
        return switch (itemId) {
            case "mianseed" -> COTTON;
            case "maseed" -> HEMP;
            default -> throw new IllegalArgumentException("未知纤维种子物品：" + itemId);
        };
    }

    public int stageFromBlockId(String blockId) {
        String prefix = id + "_plant_";
        if (!blockId.startsWith(prefix)) {
            throw new IllegalArgumentException("方块不属于纤维作物 " + id + "：" + blockId);
        }
        return Integer.parseInt(blockId.substring(prefix.length()));
    }

    ItemStack drop(int stage) {
        return new ItemStack(item(stage == stages && this == HEMP ? fiberId : seedId));
    }

    ItemStack seed() {
        return new ItemStack(item(seedId));
    }

    ItemStack fiber() {
        return new ItemStack(item(fiberId));
    }

    private static Item item(String path) {
        return BuiltInRegistries.ITEM.get(location(path));
    }

    private static ResourceLocation location(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
