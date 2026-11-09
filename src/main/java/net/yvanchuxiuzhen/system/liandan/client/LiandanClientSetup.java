package net.yvanchuxiuzhen.system.liandan.client;

import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.yvanchuxiuzhen.system.liandan.content.AlchemyBlockItem;
import net.yvanchuxiuzhen.system.liandan.content.LiandanBlockEntities;
import net.yvanchuxiuzhen.system.liandan.content.LiandanMenus;

/** 炼丹系统的菜单屏幕、丹炉方块实体与物品渲染装配。 */
public final class LiandanClientSetup {
    private LiandanClientSetup() {
    }

    public static void register(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(MedicineTooltip::onTooltip);
        modBus.addListener((RegisterMenuScreensEvent event) -> event.register(LiandanMenus.ALCHEMY.get(), AlchemyScreen::new));
        modBus.addListener((RegisterMenuScreensEvent event) -> {
            event.register(LiandanMenus.MEDICINE_SEPARATION.get(), MedicineSeparationScreen::new);
            event.register(LiandanMenus.MEDICINE_COMBINATION.get(), MedicineSeparationScreen::new);
        });
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            for (var type : LiandanBlockEntities.ALCHEMY) event.registerBlockEntityRenderer(type.get(), AlchemyRenderer::new);
        });
        modBus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(() -> {
            for (var item : BuiltInRegistries.ITEM) {
                if (item instanceof AlchemyBlockItem furnace) furnace.setRenderProvider(new AlchemyItemRenderer.Provider());
            }
        }));
    }
}
