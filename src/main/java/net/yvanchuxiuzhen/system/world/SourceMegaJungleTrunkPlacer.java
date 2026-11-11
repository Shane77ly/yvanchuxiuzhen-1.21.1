package net.yvanchuxiuzhen.system.world;

import com.google.common.collect.Lists;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

/** 来源树干布置：保留位置、随机调用和写入顺序，使用来源替换与土壤判定。 */
final class SourceMegaJungleTrunkPlacer extends SourceGiantTrunkPlacer {
    public static final MapCodec<SourceMegaJungleTrunkPlacer> CODEC = RecordCodecBuilder.mapCodec(
        p_70206_ -> trunkPlacerParts(p_70206_).apply(p_70206_, SourceMegaJungleTrunkPlacer::new)
    );

    public SourceMegaJungleTrunkPlacer(int baseHeight, int heightRandA, int heightRandB) {
        super(baseHeight, heightRandA, heightRandB);
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return SourceTreeComponents.MEGA_JUNGLE_TRUNK.get();
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
        List<FoliagePlacer.FoliageAttachment> list = Lists.newArrayList();
        list.addAll(super.placeTrunk(level, blockSetter, random, freeTreeHeight, pos, config));

        for (int i = freeTreeHeight - 2 - random.nextInt(4); i > freeTreeHeight / 2; i -= 2 + random.nextInt(4)) {
            float f = random.nextFloat() * ((float)Math.PI * 2.0F);
            int j = 0;
            int k = 0;

            for (int l = 0; l < 5; l++) {
                j = (int)(1.5F + Mth.cos(f) * (float)l);
                k = (int)(1.5F + Mth.sin(f) * (float)l);
                BlockPos blockpos = pos.offset(j, i - 3 + l / 2, k);
                this.placeLog(level, blockSetter, random, blockpos, config);
            }

            list.add(new FoliagePlacer.FoliageAttachment(pos.offset(j, i, k), -2, false));
        }

        return list;
    }
}
