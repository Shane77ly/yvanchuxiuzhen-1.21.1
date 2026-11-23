package net.yvanchuxiuzhen.collection.festival.client;

import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.yvanchuxiuzhen.collection.festival.FestivalSetup;
import net.yvanchuxiuzhen.collection.festival.FortuneCostumeItem;
import net.yvanchuxiuzhen.collection.festival.ThrownFirework;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.yvanchuxiuzhen.platform.client.AnimatedEntityRenderer;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;

/** 节日内容族的实体渲染装配。 */
public final class FestivalClientSetup {
    private FestivalClientSetup() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(() -> {
            for (var item : BuiltInRegistries.ITEM) {
                if (item instanceof FortuneCostumeItem armor) armor.setRenderProvider(new FortuneCostumeRenderer.Provider());
            }
        }));
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            event.registerEntityRenderer(FestivalSetup.doubleBangType().get(), DoubleBangFireworkRenderer::new);
            event.registerEntityRenderer(FestivalSetup.pao01Type().get(), ThrownItemRenderer::new);
            event.registerEntityRenderer(FestivalSetup.pao08Type().get(), SimpleFireworkProjectileRenderer::new);
            event.registerEntityRenderer(FestivalSetup.pao13Type().get(), ThrownItemRenderer::new);
            EntityModelRow model = FestivalSetup.caishenyeModel();
            event.registerEntityRenderer(FestivalSetup.caishenyeType().get(),
                    context -> new AnimatedEntityRenderer<>(context, model));
            EntityModelRow lantern = FestivalSetup.wishLanternModel();
            event.registerEntityRenderer(FestivalSetup.wishLanternType().get(),
                    context -> new AnimatedEntityRenderer<>(context, lantern));
            for (ThrownFirework.Kind kind : ThrownFirework.Kind.values()) {
                EntityModelRow firework = FestivalSetup.thrownFireworkModels().stream()
                        .filter(row -> row.id().equals(kind.id())).findFirst().orElseThrow();
                event.registerEntityRenderer(FestivalSetup.thrownFireworkType(kind).get(),
                        context -> new AnimatedEntityRenderer<>(context, firework));
            }
        });
        modBus.addListener((EntityRenderersEvent.RegisterLayerDefinitions event) -> {
            event.registerLayerDefinition(DoubleBangFireworkRenderer.LAYER, DoubleBangFireworkRenderer::layer);
            event.registerLayerDefinition(SimpleFireworkProjectileRenderer.LAYER, SimpleFireworkProjectileRenderer::layer);
        });
    }
}
