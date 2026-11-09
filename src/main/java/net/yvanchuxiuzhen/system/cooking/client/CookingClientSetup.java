package net.yvanchuxiuzhen.system.cooking.client;

import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.yvanchuxiuzhen.system.cooking.content.CookingBlockEntities;
import net.yvanchuxiuzhen.system.cooking.content.CookingMenus;
import net.yvanchuxiuzhen.system.cooking.content.CookingPotBlockItem;

/** 烹饪客户端装配：两种原生菜单与方块、物品 Gecko 渲染。 */
public final class CookingClientSetup {
    private CookingClientSetup() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener((RegisterMenuScreensEvent event) -> {
            event.register(CookingMenus.IRON.get(), CookingPotScreen::new);
            event.register(CookingMenus.CLAY.get(), CookingPotScreen::new);
        });
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            event.registerBlockEntityRenderer(CookingBlockEntities.IRON.get(), CookingPotRenderer::new);
            event.registerBlockEntityRenderer(CookingBlockEntities.CLAY.get(), CookingPotRenderer::new);
        });
        modBus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(() -> {
            for (var item : BuiltInRegistries.ITEM) {
                if (item instanceof CookingPotBlockItem pot) pot.setRenderProvider(new CookingPotItemRenderer.Provider());
            }
        }));
    }
}
