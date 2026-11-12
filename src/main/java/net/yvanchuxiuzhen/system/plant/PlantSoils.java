package net.yvanchuxiuzhen.system.plant;

import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.block.TraitedBlock;

/** 播种过程使用的1.18.2泥土与草材质：原版成员显式对应，本模组读取来源内容表材质。 */
final class PlantSoils {
    private static final Set<Block> DIRT_OR_GRASS = Set.of(
            Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.PODZOL, Blocks.FARMLAND,
            Blocks.SOUL_SOIL, Blocks.DIRT_PATH, Blocks.ROOTED_DIRT,
            Blocks.GRASS_BLOCK, Blocks.MYCELIUM, Blocks.HAY_BLOCK, Blocks.NETHER_WART_BLOCK,
            Blocks.DRIED_KELP_BLOCK, Blocks.WARPED_WART_BLOCK, Blocks.SHROOMLIGHT, Blocks.TARGET);
    private static final Set<ResourceLocation> MOD_DIRT = Set.of(
            id("brokenbonepile"), id("grassblock"), id("loess"), id("rottingsoil"),
            id("scorchedsoil"), id("shicai_008plant_01"), id("swampsilt"), id("youbosoil"));

    private PlantSoils() {
    }

    static boolean dirtOrGrass(BlockState soil) {
        if (soil.getBlock() instanceof TraitedBlock content) {
            String material = content.traits().row().identity().material();
            return material.equals("DIRT") || material.equals("GRASS");
        }
        return DIRT_OR_GRASS.contains(soil.getBlock())
                || MOD_DIRT.contains(BuiltInRegistries.BLOCK.getKey(soil.getBlock()));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
