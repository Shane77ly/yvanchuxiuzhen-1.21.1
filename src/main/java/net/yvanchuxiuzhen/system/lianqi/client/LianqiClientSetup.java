package net.yvanchuxiuzhen.system.lianqi.client;

import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.yvanchuxiuzhen.system.lianqi.content.LianqiBlockEntities;
import net.yvanchuxiuzhen.system.lianqi.content.LianqiMenus;
import net.yvanchuxiuzhen.system.lianqi.content.RepairTableBlockItem;

/** 炼器装置菜单屏幕、鼎内材料与修理台动画渲染装配。 */
public final class LianqiClientSetup {
    private LianqiClientSetup() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener((RegisterMenuScreensEvent event) -> event.register(LianqiMenus.TRIPOD.get(), TripodScreen::new));
        modBus.addListener((RegisterMenuScreensEvent event) -> {
            event.register(LianqiMenus.REPAIR_TABLE.get(), RepairTableScreen::new);
            event.register(LianqiMenus.SEWING_BASKET.get(), SewingBasketScreen::new);
            event.register(LianqiMenus.BLUEPRINT_TABLE.get(), BlueprintTableScreen::new);
        });
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            for (var type : LianqiBlockEntities.TRIPODS) event.registerBlockEntityRenderer(type.get(), TripodRenderer::new);
            event.registerBlockEntityRenderer(LianqiBlockEntities.REPAIR_TABLE.get(), RepairTableRenderer::new);
        });
        modBus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(() -> {
            for (var item : BuiltInRegistries.ITEM) {
                if (item instanceof RepairTableBlockItem table) table.setRenderProvider(new RepairTableItemRenderer.Provider());
            }
        }));
    }
}
