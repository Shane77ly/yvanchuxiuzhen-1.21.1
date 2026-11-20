package net.yvanchuxiuzhen.system.world;

import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;
import net.yvanchuxiuzhen.system.world.content.RiftBlock;
import net.yvanchuxiuzhen.system.world.content.RiftItem;
import net.yvanchuxiuzhen.system.world.content.SpiritCompassItem;
import net.yvanchuxiuzhen.system.world.content.WorldBlockEntities;
import net.yvanchuxiuzhen.system.world.entity.GuidanceHalo;
import net.yvanchuxiuzhen.system.world.fluid.QuicksandFluids;
import net.yvanchuxiuzhen.system.world.fluid.SpringFluids;
import net.yvanchuxiuzhen.system.world.portal.DimensionPortalBlock;
import net.yvanchuxiuzhen.system.world.portal.DimensionPortalItem;
import net.yvanchuxiuzhen.system.world.portal.DimensionPortalSpec;
import net.yvanchuxiuzhen.system.world.portal.DimensionPortalTypes;
import net.yvanchuxiuzhen.system.world.terrain.WorldTerrainTypes;
import net.yvanchuxiuzhen.system.world.tree.WorldTreeDecorators;

/**
 * 世界系统的启动装配，只在 Mod 构造时调用一次。
 */
public final class WorldSetup {
    private static final ContentTables CONTENT = new ContentTables("world")
            .itemFactory("world:spirit_compass", SpiritCompassItem::new)
            .blockFactory("world:rift", traits -> ContentBlock.create(traits, RiftBlock::new))
            .itemFactory("world:rift", (properties, tooltip) -> new RiftItem(
                    BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "liexi")), properties, tooltip))
            .blockFactory("world:dimension_portal", DimensionPortalBlock::new)
            .itemFactory("world:dimensiontianqve", (properties, tooltip) -> new DimensionPortalItem(properties, tooltip, DimensionPortalSpec.TIANQVE))
            .itemFactory("world:dimensionxiuluo", (properties, tooltip) -> new DimensionPortalItem(properties, tooltip, DimensionPortalSpec.XIULUO))
            .itemFactory("world:dimensionrenjian", (properties, tooltip) -> new DimensionPortalItem(properties, tooltip, DimensionPortalSpec.RENJIAN))
            .itemFactory("world:dimensionyaohun", (properties, tooltip) -> new DimensionPortalItem(properties, tooltip, DimensionPortalSpec.YAOHUN))
            .itemFactory("world:dimensionwugui", (properties, tooltip) -> new DimensionPortalItem(properties, tooltip, DimensionPortalSpec.WUGUI))
            .itemFactory("world:dimensiondiling", (properties, tooltip) -> new DimensionPortalItem(properties, tooltip, DimensionPortalSpec.DILING))
            .livingEntity("guanghuan", GuidanceHalo::new, GuidanceHalo::attributes);

    private WorldSetup() {
    }

    public static <T extends Entity> Supplier<EntityType<T>> entityType(String id) {
        return CONTENT.entityType(id);
    }

    public static void register(IEventBus modBus) {
        CONTENT.register(modBus);
        DimensionPortalTypes.register(modBus);
        WorldBlockEntities.register(modBus);
        modBus.addListener((RegisterSpawnPlacementsEvent event) -> event.register(EntityType.BEE,
                SpawnPlacementTypes.NO_RESTRICTIONS, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) -> true, RegisterSpawnPlacementsEvent.Operation.REPLACE));
        WorldOreFeatures.register(modBus);
        AddMissingCreatureSpawn.register(modBus);
        AddMissingBiomeFeatures.register(modBus);
        WorldInlineFeatures.register(modBus);
        WorldTreeDecorators.register(modBus);
        WorldTemplateFeatures.register(modBus);
        NativeWorldFeatures.register(modBus);
        LegacyBiomeFeatures.register(modBus);
        WorldTerrainTypes.register(modBus);
        SpringFluids.register(modBus);
        QuicksandFluids.register(modBus);
        WorldGeneration.register();
        RenjianGeneration.register();
        StructureReleases.register();
        StructureExploration.register();
    }
}
