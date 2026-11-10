package net.yvanchuxiuzhen.system.craft.client;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.yvanchuxiuzhen.system.craft.content.CarpentryBlockItem;
import net.yvanchuxiuzhen.system.craft.content.CraftBlockEntities;
import net.yvanchuxiuzhen.system.craft.content.CraftMenus;

/** 木匠台客户端装配原生菜单以及独立Geo方块和物品渲染。 */
public final class CarpentryClientSetup {
    private CarpentryClientSetup() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener((RegisterMenuScreensEvent event) ->
                event.register(CraftMenus.CARPENTRY.get(), CarpentryScreen::new));
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) ->
                event.registerBlockEntityRenderer(CraftBlockEntities.CARPENTRY.get(), CarpentryRenderer::new));
        modBus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(() -> {
            for (Item item : BuiltInRegistries.ITEM) {
                if (item instanceof CarpentryBlockItem table) table.setRenderProvider(new CarpentryItemRenderer.Provider());
            }
        }));
    }
}
