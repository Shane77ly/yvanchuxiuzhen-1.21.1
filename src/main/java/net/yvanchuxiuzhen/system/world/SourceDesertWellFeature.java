package net.yvanchuxiuzhen.system.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.predicate.BlockStatePredicate;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** 来源沙漠水井的两层砂岩地基、十字水池与顶棚生成。 */
final class SourceDesertWellFeature extends Feature<NoneFeatureConfiguration> {
    private static final BlockStatePredicate IS_SAND = BlockStatePredicate.forBlock(Blocks.SAND);
    private final BlockState slab = Blocks.SANDSTONE_SLAB.defaultBlockState();
    private final BlockState sandstone = Blocks.SANDSTONE.defaultBlockState();
    private final BlockState water = Blocks.WATER.defaultBlockState();

    SourceDesertWellFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin().above();
        while (level.isEmptyBlock(origin) && origin.getY() > level.getMinBuildHeight() + 2) {
            origin = origin.below();
        }
        if (!IS_SAND.test(level.getBlockState(origin))) {
            return false;
        }
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (level.isEmptyBlock(origin.offset(x, -1, z)) && level.isEmptyBlock(origin.offset(x, -2, z))) {
                    return false;
                }
            }
        }
        for (int y = -1; y <= 0; y++) {
            for (int x = -2; x <= 2; x++) {
                for (int z = -2; z <= 2; z++) {
                    level.setBlock(origin.offset(x, y, z), sandstone, 2);
                }
            }
        }
        level.setBlock(origin, water, 2);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            level.setBlock(origin.relative(direction), water, 2);
        }
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                if (x == -2 || x == 2 || z == -2 || z == 2) {
                    level.setBlock(origin.offset(x, 1, z), sandstone, 2);
                }
            }
        }
        level.setBlock(origin.offset(2, 1, 0), slab, 2);
        level.setBlock(origin.offset(-2, 1, 0), slab, 2);
        level.setBlock(origin.offset(0, 1, 2), slab, 2);
        level.setBlock(origin.offset(0, 1, -2), slab, 2);
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                level.setBlock(origin.offset(x, 4, z), x == 0 && z == 0 ? sandstone : slab, 2);
            }
        }
        for (int y = 1; y <= 3; y++) {
            level.setBlock(origin.offset(-1, y, -1), sandstone, 2);
            level.setBlock(origin.offset(-1, y, 1), sandstone, 2);
            level.setBlock(origin.offset(1, y, -1), sandstone, 2);
            level.setBlock(origin.offset(1, y, 1), sandstone, 2);
        }
        return true;
    }
}
