package net.yvanchuxiuzhen.system.world;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.world.WorldTemplateFeature.HeightMode;
import net.yvanchuxiuzhen.system.world.WorldTemplateFeature.Parameters;

/** 24种自然景观与随机结构的来源身份及逐项放置参数。 */
public final class WorldTemplateFeatures {
    private static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, ModId.VALUE);
    private static final Set<ResourceLocation> RENJIAN = Set.of(id("dimensionrenjian"));
    private static final Set<ResourceLocation> YAOHUN = Set.of(id("dimensionyaohun"));
    private static final Set<ResourceLocation> BOTH = Set.of(id("dimensionrenjian"), id("dimensionyaohun"));
    private static final List<ResourceLocation> NO_BASE = List.of();
    private static final List<ResourceLocation> GRASS = vanilla("grass_block");
    private static final List<ResourceLocation> GROUND = vanilla(
            "grass_block", "dirt_path", "mycelium", "dirt", "coarse_dirt", "podzol", "rooted_dirt");
    private static final List<ResourceLocation> STONY_GROUND = vanilla(
            "stone", "grass_block", "dirt_path", "mycelium", "dirt", "coarse_dirt", "podzol", "rooted_dirt", "cobblestone");
    private static final List<ResourceLocation> STONE = vanilla("stone");
    private static final List<ResourceLocation> LOESS = List.of(id("loess"));
    private static final List<ResourceLocation> LOESS_SAND = List.of(
            id("loess"), ResourceLocation.withDefaultNamespace("sand"), ResourceLocation.withDefaultNamespace("red_sand"));
    private static final List<ResourceLocation> RED_LOESS_SAND = List.of(
            ResourceLocation.withDefaultNamespace("red_sand"), id("loess"), ResourceLocation.withDefaultNamespace("sand"));

    static {
        add("huaguangshengcheng", "huaguang", RENJIAN, NO_BASE, 500000, 1, 1,
                Heightmap.Types.OCEAN_FLOOR_WG, HeightMode.GROUND, new BlockPos(0, 1, 0),
                false, BlockIgnoreProcessor.AIR, false, true);
        add("evilspirittree_01", "evil_spirit_tree01", YAOHUN, LOESS_SAND, 30000, 1, 1,
                Heightmap.Types.OCEAN_FLOOR_WG, HeightMode.GROUND, new BlockPos(-3, 0, -3),
                false, BlockIgnoreProcessor.AIR, false, false);
        add("evilspirittree_02", "evil_spirit_tree02", YAOHUN, LOESS_SAND, 20000, 1, 1,
                Heightmap.Types.OCEAN_FLOOR_WG, HeightMode.GROUND, new BlockPos(-1, 0, -1),
                false, BlockIgnoreProcessor.AIR, false, false);
        add("evilspirittree_03", "evil_spirit_tree03", YAOHUN, LOESS_SAND, 20000, 1, 1,
                Heightmap.Types.OCEAN_FLOOR_WG, HeightMode.GROUND, new BlockPos(-3, -3, -3),
                false, BlockIgnoreProcessor.AIR, false, false);
        add("evilspirittree_04", "evil_spirit_tree04", YAOHUN, LOESS_SAND, 20000, 1, 1,
                Heightmap.Types.OCEAN_FLOOR_WG, HeightMode.GROUND, new BlockPos(-2, -3, -2),
                false, BlockIgnoreProcessor.AIR, false, false);
        add("loessrock", "loessrock", YAOHUN, LOESS, 100000, 6, 3,
                Heightmap.Types.OCEAN_FLOOR_WG, HeightMode.GROUND, new BlockPos(0, -4, 0),
                true, BlockIgnoreProcessor.AIR, false, false);
        add("dingxiang_01", "dingxiang_01", BOTH, GRASS, 30000, 1, 1,
                Heightmap.Types.OCEAN_FLOOR_WG, HeightMode.GROUND, new BlockPos(-4, 1, -4),
                false, BlockIgnoreProcessor.AIR, false, false);
        add("arrowpoisonwood_01", "arrowpoisonwood_01", YAOHUN, RED_LOESS_SAND, 30000, 1, 1,
                Heightmap.Types.OCEAN_FLOOR_WG, HeightMode.GROUND, new BlockPos(-6, -8, -5),
                true, BlockIgnoreProcessor.AIR, false, false);
        add("zhuzidaoyv_01", "zhuzidaoyv01", YAOHUN, NO_BASE, 10000, 1, 1,
                Heightmap.Types.OCEAN_FLOOR_WG, HeightMode.FLOATING, new BlockPos(0, 0, 0),
                true, BlockIgnoreProcessor.AIR, false, false);
        add("zhuzidaoyv_02", "zhuzidaoyv02", YAOHUN, NO_BASE, 20000, 1, 1,
                Heightmap.Types.OCEAN_FLOOR_WG, HeightMode.FLOATING, new BlockPos(0, 0, 0),
                true, BlockIgnoreProcessor.AIR, false, false);
        add("zhuzidaoyv_03", "zhuzidaoyv03", YAOHUN, NO_BASE, 30000, 1, 1,
                Heightmap.Types.OCEAN_FLOOR_WG, HeightMode.FLOATING, new BlockPos(0, 0, 0),
                true, BlockIgnoreProcessor.AIR, false, false);
        add("zhuzidaoyv_04", "zhuzidaoyv04", YAOHUN, NO_BASE, 40000, 1, 1,
                Heightmap.Types.OCEAN_FLOOR_WG, HeightMode.FLOATING, new BlockPos(0, 0, 0),
                true, BlockIgnoreProcessor.AIR, false, false);
        add("fukongjiaoshi", "fukongjiaoshi", YAOHUN, NO_BASE, 20000, 1, 1,
                Heightmap.Types.OCEAN_FLOOR_WG, HeightMode.FLOATING, new BlockPos(0, -5, 0),
                true, BlockIgnoreProcessor.AIR, false, false);
        add("suijijiegou_01", "suijijiegou01", RENJIAN, GROUND, 1000, 1, 1,
                Heightmap.Types.WORLD_SURFACE_WG, HeightMode.GROUND, new BlockPos(0, -1, 0),
                true, BlockIgnoreProcessor.STRUCTURE_AND_AIR, true, false);
        add("suijijiegou_02", "suijijiegou02", RENJIAN, GROUND, 1000, 1, 1,
                Heightmap.Types.WORLD_SURFACE_WG, HeightMode.GROUND, new BlockPos(0, 0, 0),
                true, BlockIgnoreProcessor.STRUCTURE_AND_AIR, true, false);
        add("suijijiegou_03", "suijijiegou03", RENJIAN, STONY_GROUND, 1000, 1, 1,
                Heightmap.Types.WORLD_SURFACE_WG, HeightMode.GROUND, new BlockPos(0, 0, 0),
                true, BlockIgnoreProcessor.STRUCTURE_AND_AIR, true, false);
        add("suijijiegou_04", "suijijiegou04", RENJIAN, GROUND, 1000, 1, 1,
                Heightmap.Types.WORLD_SURFACE_WG, HeightMode.GROUND, new BlockPos(0, -3, 0),
                true, BlockIgnoreProcessor.STRUCTURE_AND_AIR, true, false);
        add("suijijiegou_05", "suijijiegou05", RENJIAN, STONE, 1000, 1, 1,
                Heightmap.Types.OCEAN_FLOOR_WG, HeightMode.UNDERGROUND, new BlockPos(0, -5, 0),
                true, BlockIgnoreProcessor.STRUCTURE_BLOCK, true, false);
        add("suijijiegou_06", "suijijiegou06", RENJIAN, STONE, 1000, 1, 1,
                Heightmap.Types.OCEAN_FLOOR_WG, HeightMode.UNDERGROUND, new BlockPos(0, -10, 0),
                true, BlockIgnoreProcessor.STRUCTURE_BLOCK, true, false);
        add("stonetree_02", "stone02", RENJIAN, NO_BASE, 500000, 1, 1,
                Heightmap.Types.OCEAN_FLOOR_WG, HeightMode.GROUND, new BlockPos(0, 0, 0),
                true, BlockIgnoreProcessor.AIR, false, false);
        add("stonetree_03", "stone03", RENJIAN, NO_BASE, 500000, 1, 1,
                Heightmap.Types.OCEAN_FLOOR_WG, HeightMode.GROUND, new BlockPos(0, 1, 0),
                true, BlockIgnoreProcessor.AIR, false, false);
        add("stonetree_04", "stone04", RENJIAN, NO_BASE, 500000, 1, 1,
                Heightmap.Types.OCEAN_FLOOR_WG, HeightMode.GROUND, new BlockPos(0, 1, 0),
                true, BlockIgnoreProcessor.AIR, false, false);
        add("stonetree_06", "stone06", RENJIAN, NO_BASE, 200000, 1, 1,
                Heightmap.Types.OCEAN_FLOOR_WG, HeightMode.GROUND, new BlockPos(5, 0, 4),
                true, BlockIgnoreProcessor.AIR, false, false);
        add("stonetree_07", "stone07", RENJIAN, NO_BASE, 200000, 1, 1,
                Heightmap.Types.OCEAN_FLOOR_WG, HeightMode.GROUND, new BlockPos(5, 0, 5),
                true, BlockIgnoreProcessor.AIR, false, false);
    }

    private WorldTemplateFeatures() {
    }

    public static void register(IEventBus modBus) {
        FEATURES.register(modBus);
    }

    private static void add(String name, String template, Set<ResourceLocation> dimensions, List<ResourceLocation> baseBlocks,
            int chance, int countBound, int countMinimum, Heightmap.Types heightmap, HeightMode heightMode,
            BlockPos offset, boolean randomTransform, BlockIgnoreProcessor processor, boolean randomStructure, boolean huaguang) {
        Parameters parameters = new Parameters(id(template), dimensions, baseBlocks, chance, countBound, countMinimum,
                heightmap, heightMode, offset, randomTransform, processor, randomStructure, huaguang);
        FEATURES.register(name, () -> new WorldTemplateFeature(parameters));
    }

    private static List<ResourceLocation> vanilla(String... paths) {
        return Arrays.stream(paths).map(ResourceLocation::withDefaultNamespace).toList();
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
