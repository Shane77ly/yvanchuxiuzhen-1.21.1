package net.yvanchuxiuzhen.system.world;

import net.neoforged.bus.api.IEventBus;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.system.world.fluid.QuicksandFluids;
import net.yvanchuxiuzhen.system.world.fluid.SpringFluids;
import net.yvanchuxiuzhen.system.world.terrain.WorldTerrainTypes;
import net.yvanchuxiuzhen.system.world.tree.WorldTreeDecorators;

/**
 * 世界系统的启动装配，只在 Mod 构造时调用一次。
 */
public final class WorldSetup {
    private static final ContentTables CONTENT = new ContentTables("world");

    private WorldSetup() {
    }

    public static void register(IEventBus modBus) {
        CONTENT.register(modBus);
        modBus.addListener((RegisterSpawnPlacementsEvent event) -> event.register(EntityType.BEE,
                SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) -> true, RegisterSpawnPlacementsEvent.Operation.REPLACE));
        WorldOreFeatures.register(modBus);
        WorldInlineFeatures.register(modBus);
        WorldTreeDecorators.register(modBus);
        WorldTemplateFeatures.register(modBus);
        NativeWorldFeatures.register(modBus);
        LegacyBiomeFeatures.register(modBus);
        WorldTerrainTypes.register(modBus);
        SpringFluids.register(modBus);
        QuicksandFluids.register(modBus);
        WorldGeneration.register();
        WorldGenerationCommands.register();
    }
}
