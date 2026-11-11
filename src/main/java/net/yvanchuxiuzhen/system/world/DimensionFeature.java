package net.yvanchuxiuzhen.system.world;

import com.mojang.serialization.Codec;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

/** 在原版生成算法之前保留来源维度门；空集合表示来源没有维度限制。 */
final class DimensionFeature<C extends FeatureConfiguration> extends Feature<C> {
    private final Feature<C> delegate;
    private final Set<ResourceLocation> dimensions;

    DimensionFeature(Codec<C> codec, Feature<C> delegate, Set<ResourceLocation> dimensions) {
        super(codec);
        this.delegate = delegate;
        this.dimensions = dimensions;
    }

    @Override
    public boolean place(FeaturePlaceContext<C> context) {
        if (!dimensions.isEmpty() && !dimensions.contains(context.level().getLevel().dimension().location())) {
            return false;
        }
        return delegate.place(context);
    }
}
