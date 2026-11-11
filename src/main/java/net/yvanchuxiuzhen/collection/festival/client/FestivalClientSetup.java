package net.yvanchuxiuzhen.collection.festival.client;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.yvanchuxiuzhen.collection.festival.Caishenye;
import net.yvanchuxiuzhen.collection.festival.FestivalSetup;
import net.yvanchuxiuzhen.platform.client.AnimatedEntityRenderer;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;

/** 节日内容族的实体渲染装配。 */
public final class FestivalClientSetup {
    private FestivalClientSetup() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            EntityModelRow model = FestivalSetup.caishenyeModel();
            event.registerEntityRenderer(FestivalSetup.caishenyeType().get(),
                    context -> new AnimatedEntityRenderer<>(context, model));
        });
    }
}
