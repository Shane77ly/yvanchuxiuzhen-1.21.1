package net.yvanchuxiuzhen.system.plant;

import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.RandomPatchFeature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.yvanchuxiuzhen.platform.ModId;

/** 001水稻与002糯稻成熟植株地物共有的两维度生成限制。 */
public final class CerealFeature extends RandomPatchFeature {
    private static final Set<ResourceKey<Level>> DIMENSIONS = Set.of(
            ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "dimensionrenjian")),
            ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "dimensionyaohun")));

    public CerealFeature() {
        super(RandomPatchConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<RandomPatchConfiguration> context) {
        return DIMENSIONS.contains(context.level().getLevel().dimension()) && super.place(context);
    }
}
