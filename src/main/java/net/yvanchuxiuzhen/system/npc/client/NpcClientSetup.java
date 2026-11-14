package net.yvanchuxiuzhen.system.npc.client;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.yvanchuxiuzhen.platform.client.AnimatedEntityRenderer;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.npc.NpcSetup;
import net.yvanchuxiuzhen.system.npc.entity.NpcAnimated;
import net.yvanchuxiuzhen.system.npc.entity.NpcCreatures;
import net.yvanchuxiuzhen.system.npc.entity.NpcFlyingShuttle;
import net.yvanchuxiuzhen.system.npc.entity.NpcMob;
import net.yvanchuxiuzhen.system.npc.entity.NpcResident;
import net.yvanchuxiuzhen.system.npc.entity.NpcResidentAnimated;
import net.yvanchuxiuzhen.system.npc.entity.NpcResidents;

/** 修士模型只在物理客户端注册。 */
public final class NpcClientSetup {
    private NpcClientSetup() { }

    public static void register(IEventBus modBus) {
        modBus.addListener(NpcClientSetup::renderers);
    }

    private static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(NpcSetup.<NpcFlyingShuttle>entityType("businessman_08").get(), context -> new AnimatedEntityRenderer<>(context, NpcFlyingShuttle.model()));
        for (EntityModelRow model : NpcCreatures.models()) {
            event.registerEntityRenderer(NpcSetup.<NpcAnimated>entityType(model.id()).get(), context -> new AnimatedEntityRenderer<>(context, model));
        }
        event.registerEntityRenderer(NpcSetup.<NpcMob>entityType("businessman_09").get(), context -> new NpcHumanoidRenderer<>(context, "businessman29"));
        event.registerEntityRenderer(NpcSetup.<NpcMob>entityType("businessman_10").get(), context -> new NpcHumanoidRenderer<>(context, "businessman27"));
        event.registerEntityRenderer(NpcSetup.<NpcMob>entityType("businessman_11").get(), context -> new NpcHumanoidRenderer<>(context, "businessman25"));
        for (EntityModelRow model : NpcResidents.models()) {
            event.registerEntityRenderer(NpcSetup.<NpcResidentAnimated>entityType(model.id()).get(), context -> new AnimatedEntityRenderer<>(context, model));
        }
        for (int index = 23; index <= 30; index++) {
            String skin = "businessman" + index;
            event.registerEntityRenderer(NpcSetup.<NpcResident>entityType("businessman_" + index).get(), context -> new NpcHumanoidRenderer<>(context, skin));
        }
    }
}
