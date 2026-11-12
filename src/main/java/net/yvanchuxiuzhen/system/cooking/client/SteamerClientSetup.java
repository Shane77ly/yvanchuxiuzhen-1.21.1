package net.yvanchuxiuzhen.system.cooking.client;

import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.yvanchuxiuzhen.system.cooking.content.SteamerBlockEntities;
import net.yvanchuxiuzhen.system.cooking.content.SteamerMenus;
import net.yvanchuxiuzhen.system.cooking.content.SteamerBlockItem;

/** 蒸锅与笼屉原生菜单、两种方块及物品Geo渲染装配。 */
public final class SteamerClientSetup {
    private SteamerClientSetup() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener((RegisterMenuScreensEvent event) -> {
            event.register(SteamerMenus.POT.get(), SteamerScreen::new);
            event.register(SteamerMenus.TRAY.get(), SteamerScreen::new);
        });
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            event.registerBlockEntityRenderer(SteamerBlockEntities.POT.get(), SteamerRenderer::new);
            event.registerBlockEntityRenderer(SteamerBlockEntities.TRAY.get(), SteamerRenderer::new);
        });
        modBus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(() -> {
            for (var item : BuiltInRegistries.ITEM) {
                if (item instanceof SteamerBlockItem pot) pot.setRenderProvider(new SteamerItemRenderer.Provider());
            }
        }));
    }
}
