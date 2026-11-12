package net.yvanchuxiuzhen.system.world;

import com.google.common.collect.Lists;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

/** 来源树干布置：保留位置、随机调用和写入顺序，使用来源替换与土壤判定。 */
final class SourceBendingTrunkPlacer extends SourceTrunkPlacer {
    public static final MapCodec<SourceBendingTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec(
        p_161786_ -> trunkPlacerParts(p_161786_)
                .and(
                    p_161786_.group(
                        ExtraCodecs.POSITIVE_INT.optionalFieldOf("min_height_for_leaves", 1).forGetter(p_161788_ -> p_161788_.minHeightForLeaves),
                        IntProvider.codec(1, 64).fieldOf("bend_length").forGetter(p_161784_ -> p_161784_.bendLength)
                    )
                )
                .apply(p_161786_, SourceBendingTrunkPlacer::new)
    );
    private final int minHeightForLeaves;
    private final IntProvider bendLength;

    public SourceBendingTrunkPlacer(int baseHeight, int heightRandA, int heightRandB, int minHeightForLeaves, IntProvider bendLength) {
        super(baseHeight, heightRandA, heightRandB);
        this.minHeightForLeaves = minHeightForLeaves;
        this.bendLength = bendLength;
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return SourceTreeComponents.BENDING_TRUNK.get();
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
        Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        int i = freeTreeHeight - 1;
        BlockPos.MutableBlockPos blockpos$mutableblockpos = pos.mutable();
        BlockPos blockpos = blockpos$mutableblockpos.below();
        setDirtAt(level, blockSetter, random, blockpos, config);
        List<FoliagePlacer.FoliageAttachment> list = Lists.newArrayList();

        for (int j = 0; j <= i; j++) {
            if (j + 1 >= i + random.nextInt(2)) {
                blockpos$mutableblockpos.move(direction);
            }

            if (SourceTreeFeature.validTreePos(level, blockpos$mutableblockpos)) {
                this.placeLog(level, blockSetter, random, blockpos$mutableblockpos, config);
            }

            if (j >= this.minHeightForLeaves) {
                list.add(new FoliagePlacer.FoliageAttachment(blockpos$mutableblockpos.immutable(), 0, false));
            }

            blockpos$mutableblockpos.move(Direction.UP);
        }

        int l = this.bendLength.sample(random);

        for (int k = 0; k <= l; k++) {
            if (SourceTreeFeature.validTreePos(level, blockpos$mutableblockpos)) {
                this.placeLog(level, blockSetter, random, blockpos$mutableblockpos, config);
            }

            list.add(new FoliagePlacer.FoliageAttachment(blockpos$mutableblockpos.immutable(), 0, false));
            blockpos$mutableblockpos.move(direction);
        }

        return list;
    }
}
