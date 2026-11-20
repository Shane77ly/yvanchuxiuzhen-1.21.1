package net.yvanchuxiuzhen.system.creature.client;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.yvanchuxiuzhen.platform.client.AnimatedEntityRenderer;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.creature.CreatureSetup;
import net.yvanchuxiuzhen.system.creature.entity.JiantouEntities;
import net.yvanchuxiuzhen.system.creature.entity.JiantouEntity;

/** 箭头显示实体沿用来源半透明模型、零阴影和零死亡旋转。 */
public final class JiantouClient {
    private JiantouClient() { }

    public static void register(IEventBus modBus) {
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            EntityModelRow row = JiantouEntities.model();
            event.registerEntityRenderer(CreatureSetup.<JiantouEntity>entityType(row.id()).get(),
                    context -> new AnimatedEntityRenderer<>(context, row));
        });
    }
}
