package net.yvanchuxiuzhen.system.combat.client;

import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.yvanchuxiuzhen.system.combat.CombatSetup;
import net.yvanchuxiuzhen.system.combat.content.CombatMenus;
import net.yvanchuxiuzhen.system.combat.content.WoodenDummyEntity;
import net.yvanchuxiuzhen.system.combat.damage.LightningLanding;

/** 战斗系统的客户端装配：木人桩、彩色闪电渲染及木人桩设置界面。 */
public final class CombatClientSetup {
    private CombatClientSetup() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(CombatClientSetup::registerRenderers);
        modBus.addListener(CombatClientSetup::registerScreens);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        EntityType<WoodenDummyEntity> dummy = CombatSetup.<WoodenDummyEntity>entityType("murenzhuang").get();
        event.registerEntityRenderer(dummy, WoodenDummyRenderer::new);
        event.registerEntityRenderer(LightningLanding.TYPE.get(), ColoredLightningRenderer::new);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(CombatMenus.DUMMY.get(), DummyScreen::new);
    }
}
