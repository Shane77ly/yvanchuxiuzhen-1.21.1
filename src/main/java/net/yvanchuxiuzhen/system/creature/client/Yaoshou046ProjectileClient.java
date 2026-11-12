package net.yvanchuxiuzhen.system.creature.client;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.yvanchuxiuzhen.system.creature.CreatureSetup;
import net.yvanchuxiuzhen.system.creature.entity.Yaoshou046skill01Entity;

/** 飞箭豪猪骨刺的原生模型层及渲染器装配。 */
public final class Yaoshou046ProjectileClient {
    private Yaoshou046ProjectileClient() { }

    public static void register(IEventBus modBus) {
        modBus.addListener((EntityRenderersEvent.RegisterLayerDefinitions event) ->
                event.registerLayerDefinition(Yaoshou046skill01Model.LAYER, Yaoshou046skill01Model::layer));
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> event.registerEntityRenderer(
                CreatureSetup.<Yaoshou046skill01Entity>entityType("projectile_yaoshou_046skill_01").get(), Yaoshou046skill01Renderer::new));
    }
}
