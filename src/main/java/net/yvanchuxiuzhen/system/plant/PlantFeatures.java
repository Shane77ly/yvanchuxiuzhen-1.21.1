package net.yvanchuxiuzhen.system.plant;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/** 植物系统拥有的地物类型注册，由PlantSetup统一接入模组事件总线。 */
final class PlantFeatures {
    private static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, ModId.VALUE);

    static {
        FEATURES.register("shicai_001_plant_08", CerealFeature::new);
        FEATURES.register("shicai_002_plant_08", CerealFeature::new);
    }

    private PlantFeatures() {
    }

    static void register(IEventBus modBus) {
        FEATURES.register(modBus);
    }
}
