package net.yvanchuxiuzhen.system.equipment.client;

import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.yvanchuxiuzhen.system.equipment.EquipmentSetup;

/** 装备弹体的客户端装配；拂尘的物品图标为空，本体不可见，轨迹由粒子显示。 */
public final class EquipmentClientSetup {
    private EquipmentClientSetup() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) ->
                event.registerEntityRenderer(EquipmentSetup.fuchenType().get(), ThrownItemRenderer::new));
    }
}
