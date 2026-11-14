package net.yvanchuxiuzhen.system.zhenfa.client;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.yvanchuxiuzhen.system.zhenfa.content.FormationDisplayItem;
import net.yvanchuxiuzhen.system.zhenfa.content.ZhenfaBlockEntities;

/** 阵法客户端装配登记阵元的材料与伪装附加展示。 */
public final class ZhenfaClientSetup {
    private ZhenfaClientSetup() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            event.registerBlockEntityRenderer(ZhenfaBlockEntities.ELEMENT.get(), FormationElementRenderer::new);
            event.registerBlockEntityRenderer(ZhenfaBlockEntities.MOTHER.get(), FormationMarkerRenderer::new);
            event.registerBlockEntityRenderer(ZhenfaBlockEntities.SON.get(), FormationMarkerRenderer::new);
            event.registerBlockEntityRenderer(ZhenfaBlockEntities.COMPASS.get(), FormationMarkerRenderer::new);
        });
        modBus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(() -> {
            for (Item item : BuiltInRegistries.ITEM) {
                if (item instanceof FormationDisplayItem display) display.setRenderProvider(new FormationDisplayItemRenderer.Provider());
            }
        }));
    }
}
