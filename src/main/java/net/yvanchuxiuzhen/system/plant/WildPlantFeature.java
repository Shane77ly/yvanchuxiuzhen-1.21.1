package net.yvanchuxiuzhen.system.plant;

import java.util.Set;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.RandomPatchFeature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;

/** 普通野生植物地物的维度过滤；采样与放置继续由原生随机斑块完成。 */
final class WildPlantFeature extends RandomPatchFeature {
    private final Set<ResourceKey<Level>> dimensions;

    WildPlantFeature(Set<ResourceKey<Level>> dimensions) {
        super(RandomPatchConfiguration.CODEC);
        this.dimensions = Set.copyOf(dimensions);
    }

    @Override
    public boolean place(FeaturePlaceContext<RandomPatchConfiguration> context) {
        return dimensions.contains(context.level().getLevel().dimension()) && super.place(context);
    }
}
