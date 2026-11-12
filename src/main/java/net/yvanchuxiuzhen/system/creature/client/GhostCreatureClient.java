package net.yvanchuxiuzhen.system.creature.client;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.yvanchuxiuzhen.platform.client.AnimatedEntityRenderer;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.creature.CreatureSetup;
import net.yvanchuxiuzhen.system.creature.entity.GhostCreatures;
import net.yvanchuxiuzhen.system.creature.entity.Guiguai02Entity;
import net.yvanchuxiuzhen.system.creature.entity.Guiguai03Entity;

/** 鬼火与小尸王的来源模型、半透明纹理和鬼火发光层。 */
public final class GhostCreatureClient {
    private GhostCreatureClient() { }

    public static void register(IEventBus modBus) {
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            for (EntityModelRow row : GhostCreatures.models()) {
                switch (row.id()) {
                    case "guiguai_02" -> event.registerEntityRenderer(CreatureSetup.<Guiguai02Entity>entityType(row.id()).get(),
                            context -> new AnimatedEntityRenderer<>(context, row));
                    case "guiguai_03" -> event.registerEntityRenderer(CreatureSetup.<Guiguai03Entity>entityType(row.id()).get(),
                            context -> new AnimatedEntityRenderer<>(context, row));
                    default -> throw new IllegalStateException("未知鬼怪身份：" + row.id());
                }
            }
        });
    }
}
