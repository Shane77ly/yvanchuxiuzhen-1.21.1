package net.yvanchuxiuzhen.system.fulu.client;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.system.fulu.content.FuluBlockEntities;
import net.yvanchuxiuzhen.system.fulu.content.FuluMenus;

/** 符箓系统的工作台屏幕、烧灰架渲染和成品品阶提示装配。 */
public final class FuluClientSetup {
    private FuluClientSetup() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener((RegisterMenuScreensEvent event) -> {
            event.register(FuluMenus.DRAWING_TABLE.get(), DrawingTableScreen::new);
            event.register(FuluMenus.ASH_BURNER.get(), AshBurnerScreen::new);
        });
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> event.registerBlockEntityRenderer(FuluBlockEntities.ASH_BURNER.get(), AshBurnerRenderer::new));
        NeoForge.EVENT_BUS.addListener(FuluTooltip::onTooltip);
    }
}
