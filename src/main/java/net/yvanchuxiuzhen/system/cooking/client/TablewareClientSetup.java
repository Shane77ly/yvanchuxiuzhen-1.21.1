package net.yvanchuxiuzhen.system.cooking.client;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.yvanchuxiuzhen.system.cooking.content.TablewareBlockEntities;

/** 三种餐具的静态方块模型与额外库存层使用各自来源资源。 */
public final class TablewareClientSetup {
    private TablewareClientSetup() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            event.registerBlockEntityRenderer(TablewareBlockEntities.BOWL.get(), TablewareRenderer::new);
            event.registerBlockEntityRenderer(TablewareBlockEntities.PLATE.get(), TablewareRenderer::new);
            event.registerBlockEntityRenderer(TablewareBlockEntities.LARGE_BOWL.get(), TablewareRenderer::new);
        });
    }
}
