package net.yvanchuxiuzhen.system.world;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;

/** 来源树干布置：保留位置、随机调用和写入顺序，使用来源替换与土壤判定。 */
abstract class SourceGiantTrunkPlacer extends SourceTrunkPlacer {

    public SourceGiantTrunkPlacer(int baseHeight, int heightRandA, int heightRandB) {
        super(baseHeight, heightRandA, heightRandB);
    }


    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(
        LevelSimulatedReader level,
        BiConsumer<BlockPos, BlockState> blockSetter,
        RandomSource random,
        int freeTreeHeight,
        BlockPos pos,
        TreeConfiguration config
    ) {
        BlockPos blockpos = pos.below();
        setDirtAt(level, blockSetter, random, blockpos, config);
        setDirtAt(level, blockSetter, random, blockpos.east(), config);
        setDirtAt(level, blockSetter, random, blockpos.south(), config);
        setDirtAt(level, blockSetter, random, blockpos.south().east(), config);
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

        for (int i = 0; i < freeTreeHeight; i++) {
            this.placeLogIfFreeWithOffset(level, blockSetter, random, blockpos$mutableblockpos, config, pos, 0, i, 0);
            if (i < freeTreeHeight - 1) {
                this.placeLogIfFreeWithOffset(level, blockSetter, random, blockpos$mutableblockpos, config, pos, 1, i, 0);
                this.placeLogIfFreeWithOffset(level, blockSetter, random, blockpos$mutableblockpos, config, pos, 1, i, 1);
                this.placeLogIfFreeWithOffset(level, blockSetter, random, blockpos$mutableblockpos, config, pos, 0, i, 1);
            }
        }

        return ImmutableList.of(new FoliagePlacer.FoliageAttachment(pos.above(freeTreeHeight), 0, true));
    }

    private void placeLogIfFreeWithOffset(
        LevelSimulatedReader level,
        BiConsumer<BlockPos, BlockState> blockSetter,
        RandomSource random,
        BlockPos.MutableBlockPos pos,
        TreeConfiguration config,
        BlockPos offsetPos,
        int offsetX,
        int offsetY,
        int offsetZ
    ) {
        pos.setWithOffset(offsetPos, offsetX, offsetY, offsetZ);
        this.placeLogIfFree(level, blockSetter, random, pos, config);
    }
}
