package net.yvanchuxiuzhen.system.plant;

import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/** 按来源身份注册野生植物地物类型；群系投放由 World 装配。 */
final class WildPlantFeatures {
    private static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, ModId.VALUE);
    private static final Set<ResourceKey<Level>> RENJIAN = Set.of(dimension("dimensionrenjian"));
    private static final Set<ResourceKey<Level>> YAOHUN = Set.of(dimension("dimensionyaohun"));
    private static final Set<ResourceKey<Level>> DILING = Set.of(dimension("dimensiondiling"));
    private static final Set<ResourceKey<Level>> WUGUI = Set.of(dimension("dimensionwugui"));
    private static final Set<ResourceKey<Level>> RENJIAN_YAOHUN = Set.of(dimension("dimensionrenjian"), dimension("dimensionyaohun"));

    static {
        FEATURES.register("androsace_02", () -> new WildPlantFeature(RENJIAN));
        FEATURES.register("androsace", () -> new WildPlantFeature(RENJIAN));
        FEATURES.register("annualfleabane", () -> new WildPlantFeature(RENJIAN));
        FEATURES.register("asiaticdayflower", () -> new WildPlantFeature(RENJIAN));
        FEATURES.register("biggrass", () -> new WildPlantFeature(YAOHUN));
        FEATURES.register("bitegrass_01", () -> new WildPlantFeature(RENJIAN));
        FEATURES.register("bladegrass", () -> new WildPlantFeature(DILING));
        FEATURES.register("bluegrass", () -> new WildPlantFeature(RENJIAN));
        FEATURES.register("cannibalmoss", () -> new WildPlantFeature(RENJIAN));
        FEATURES.register("chineseviolet", () -> new WildPlantFeature(RENJIAN));
        FEATURES.register("chongshicao", () -> new WildPlantFeature(RENJIAN));
        FEATURES.register("coldleaf", () -> new WildPlantFeature(RENJIAN));
        FEATURES.register("columbinemimosa", () -> new WildPlantFeature(RENJIAN));
        FEATURES.register("corpsegrass", () -> new WildPlantFeature(WUGUI));
        FEATURES.register("fragranceflower", () -> new WildPlantFeature(RENJIAN));
        FEATURES.register("frostflowers", () -> new WildPlantFeature(RENJIAN));
        FEATURES.register("gardenia", () -> new WildPlantFeature(RENJIAN));
        FEATURES.register("goldenboilinggrass", () -> new WildPlantFeature(RENJIAN));
        FEATURES.register("greenbristlegrass", () -> new WildPlantFeature(RENJIAN));
        FEATURES.register("higanbana", () -> new WildPlantFeature(DILING));
        FEATURES.register("iceboundrock_02", () -> new WildPlantFeature(RENJIAN));
        FEATURES.register("iceboundrock", () -> new WildPlantFeature(RENJIAN));
        FEATURES.register("icecrystal", () -> new WildPlantFeature(RENJIAN));
        FEATURES.register("icicleflower", () -> new WildPlantFeature(RENJIAN));
        FEATURES.register("ironbamboo", () -> new WildPlantFeature(YAOHUN));
        FEATURES.register("ironbambootail", () -> new WildPlantFeature(YAOHUN));
        FEATURES.register("jadefairy", () -> new WildPlantFeature(RENJIAN));
        FEATURES.register("lamiumamplexicaule", () -> new WildPlantFeature(RENJIAN));
        FEATURES.register("lurker", () -> new WildPlantFeature(YAOHUN));
        FEATURES.register("ma_plant_04", () -> new WildPlantFeature(RENJIAN_YAOHUN));
        FEATURES.register("mian_plant_05", () -> new WildPlantFeature(RENJIAN_YAOHUN));
        FEATURES.register("mint", () -> new WildPlantFeature(YAOHUN));
        FEATURES.register("nautilus", () -> new WildPlantFeature(RENJIAN));
        FEATURES.register("nichanggrass_06", () -> new WildPlantFeature(RENJIAN_YAOHUN));
        FEATURES.register("opticalprism", () -> new WildPlantFeature(RENJIAN));
        FEATURES.register("purslane", () -> new WildPlantFeature(RENJIAN));
        FEATURES.register("raincloud", () -> new WildPlantFeature(RENJIAN));
        FEATURES.register("scorchedgrass", () -> new WildPlantFeature(RENJIAN));
        FEATURES.register("skybluealfalfa", () -> new WildPlantFeature(RENJIAN));
        FEATURES.register("soulgrass", () -> new WildPlantFeature(WUGUI));
        FEATURES.register("spiritgrass", () -> new WildPlantFeature(RENJIAN));
        FEATURES.register("tailbladegrass", () -> new WildPlantFeature(DILING));
        FEATURES.register("tailgreenbristlegrass", () -> new WildPlantFeature(RENJIAN));
        FEATURES.register("tailreed", () -> new WildPlantFeature(RENJIAN));
        FEATURES.register("thistlesandthorns", () -> new WildPlantFeature(YAOHUN));
        FEATURES.register("thundergrass", () -> new WildPlantFeature(RENJIAN));
        FEATURES.register("tripterygiumwilfordii", () -> new WildPlantFeature(YAOHUN));
    }

    private WildPlantFeatures() {
    }

    static void register(IEventBus modBus) {
        FEATURES.register(modBus);
    }

    private static ResourceKey<Level> dimension(String name) {
        return ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, name));
    }
}
