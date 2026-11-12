package net.yvanchuxiuzhen.system.plant;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.yvanchuxiuzhen.platform.ModId;

/** 霓裳草来源标识与六个生长阶段、一个成熟植株阶段的映射。 */
public final class NichangGrassCrop {
    public static final NichangGrassCrop INSTANCE = new NichangGrassCrop();

    private NichangGrassCrop() {
    }

    public Block block(int stage) {
        return BuiltInRegistries.BLOCK.get(id("nichanggrass_0" + stage));
    }

    ItemStack seedDrop() {
        return new ItemStack(BuiltInRegistries.ITEM.get(id("nichanggrassseed")));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
