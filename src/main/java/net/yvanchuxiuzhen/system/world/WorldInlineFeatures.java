package net.yvanchuxiuzhen.system.world;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/** 来源群系内联地物需要的独立算法类型；各实例配置由群系转换数据提供。 */
public final class WorldInlineFeatures {
    private static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, ModId.VALUE);

    static {
        FEATURES.register("legacy_biome_disk", LegacyBiomeDiskFeature::new);
    }

    private WorldInlineFeatures() {
    }

    public static void register(IEventBus modBus) {
        FEATURES.register(modBus);
    }
}
