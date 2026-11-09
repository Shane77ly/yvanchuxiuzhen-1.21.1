package net.yvanchuxiuzhen.system.lianqi.client;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.yvanchuxiuzhen.system.lianqi.content.LianqiBlockEntities;
import net.yvanchuxiuzhen.system.lianqi.content.LianqiMenus;

/** 炼器菜单屏幕与八鼎材料渲染装配。 */
public final class LianqiClientSetup {
    private LianqiClientSetup() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener((RegisterMenuScreensEvent event) -> event.register(LianqiMenus.TRIPOD.get(), TripodScreen::new));
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            for (var type : LianqiBlockEntities.TRIPODS) event.registerBlockEntityRenderer(type.get(), TripodRenderer::new);
        });
    }
}
