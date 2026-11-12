package net.yvanchuxiuzhen.system.world;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/** 群系默认生成中需要保留来源算法的独立地物类型。 */
public final class LegacyBiomeFeatures {
    private static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, ModId.VALUE);

    static {
        FEATURES.register("legacy_bamboo", SourceBambooFeature::new);
        FEATURES.register("legacy_desert_well", SourceDesertWellFeature::new);
        FEATURES.register("legacy_forest_rock", SourceBlockBlobFeature::new);
        FEATURES.register("legacy_large_dripstone", SourceLargeDripstoneFeature::new);
        FEATURES.register("legacy_lake", SourceLakeFeature::new);
        FEATURES.register("legacy_geode", SourceGeodeFeature::new);
        FEATURES.register("legacy_tree", SourceTreeFeature::new);
        FEATURES.register("legacy_root_system", SourceRootSystemFeature::new);
        FEATURES.register("legacy_tree_groundcover", SourceTreeGroundcoverFeature::new);
    }

    private LegacyBiomeFeatures() {
    }

    public static void register(IEventBus modBus) {
        FEATURES.register(modBus);
        SourceTreeComponents.register(modBus);
    }
}
