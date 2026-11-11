package net.yvanchuxiuzhen.system.world;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;

/** 来源树叶公共操作：按原始矩形遍历与位置谓词直接写入提供器状态。 */
abstract class SourceFoliagePlacer extends FoliagePlacer {
    SourceFoliagePlacer(IntProvider radius, IntProvider offset) {
        super(radius, offset);
    }

    @Override
    protected void placeLeavesRow(LevelSimulatedReader level, FoliageSetter setter, RandomSource random,
            TreeConfiguration configuration, BlockPos position, int radius, int localY, boolean doubleTrunk) {
        int extra = doubleTrunk ? 1 : 0;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = -radius; x <= radius + extra; x++) {
            for (int z = -radius; z <= radius + extra; z++) {
                if (!shouldSkipLocationSigned(random, x, localY, z, radius, doubleTrunk)) {
                    cursor.setWithOffset(position, x, localY, z);
                    tryPlaceLeaf(level, setter, random, configuration, cursor);
                }
            }
        }
    }

    protected static boolean tryPlaceLeaf(LevelSimulatedReader level, FoliageSetter setter, RandomSource random,
            TreeConfiguration configuration, BlockPos position) {
        if (SourceTreeFeature.validTreePos(level, position)) {
            setter.set(position, configuration.foliageProvider.getState(random, position));
            return true;
        }
        return false;
    }
}
