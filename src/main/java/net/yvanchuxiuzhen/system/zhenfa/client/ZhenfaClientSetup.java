package net.yvanchuxiuzhen.system.zhenfa.client;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.yvanchuxiuzhen.platform.client.AnimatedEntityRenderer;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.zhenfa.ZhenfaSetup;
import net.yvanchuxiuzhen.system.zhenfa.entity.FormationArrow;
import net.yvanchuxiuzhen.system.zhenfa.entity.FormationFalling;
import net.yvanchuxiuzhen.system.zhenfa.entity.FormationLauncher;
import net.yvanchuxiuzhen.system.zhenfa.content.FormationDisplayItem;
import net.yvanchuxiuzhen.system.zhenfa.content.ZhenfaBlockEntities;

/** 阵法客户端装配登记方块附加展示、阳系实体渲染与模型层。 */
public final class ZhenfaClientSetup {
    private ZhenfaClientSetup() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            event.registerBlockEntityRenderer(ZhenfaBlockEntities.ELEMENT.get(), FormationElementRenderer::new);
            event.registerBlockEntityRenderer(ZhenfaBlockEntities.MOTHER.get(), FormationMarkerRenderer::new);
            event.registerBlockEntityRenderer(ZhenfaBlockEntities.SON.get(), FormationMarkerRenderer::new);
            event.registerBlockEntityRenderer(ZhenfaBlockEntities.COMPASS.get(), FormationMarkerRenderer::new);
            event.registerEntityRenderer(ZhenfaSetup.<FormationLauncher>entityType("zhenfashiti").get(), FormationLauncherRenderer::new);
            event.registerEntityRenderer(ZhenfaSetup.<FormationArrow>entityType("projectile_zhenyang_01").get(), FormationArrowRenderer::new);
            EntityModelRow falling = ZhenfaSetup.fallingModel();
            event.registerEntityRenderer(ZhenfaSetup.<FormationFalling>entityType("zhenjineng_03").get(), context -> new AnimatedEntityRenderer<>(context, falling));
        });
        modBus.addListener((EntityRenderersEvent.RegisterLayerDefinitions event) -> {
            event.registerLayerDefinition(FormationLauncherRenderer.LAYER, FormationLauncherRenderer::layer);
            event.registerLayerDefinition(FormationArrowRenderer.LAYER, FormationArrowRenderer::layer);
        });
        modBus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(() -> {
            for (Item item : BuiltInRegistries.ITEM) {
                if (item instanceof FormationDisplayItem display) display.setRenderProvider(new FormationDisplayItemRenderer.Provider());
            }
        }));
    }
}
