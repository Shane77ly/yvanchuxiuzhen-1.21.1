package net.yvanchuxiuzhen.system.fulu.client;

import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.yvanchuxiuzhen.system.fulu.content.InheritanceBlockItem;
import net.yvanchuxiuzhen.system.fulu.content.InheritanceContent;

/** 传承之光的管理屏幕、方块动画与物品模型渲染装配。 */
public final class InheritanceClientSetup {
    private InheritanceClientSetup() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener((RegisterMenuScreensEvent event) -> event.register(InheritanceContent.MENU.get(), InheritanceScreen::new));
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> event.registerBlockEntityRenderer(
                InheritanceContent.BLOCK_ENTITY.get(), InheritanceRenderer::new));
        modBus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(() -> {
            for (var item : BuiltInRegistries.ITEM) {
                if (item instanceof InheritanceBlockItem inheritance) inheritance.setRenderProvider(new InheritanceItemRenderer.Provider());
            }
        }));
    }
}
