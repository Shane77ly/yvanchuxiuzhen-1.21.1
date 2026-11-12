package net.yvanchuxiuzhen.system.world;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.LargeDripstoneConfiguration;

/** 限定来源风偏移取整语义的大型滴水石地物入口。 */
public final class SourceLargeDripstoneFeature extends Feature<LargeDripstoneConfiguration> {
    private static final ThreadLocal<Boolean> LEGACY_OFFSET = ThreadLocal.withInitial(() -> false);

    public SourceLargeDripstoneFeature() {
        super(LargeDripstoneConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<LargeDripstoneConfiguration> context) {
        @SuppressWarnings("unchecked")
        Feature<LargeDripstoneConfiguration> delegate = (Feature<LargeDripstoneConfiguration>)
                BuiltInRegistries.FEATURE.get(ResourceLocation.withDefaultNamespace("large_dripstone"));
        boolean wasActive = LEGACY_OFFSET.get();
        LEGACY_OFFSET.set(true);
        try {
            return delegate.place(context);
        } finally {
            if (wasActive) {
                LEGACY_OFFSET.set(true);
            } else {
                LEGACY_OFFSET.remove();
            }
        }
    }

    public static boolean usesLegacyOffset() {
        return LEGACY_OFFSET.get();
    }
}
