package net.yvanchuxiuzhen.system.world;

import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;

/** 来源树干公共操作：以锁定标签还原替换判定和土壤写入。 */
abstract class SourceTrunkPlacer extends TrunkPlacer {
    SourceTrunkPlacer(int baseHeight, int heightRandA, int heightRandB) {
        super(baseHeight, heightRandA, heightRandB);
    }

    protected static void setDirtAt(LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> setter,
            RandomSource random, BlockPos position, TreeConfiguration configuration) {
        boolean dirt = level.isStateAtPosition(position, state -> state.is(SourceTreeFeature.DIRT)
                && !state.is(Blocks.GRASS_BLOCK) && !state.is(Blocks.MYCELIUM));
        if (configuration.forceDirt || !dirt) {
            setter.accept(position, configuration.dirtProvider.getState(random, position));
        }
    }

    @Override
    protected boolean validTreePos(LevelSimulatedReader level, BlockPos position) {
        return SourceTreeFeature.validTreePos(level, position);
    }

    @Override
    public boolean isFree(LevelSimulatedReader level, BlockPos position) {
        return SourceTreeFeature.isFree(level, position);
    }

    @Override
    protected boolean placeLog(LevelSimulatedReader level, BiConsumer<BlockPos, BlockState> setter,
            RandomSource random, BlockPos position, TreeConfiguration configuration,
            Function<BlockState, BlockState> propertySetter) {
        if (SourceTreeFeature.validTreePos(level, position)) {
            setter.accept(position, propertySetter.apply(configuration.trunkProvider.getState(random, position)));
            return true;
        }
        return false;
    }
}
