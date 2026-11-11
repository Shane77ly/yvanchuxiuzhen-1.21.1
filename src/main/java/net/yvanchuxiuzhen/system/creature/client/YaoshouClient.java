package net.yvanchuxiuzhen.system.creature.client;

import net.neoforged.bus.api.IEventBus;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.creature.CreatureSetup;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.YaoshouEntity;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.YaoshouCreatures;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.Yaoshou023BaodanEntity;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.Yaoshou026huoyanEntity;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.Yaoshou028shuiqiangEntity;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.Yaoshou031WaterProjectile;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.Yaoshou013taoziwuqiEntity;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.Yaoshou014skill03Entity;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.Yaoshou040skill0301Entity;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.Yaoshou040skill02Entity;

/** 编号妖兽的GeckoLib模型、动画、纹理及阴影装配。 */
public final class YaoshouClient {
    private YaoshouClient() { }

    public static void register(IEventBus modBus) {
        modBus.addListener((EntityRenderersEvent.RegisterLayerDefinitions event) -> {
            event.registerLayerDefinition(Yaoshou014FeatherModel.LAYER, Yaoshou014FeatherModel::createBodyLayer);
        });
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            for (EntityModelRow row : YaoshouCreatures.models()) {
                event.registerEntityRenderer(CreatureSetup.<YaoshouEntity>entityType(row.id()).get(),
                        context -> new YaoshouRenderer<>(context, row));
            }
            event.registerEntityRenderer(CreatureSetup.<Yaoshou023BaodanEntity>entityType("projectile_yaoshou_023baodan").get(),
                    ThrownItemRenderer::new);
            event.registerEntityRenderer(CreatureSetup.<Yaoshou026huoyanEntity>entityType("projectile_yaoshou_026huoyan").get(),
                    ThrownItemRenderer::new);
            event.registerEntityRenderer(CreatureSetup.<Yaoshou028shuiqiangEntity>entityType("projectile_yaoshou_028shuiqiang").get(),
                    ThrownItemRenderer::new);
            event.registerEntityRenderer(CreatureSetup.<Yaoshou031WaterProjectile>entityType("projectile_yaoshou_048skill_01").get(),
                    ThrownItemRenderer::new);
            event.registerEntityRenderer(CreatureSetup.<Yaoshou013taoziwuqiEntity>entityType("projectile_yaoshou_013taoziwuqi").get(),
                    ThrownItemRenderer::new);
            event.registerEntityRenderer(CreatureSetup.<Yaoshou014skill03Entity>entityType("projectile_yaoshou_014skill_03").get(),
                    Yaoshou014FeatherRenderer::new);
            event.registerEntityRenderer(CreatureSetup.<Yaoshou040skill0301Entity>entityType("projectile_yaoshou_040skill_0301").get(),
                    ThrownItemRenderer::new);
            event.registerEntityRenderer(CreatureSetup.<Yaoshou040skill02Entity>entityType("yaoshou_040skill_02").get(),
                    Yaoshou040FallingSunRenderer::new);
        });
    }
}
