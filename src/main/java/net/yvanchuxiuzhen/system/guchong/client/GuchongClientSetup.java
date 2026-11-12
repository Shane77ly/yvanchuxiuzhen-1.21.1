package net.yvanchuxiuzhen.system.guchong.client;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.yvanchuxiuzhen.platform.client.AnimatedEntityRenderer;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.guchong.GuchongSetup;
import net.yvanchuxiuzhen.system.guchong.entity.GuchongCreatures;
import net.yvanchuxiuzhen.system.guchong.entity.GuchongEntity;
import net.yvanchuxiuzhen.system.guchong.content.GuchongMenus;

/** 蛊虫模型、动画、纹理和来源阴影装配。 */
public final class GuchongClientSetup {
    private GuchongClientSetup() { }

    public static void register(IEventBus modBus) {
        modBus.addListener((RegisterMenuScreensEvent event) -> {
            for (int grade = 1; grade <= 3; grade++) event.register(GuchongMenus.type(grade), TaoguanScreen::new);
        });
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            for (EntityModelRow row : GuchongCreatures.models()) {
                event.registerEntityRenderer(GuchongSetup.<GuchongEntity>entityType(row.id()).get(),
                        context -> new AnimatedEntityRenderer<>(context, row));
            }
        });
    }
}
