package net.yvanchuxiuzhen.system.world;

import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/** 32种来源矿物地物的身份与维度门槛，矿脉参数及群系投放由生成数据承担。 */
public final class WorldOreFeatures {
    private static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, ModId.VALUE);
    private static final Set<ResourceKey<Level>> RENJIAN = Set.of(dimension("dimensionrenjian"));
    private static final Set<ResourceKey<Level>> RENJIAN_YAOHUN = Set.of(
            dimension("dimensionrenjian"), dimension("dimensionyaohun"));
    private static final Set<ResourceKey<Level>> FOUR_DIMENSIONS = Set.of(
            dimension("dimensionxiuluo"), dimension("dimensionrenjian"),
            dimension("dimensionyaohun"), dimension("dimensionwugui"));

    static {
        add("bluestone", RENJIAN);
        add("cailiao_16101", RENJIAN_YAOHUN);
        add("cailiao_16201", RENJIAN_YAOHUN);
        add("cailiao_16301", RENJIAN_YAOHUN);
        add("cailiao_16401", RENJIAN_YAOHUN);
        add("cailiao_16501", RENJIAN_YAOHUN);
        add("cailiao_16601", RENJIAN_YAOHUN);
        add("cailiao_16701", RENJIAN_YAOHUN);
        add("cailiao_16801", RENJIAN_YAOHUN);
        add("cailiao_16901", RENJIAN_YAOHUN);
        add("cailiao_191ore", FOUR_DIMENSIONS);
        add("cailiao_192ore", FOUR_DIMENSIONS);
        add("cailiao_193ore", FOUR_DIMENSIONS);
        add("cailiao_194ore", FOUR_DIMENSIONS);
        add("cailiao_195ore", FOUR_DIMENSIONS);
        add("cailiao_196ore", FOUR_DIMENSIONS);
        add("cailiao_197ore", FOUR_DIMENSIONS);
        add("cailiao_198ore", FOUR_DIMENSIONS);
        add("cailiao_199ore", FOUR_DIMENSIONS);
        add("cailiao_20101", RENJIAN_YAOHUN);
        add("cailiao_20301", RENJIAN_YAOHUN);
        add("cailiao_20501", RENJIAN_YAOHUN);
        add("cailiao_20701", RENJIAN_YAOHUN);
        add("cailiao_20801", RENJIAN_YAOHUN);
        add("cailiao_20901", RENJIAN_YAOHUN);
        add("cailiao_21101", RENJIAN_YAOHUN);
        add("cailiao_21301", RENJIAN_YAOHUN);
        add("cailiao_21501", RENJIAN_YAOHUN);
        add("cailiao_21601", RENJIAN_YAOHUN);
        add("cailiao_21701", RENJIAN_YAOHUN);
        add("cailiao_21901", RENJIAN_YAOHUN);
        add("fuluzhushablock", FOUR_DIMENSIONS);
    }

    private WorldOreFeatures() {
    }

    public static void register(IEventBus modBus) {
        FEATURES.register(modBus);
    }

    private static void add(String name, Set<ResourceKey<Level>> dimensions) {
        FEATURES.register(name, () -> new DimensionOreFeature(dimensions));
    }

    private static ResourceKey<Level> dimension(String path) {
        return ResourceKey.create(Registries.DIMENSION, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path));
    }
}
