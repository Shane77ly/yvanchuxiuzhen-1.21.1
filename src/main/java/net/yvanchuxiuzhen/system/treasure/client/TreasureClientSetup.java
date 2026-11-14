package net.yvanchuxiuzhen.system.treasure.client;

import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.yvanchuxiuzhen.system.treasure.TreasureSetup;
import net.yvanchuxiuzhen.system.treasure.content.TreasureBlockEntities;
import net.yvanchuxiuzhen.system.treasure.content.TreasureChestBlockItem;
import net.yvanchuxiuzhen.system.treasure.content.TreasureMenus;

/** 两种宝箱渲染、天道设置页和设置弹体的客户端装配。 */
public final class TreasureClientSetup {
    private TreasureClientSetup() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener((RegisterMenuScreensEvent event) -> event.register(TreasureMenus.TOOL.get(), TreasureToolScreen::new));
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            event.registerEntityRenderer(TreasureSetup.projectile().get(), ThrownItemRenderer::new);
            event.registerBlockEntityRenderer(TreasureBlockEntities.CHEST_03.get(), TreasureChestRenderer::new);
            event.registerBlockEntityRenderer(TreasureBlockEntities.CHEST_11.get(), TreasureChestRenderer::new);
        });
        modBus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(() -> {
            for (Item item : BuiltInRegistries.ITEM) {
                if (item instanceof TreasureChestBlockItem chest) chest.setRenderProvider(new TreasureChestItemRenderer.Provider());
            }
        }));
    }
}
