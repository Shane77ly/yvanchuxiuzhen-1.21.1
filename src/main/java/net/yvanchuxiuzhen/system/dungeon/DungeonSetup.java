package net.yvanchuxiuzhen.system.dungeon;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;
import net.yvanchuxiuzhen.system.dungeon.content.DungeonBlock;
import net.yvanchuxiuzhen.system.dungeon.content.DungeonBlockEntities;
import net.yvanchuxiuzhen.system.dungeon.content.DungeonMenus;
import net.yvanchuxiuzhen.system.dungeon.content.DungeonPortalBlock;
import net.yvanchuxiuzhen.system.dungeon.content.DungeonPortalItem;
import net.yvanchuxiuzhen.system.dungeon.content.PaintingItem;
import net.yvanchuxiuzhen.system.dungeon.data.DungeonData;
import net.yvanchuxiuzhen.system.dungeon.api.Dungeons;
import net.yvanchuxiuzhen.system.world.api.StructureReleasedEvent;

/**
 * 副本系统的启动装配，只在 Mod 构造时调用一次。
 */
public final class DungeonSetup {
    private static final ContentTables CONTENT = new ContentTables("dungeon")
            .blockFactory("dungeon:tiandaofuben", traits -> ContentBlock.create(traits, DungeonPortalBlock::new))
            .blockFactory("dungeon:fabao_009", traits -> ContentBlock.create(traits, DungeonPortalBlock.Painting::new))
            .blockFactory("dungeon:fubenblock", traits -> ContentBlock.create(traits, DungeonBlock::new))
            .blockFactory("dungeon:fubenentity", traits -> ContentBlock.create(traits, DungeonBlock.Marker::new))
            .itemFactory("dungeon:fabao_00901", PaintingItem::new)
            .itemFactory("dungeon:tiandaofuben", (properties, tooltip) -> new DungeonPortalItem(DungeonPlayers.block("tiandaofuben"), properties, tooltip));

    private DungeonSetup() {
    }

    public static void register(IEventBus modBus) {
        CONTENT.register(modBus);
        DungeonData.register(modBus);
        DungeonBlockEntities.register(modBus);
        DungeonMenus.register(modBus);
        NeoForge.EVENT_BUS.addListener(DungeonPlayers::tick);
        NeoForge.EVENT_BUS.addListener(DungeonPlayers::changedDimension);
        NeoForge.EVENT_BUS.addListener(DungeonPlayers::entityJoined);
        NeoForge.EVENT_BUS.addListener(DungeonController::endTick);
        NeoForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> DungeonController.clearDelayedReturns());
        NeoForge.EVENT_BUS.addListener((StructureReleasedEvent event) -> {
            if (event.structureName().equals("镇妖塔")) Dungeons.configureTower(event.level());
        });
    }
}
