package net.yvanchuxiuzhen.system.world;

import java.util.Set;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.OreFeature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;

/** 矿物地物在原生矿脉算法取随机数前核对来源维度条件。 */
final class DimensionOreFeature extends OreFeature {
    private final Set<ResourceKey<Level>> dimensions;

    DimensionOreFeature(Set<ResourceKey<Level>> dimensions) {
        super(OreConfiguration.CODEC);
        this.dimensions = Set.copyOf(dimensions);
    }

    @Override
    public boolean place(FeaturePlaceContext<OreConfiguration> context) {
        return dimensions.contains(context.level().getLevel().dimension()) && super.place(context);
    }
}
