package net.yvanchuxiuzhen.system.treasure.client;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.yvanchuxiuzhen.system.treasure.content.TreasureBlockEntities;
import net.yvanchuxiuzhen.system.treasure.content.TreasureChestBlockItem;

/** 两种宝箱独立注册方块实体渲染与手持 Geo 物品渲染。 */
public final class TreasureClientSetup {
    private TreasureClientSetup() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
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
