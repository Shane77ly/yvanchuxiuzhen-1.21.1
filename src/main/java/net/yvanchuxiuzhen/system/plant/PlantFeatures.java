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
        FEATURES.register("shicai_001_plant_08", CropFeature::new);
        FEATURES.register("shicai_002_plant_08", CropFeature::new);
        FEATURES.register("shicai_004_plant_08", CropFeature::new);
        FEATURES.register("shicai_005_plant_08", CropFeature::new);
        FEATURES.register("shicai_006_plant_08", CropFeature::new);
        FEATURES.register("shicai_007_plant_04", CropFeature::new);
        FEATURES.register("shicai_025_plant_04", CropFeature::new);
        FEATURES.register("shicai_026_plant_04", CropFeature::new);
        FEATURES.register("shicai_027_plant_04", CropFeature::new);
        FEATURES.register("shicai_011_plant_04", CropFeature::new);
        FEATURES.register("shicai_012_plant_04", CropFeature::new);
        FEATURES.register("shicai_013_plant_04", CropFeature::new);
        FEATURES.register("shicai_014_plant_04", CropFeature::new);
        FEATURES.register("shicai_016_plant_04", CropFeature::new);
        FEATURES.register("shicai_017_plant_04", CropFeature::new);
        FEATURES.register("shicai_018_plant_04", CropFeature::new);
        FEATURES.register("shicai_019_plant_04", CropFeature::new);
        FEATURES.register("shicai_020_plant_04", CropFeature::new);
        FEATURES.register("shicai_021_plant_04", CropFeature::new);
        FEATURES.register("shicai_022_plant_04", CropFeature::new);
        FEATURES.register("shicai_023_plant_04", CropFeature::new);
        FEATURES.register("shicai_024_plant_04", CropFeature::new);
        FEATURES.register("shicai_028_plant_04", CropFeature::new);
        FEATURES.register("shicai_029_plant_04", CropFeature::new);
        FEATURES.register("shicai_030_plant_04", CropFeature::new);
        FEATURES.register("shicai_031_plant_04", CropFeature::new);
        FEATURES.register("shicai_032_plant_04", CropFeature::new);
        FEATURES.register("shicai_033_plant_04", CropFeature::new);
        FEATURES.register("shicai_00907", () -> new FruitTreeFeature(FruitTree.SHICAI_009));
        FEATURES.register("shicai_01004", () -> new FruitTreeFeature(FruitTree.SHICAI_010));
        FEATURES.register("shicai_042_plant_05", CropFeature::new);
        FEATURES.register("shicai_043_plant_04", CropFeature::new);
        FEATURES.register("shicai_044_plant_04", CropFeature::new);
        FEATURES.register("shicai_045_plant_04", CropFeature::new);
        FEATURES.register("shicai_046_plant_04", CropFeature::new);
        FEATURES.register("shicai_047_plant_04", CropFeature::new);
        FEATURES.register("shicai_048_plant_04", CropFeature::new);
        FEATURES.register("shicai_051_plant_04", CropFeature::new);
        FEATURES.register("shicai_054_plant_04", CropFeature::new);
        FEATURES.register("shicai_055_plant_04", CropFeature::new);
        FEATURES.register("shicai_056_plant_05", CropFeature::new);
        FEATURES.register("shicai_05701", CropFeature::new);
    }

    private PlantFeatures() {
    }

    static void register(IEventBus modBus) {
        FEATURES.register(modBus);
    }
}
