package net.yvanchuxiuzhen.system.npc.client;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.yvanchuxiuzhen.platform.client.AnimatedEntityRenderer;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.npc.NpcSetup;
import net.yvanchuxiuzhen.system.npc.entity.NpcAnimated;
import net.yvanchuxiuzhen.system.npc.entity.NpcCreatures;
import net.yvanchuxiuzhen.system.npc.entity.NpcMob;

/** 修士模型只在物理客户端注册。 */
public final class NpcClientSetup {
    private NpcClientSetup() { }

    public static void register(IEventBus modBus) {
        modBus.addListener(NpcClientSetup::renderers);
    }

    private static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        for (EntityModelRow model : NpcCreatures.models()) {
            event.registerEntityRenderer(NpcSetup.<NpcAnimated>entityType(model.id()).get(), context -> new AnimatedEntityRenderer<>(context, model));
        }
        event.registerEntityRenderer(NpcSetup.<NpcMob>entityType("businessman_09").get(), context -> new NpcHumanoidRenderer(context, "businessman29"));
        event.registerEntityRenderer(NpcSetup.<NpcMob>entityType("businessman_10").get(), context -> new NpcHumanoidRenderer(context, "businessman27"));
        event.registerEntityRenderer(NpcSetup.<NpcMob>entityType("businessman_11").get(), context -> new NpcHumanoidRenderer(context, "businessman25"));
    }
}
