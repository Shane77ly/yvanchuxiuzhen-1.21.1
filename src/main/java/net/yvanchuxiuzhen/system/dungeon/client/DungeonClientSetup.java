package net.yvanchuxiuzhen.system.dungeon.client;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.yvanchuxiuzhen.system.dungeon.content.DungeonBlockEntities;
import net.yvanchuxiuzhen.system.dungeon.content.DungeonMenus;
import net.yvanchuxiuzhen.system.dungeon.content.DungeonPortalItem;

/** 副本客户端装配登记四页、两个传送门和占位符文字。 */
public final class DungeonClientSetup {
    private DungeonClientSetup() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener((RegisterMenuScreensEvent event) -> {
            event.register(DungeonMenus.OPEN.get(), DungeonScreen::new);
            event.register(DungeonMenus.MANAGE.get(), DungeonScreen::new);
            event.register(DungeonMenus.CONTROLLER.get(), DungeonScreen::new);
            event.register(DungeonMenus.MARKER.get(), DungeonScreen::new);
        });
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            event.registerBlockEntityRenderer(DungeonBlockEntities.ENTRANCE.get(), DungeonPortalRenderer::new);
            event.registerBlockEntityRenderer(DungeonBlockEntities.PAINTING.get(), DungeonPortalRenderer::new);
            event.registerBlockEntityRenderer(DungeonBlockEntities.MARKER.get(), DungeonMarkerRenderer::new);
        });
        modBus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(() -> {
            for (Item item : BuiltInRegistries.ITEM) if (item instanceof DungeonPortalItem portal) portal.setRenderProvider(new DungeonPortalItemRenderer.Provider());
        }));
    }
}
