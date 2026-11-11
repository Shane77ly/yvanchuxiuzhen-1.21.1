package net.yvanchuxiuzhen.system.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;

/** 来源树冠布置：保持层次、形状筛选及随机消费，叶片替换由来源公共实现处理。 */
final class SourceRandomSpreadFoliagePlacer extends SourceFoliagePlacer {
    public static final MapCodec<SourceRandomSpreadFoliagePlacer> CODEC = RecordCodecBuilder.mapCodec(
        p_161522_ -> foliagePlacerParts(p_161522_)
                .and(
                    p_161522_.group(
                        IntProvider.codec(1, 512).fieldOf("foliage_height").forGetter(p_161537_ -> p_161537_.foliageHeight),
                        Codec.intRange(0, 256).fieldOf("leaf_placement_attempts").forGetter(p_161524_ -> p_161524_.leafPlacementAttempts)
                    )
                )
                .apply(p_161522_, SourceRandomSpreadFoliagePlacer::new)
    );
    private final IntProvider foliageHeight;
    private final int leafPlacementAttempts;

    public SourceRandomSpreadFoliagePlacer(IntProvider radius, IntProvider offset, IntProvider foliageHeight, int leafPlacementAttempts) {
        super(radius, offset);
        this.foliageHeight = foliageHeight;
        this.leafPlacementAttempts = leafPlacementAttempts;
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return SourceTreeComponents.RANDOM_SPREAD_FOLIAGE.get();
    }

    @Override
    protected void createFoliage(
        LevelSimulatedReader level,
        FoliagePlacer.FoliageSetter blockSetter,
        RandomSource random,
        TreeConfiguration config,
        int maxFreeTreeHeight,
        FoliagePlacer.FoliageAttachment attachment,
        int foliageHeight,
        int foliageRadius,
        int offset
    ) {
        BlockPos blockpos = attachment.pos();
        BlockPos.MutableBlockPos blockpos$mutableblockpos = blockpos.mutable();

        for (int i = 0; i < this.leafPlacementAttempts; i++) {
            blockpos$mutableblockpos.setWithOffset(
                blockpos,
                random.nextInt(foliageRadius) - random.nextInt(foliageRadius),
                random.nextInt(foliageHeight) - random.nextInt(foliageHeight),
                random.nextInt(foliageRadius) - random.nextInt(foliageRadius)
            );
            tryPlaceLeaf(level, blockSetter, random, config, blockpos$mutableblockpos);
        }
    }

    @Override
    public int foliageHeight(RandomSource random, int height, TreeConfiguration config) {
        return this.foliageHeight.sample(random);
    }


    @Override
    protected boolean shouldSkipLocation(RandomSource random, int localX, int localY, int localZ, int range, boolean large) {
        return false;
    }
}
