package net.yvanchuxiuzhen.system.cooking.client;

import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.yvanchuxiuzhen.system.cooking.content.CleaverBlockItem;
import net.yvanchuxiuzhen.system.cooking.content.CuttingBlockEntities;

/** 装配案板食材、菜刀方块与菜刀方块物品的来源渲染。 */
public final class CuttingClientSetup {
    private CuttingClientSetup() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            event.registerBlockEntityRenderer(CuttingBlockEntities.BOARD.get(), CuttingBoardRenderer::new);
            event.registerBlockEntityRenderer(CuttingBlockEntities.CLEAVER.get(), CleaverRenderer::new);
        });
        modBus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(() -> {
            for (var item : BuiltInRegistries.ITEM) {
                if (item instanceof CleaverBlockItem cleaver) cleaver.setRenderProvider(new CleaverItemRenderer.Provider());
            }
        }));
    }
}
