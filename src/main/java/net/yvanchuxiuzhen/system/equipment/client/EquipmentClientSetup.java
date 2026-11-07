package net.yvanchuxiuzhen.system.equipment.client;

import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.system.equipment.EquipmentSetup;
import net.yvanchuxiuzhen.system.equipment.content.DemonArmorItem;
import net.yvanchuxiuzhen.system.equipment.content.EquipmentMenus;
import net.yvanchuxiuzhen.system.equipment.content.ToolMachineEntity;
import net.yvanchuxiuzhen.platform.client.AnimatedEntityRenderer;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;

/** 装备弹体的客户端装配；拂尘的物品图标为空，本体不可见，轨迹由粒子显示。 */
public final class EquipmentClientSetup {
    private EquipmentClientSetup() {
    }

    public static void register(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(EquipmentTooltip::tooltip);
        modBus.addListener((RegisterMenuScreensEvent event) -> event.register(EquipmentMenus.ACCESSORIES.get(), AccessoryScreen::new));
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) ->
                event.registerEntityRenderer(EquipmentSetup.fuchenType().get(), ThrownItemRenderer::new));
        modBus.addListener(EquipmentClientSetup::registerToolRenderers);
        modBus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(() -> {
            for (var item : BuiltInRegistries.ITEM) {
                if (item instanceof DemonArmorItem armor) armor.setRenderProvider(new DemonArmorRenderer.Provider());
            }
        }));
    }

    private static void registerToolRenderers(EntityRenderersEvent.RegisterRenderers event) {
        for (ToolMachineEntity.Kind kind : ToolMachineEntity.Kind.values()) {
            EntityModelRow row = EquipmentModels.model(kind.id());
            event.registerEntityRenderer(EquipmentSetup.toolMachineType(kind).get(), context -> new AnimatedEntityRenderer<>(context, row));
        }
        event.registerEntityRenderer(EquipmentSetup.kailingProjectileType().get(), ThrownItemRenderer::new);
    }
}
