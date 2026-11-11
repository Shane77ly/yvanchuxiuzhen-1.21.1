package net.yvanchuxiuzhen.system.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;

/** 来源树冠布置：保持层次、形状筛选及随机消费，叶片替换由来源公共实现处理。 */
final class SourceMegaJungleFoliagePlacer extends SourceFoliagePlacer {
    public static final MapCodec<SourceMegaJungleFoliagePlacer> CODEC = RecordCodecBuilder.mapCodec(
        p_68630_ -> foliagePlacerParts(p_68630_)
                .and(Codec.intRange(0, 16).fieldOf("height").forGetter(p_161468_ -> p_161468_.height))
                .apply(p_68630_, SourceMegaJungleFoliagePlacer::new)
    );
    protected final int height;

    public SourceMegaJungleFoliagePlacer(IntProvider radius, IntProvider offset, int height) {
        super(radius, offset);
        this.height = height;
    }

    @Override
    protected FoliagePlacerType<?> type() {
        return SourceTreeComponents.MEGA_JUNGLE_FOLIAGE.get();
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
        int i = attachment.doubleTrunk() ? foliageHeight : 1 + random.nextInt(2);

        for (int j = offset; j >= offset - i; j--) {
            int k = foliageRadius + attachment.radiusOffset() + 1 - j;
            this.placeLeavesRow(level, blockSetter, random, config, attachment.pos(), k, j, attachment.doubleTrunk());
        }
    }

    @Override
    public int foliageHeight(RandomSource random, int height, TreeConfiguration config) {
        return this.height;
    }


    @Override
    protected boolean shouldSkipLocation(RandomSource random, int localX, int localY, int localZ, int range, boolean large) {
        return localX + localZ >= 7 ? true : localX * localX + localZ * localZ > range * range;
    }
}
