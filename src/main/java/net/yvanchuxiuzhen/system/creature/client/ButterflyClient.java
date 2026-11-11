package net.yvanchuxiuzhen.system.creature.client;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.yvanchuxiuzhen.platform.client.AnimatedEntityRenderer;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.creature.CreatureSetup;
import net.yvanchuxiuzhen.system.creature.entity.Butterflies;
import net.yvanchuxiuzhen.system.creature.entity.ButterflyEntity;

/** 按模型表装配蝴蝶的几何、动画、半透明纹理、阴影和缩放。 */
public final class ButterflyClient {
    private ButterflyClient() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            for (EntityModelRow row : Butterflies.models()) {
                event.registerEntityRenderer(CreatureSetup.<ButterflyEntity>entityType(row.id()).get(),
                        context -> new AnimatedEntityRenderer<ButterflyEntity>(context, row));
            }
        });
    }
}
