package net.yvanchuxiuzhen.system.plant;

import java.util.List;
import java.util.Set;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/** 来源自然模板树木的注册ID与逐地物参数；群系投放由世界生成数据承载。 */
public final class NaturalTreeFeatures {
    private static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, ModId.VALUE);
    private static final Set<ResourceLocation> RENJIAN = Set.of(id("dimensionrenjian"));
    private static final Set<ResourceLocation> YAOHUN = Set.of(id("dimensionyaohun"));
    private static final Set<ResourceLocation> BOTH = Set.of(id("dimensionrenjian"), id("dimensionyaohun"));
    private static final List<Block> GRASS = List.of(Blocks.GRASS_BLOCK);
    private static final List<Block> SNOW = List.of(Blocks.GRASS_BLOCK, Blocks.SNOW_BLOCK);

    static {
        add("cassia_06", "cassia_06", BOTH, GRASS, 500000, Heightmap.Types.OCEAN_FLOOR_WG, 3);
        add("elm_07", "elm_07", RENJIAN, GRASS, 500000, Heightmap.Types.OCEAN_FLOOR_WG, 3);
        add("elm_08", "elm_08", RENJIAN, GRASS, 100000, Heightmap.Types.OCEAN_FLOOR_WG, 3);
        add("laurel_06", "laurel_06", BOTH, GRASS, 500000, Heightmap.Types.OCEAN_FLOOR_WG, 3);
        add("laurel_08", "laurel_08", RENJIAN, GRASS, 500000, Heightmap.Types.OCEAN_FLOOR_WG, 3);
        add("maple_01", "maple_01", RENJIAN, GRASS, 100000, Heightmap.Types.OCEAN_FLOOR_WG, 6);
        add("mulberry_02", "mulberry_02", BOTH,
                List.of(Blocks.GRASS_BLOCK, Blocks.DIRT_PATH, Blocks.MYCELIUM, Blocks.DIRT, Blocks.COARSE_DIRT,
                        Blocks.PODZOL, Blocks.ROOTED_DIRT), 50000, Heightmap.Types.WORLD_SURFACE_WG, 4);
        add("peach_01", "peach_01", BOTH, GRASS, 500000, Heightmap.Types.OCEAN_FLOOR_WG, 3);
        add("poplar_01", "poplar_01", RENJIAN, GRASS, 100000, Heightmap.Types.OCEAN_FLOOR_WG, 4);
        add("poplar_03", "poplar_03", RENJIAN, GRASS, 100000, Heightmap.Types.OCEAN_FLOOR_WG, 4);
        add("privet_05", "privet_05", BOTH, GRASS, 500000, Heightmap.Types.OCEAN_FLOOR_WG, 3, true);
        add("sandalwoodtree_01", "sandalwood_tree01", YAOHUN,
                List.of(Blocks.GRASS_BLOCK, Blocks.DIRT_PATH, Blocks.MYCELIUM, Blocks.DIRT, Blocks.COARSE_DIRT),
                10000, Heightmap.Types.WORLD_SURFACE_WG, 4);
        add("treejiegou_01", "tree_jiegou01", RENJIAN, SNOW, 10000, Heightmap.Types.OCEAN_FLOOR_WG, 4);
        add("treejiegou_03", "tree_jiegou03", RENJIAN, SNOW, 1000000, Heightmap.Types.OCEAN_FLOOR_WG, 3);
        add("treejiegou_07", "tree_jiegou07", RENJIAN, GRASS, 100000, Heightmap.Types.OCEAN_FLOOR_WG, 5);
        add("treejiegou_09", "tree_jiegou09", RENJIAN, GRASS, 100000, Heightmap.Types.OCEAN_FLOOR_WG, 7);
        add("treejiegou_12", "tree_jiegou12", RENJIAN, SNOW, 500000, Heightmap.Types.OCEAN_FLOOR_WG, 6);
        add("treejiegou_15", "tree_jiegou15", YAOHUN,
                List.of(Blocks.GRASS_BLOCK, Blocks.DIRT_PATH, Blocks.DIRT, Blocks.COARSE_DIRT),
                100000, Heightmap.Types.WORLD_SURFACE_WG, 5);
        add("treejiegou_17", "tree_jiegou17", RENJIAN, GRASS, 50000, Heightmap.Types.OCEAN_FLOOR_WG, 5);
        add("treejiegou_18", "tree_jiegou18", RENJIAN, GRASS, 10000, Heightmap.Types.OCEAN_FLOOR_WG, 5);
    }

    private NaturalTreeFeatures() {
    }

    public static void register(IEventBus modBus) {
        FEATURES.register(modBus);
    }

    private static void add(String name, String template, Set<ResourceLocation> dimensions, List<Block> baseBlocks,
            int chance, Heightmap.Types heightmap, int horizontalOffset) {
        add(name, template, dimensions, baseBlocks, chance, heightmap, horizontalOffset, false);
    }

    private static void add(String name, String template, Set<ResourceLocation> dimensions, List<Block> baseBlocks,
            int chance, Heightmap.Types heightmap, int horizontalOffset, boolean randomTransform) {
        FEATURES.register(name, () -> new NaturalTreeFeature(id(template), dimensions, baseBlocks, chance,
                heightmap, horizontalOffset, randomTransform));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
