package net.yvanchuxiuzhen.system.plant;

import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentFlower;

/** 浮萍沿来源水生植物规则，也可生存于普通花草基底。 */
final class DuckweedBlock extends ContentFlower {
    private static final Set<ResourceLocation> SOURCE_WATER_MATERIAL = Set.of(
            id("hotoil"), id("quicksand"), id("watercloud"), id("wenqvan"), id("xveliu"));

    DuckweedBlock(BlockTraits traits) {
        super(traits);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        return below.is(Blocks.WATER) || SOURCE_WATER_MATERIAL.contains(BuiltInRegistries.BLOCK.getKey(below.getBlock()))
                || super.canSurvive(state, level, pos);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
