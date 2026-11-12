package net.yvanchuxiuzhen.system.fulu.client;

import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.platform.client.AnimatedEntityRenderer;
import net.yvanchuxiuzhen.system.fulu.FuluSetup;
import net.yvanchuxiuzhen.system.fulu.content.FuluBlockEntities;
import net.yvanchuxiuzhen.system.fulu.content.FuluMenus;
import net.yvanchuxiuzhen.system.fulu.content.TalismanProjectile.Kind;

/** 符箓系统的工作台屏幕、烧灰架渲染和成品品阶提示装配。 */
public final class FuluClientSetup {
    private FuluClientSetup() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener((RegisterMenuScreensEvent event) -> {
            event.register(FuluMenus.DRAWING_TABLE.get(), DrawingTableScreen::new);
            event.register(FuluMenus.ASH_BURNER.get(), AshBurnerScreen::new);
        });
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> event.registerBlockEntityRenderer(FuluBlockEntities.ASH_BURNER.get(), AshBurnerRenderer::new));
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            for (Kind kind : Kind.values()) {
                event.registerEntityRenderer(FuluSetup.projectile(kind).get(), ThrownItemRenderer::new);
            }
        });
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> event.registerEntityRenderer(FuluSetup.summon().get(), TalismanSummonRenderer::new));
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> FuluSetup.beasts().forEach((id, row) ->
                event.registerEntityRenderer(FuluSetup.beast(id).get(), context -> new AnimatedEntityRenderer<>(context, row))));
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) ->
                event.registerEntityRenderer(FuluSetup.cloud().get(), context -> new AnimatedEntityRenderer<>(context, FuluSetup.cloudModel())));
        NeoForge.EVENT_BUS.addListener(FuluTooltip::onTooltip);
    }
}
