package net.yvanchuxiuzhen.system.guchong.client;

import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.yvanchuxiuzhen.platform.client.AnimatedEntityRenderer;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.guchong.GuchongSetup;
import net.yvanchuxiuzhen.system.guchong.entity.GuchongCreatures;
import net.yvanchuxiuzhen.system.guchong.entity.Guchong05Entity;
import net.yvanchuxiuzhen.system.guchong.entity.GuchongEntity;
import net.yvanchuxiuzhen.system.guchong.content.GuchongMenus;
import net.yvanchuxiuzhen.system.guchong.content.GuchongFluteItem;

/** 蛊虫模型、动画、纹理和来源阴影装配。 */
public final class GuchongClientSetup {
    private GuchongClientSetup() { }

    public static void register(IEventBus modBus) {
        modBus.addListener((RegisterMenuScreensEvent event) -> {
            for (int grade = 1; grade <= 3; grade++) event.register(GuchongMenus.type(grade), TaoguanScreen::new);
            event.register(GuchongMenus.RELEASE.get(), GuchongReleaseScreen::new);
        });
        modBus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(() -> {
            for (var item : BuiltInRegistries.ITEM) {
                if (item instanceof GuchongFluteItem flute) flute.setRenderProvider(new GuchongFluteRenderer.Provider());
            }
        }));
        modBus.addListener((EntityRenderersEvent.RegisterLayerDefinitions event) ->
                event.registerLayerDefinition(Guchong05Model.LAYER, Guchong05Model::layer));
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            event.registerEntityRenderer(GuchongSetup.<Guchong05Entity>entityType("guchong_05").get(), Guchong05Renderer::new);
            for (EntityModelRow row : GuchongCreatures.models()) {
                event.registerEntityRenderer(GuchongSetup.<GuchongEntity>entityType(row.id()).get(),
                        context -> new AnimatedEntityRenderer<>(context, row));
            }
        });
    }
}

