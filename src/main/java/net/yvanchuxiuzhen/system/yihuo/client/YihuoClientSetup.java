package net.yvanchuxiuzhen.system.yihuo.client;

import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.yvanchuxiuzhen.system.yihuo.YihuoProjectiles;
import net.yvanchuxiuzhen.system.yihuo.YihuoProjectile;
import net.yvanchuxiuzhen.system.yihuo.YihuoSetup;

/** 异火投掷物使用原生物品投掷物渲染器，显示对应材料物品模型。 */
public final class YihuoClientSetup {
    private YihuoClientSetup() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(YihuoClientSetup::registerRenderers);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        for (int material : YihuoProjectiles.ids()) {
            event.registerEntityRenderer(YihuoSetup.<YihuoProjectile>entityType("projectile_cailiao_" + material).get(), ThrownItemRenderer::new);
        }
    }
}
