package net.yvanchuxiuzhen.system.craft.client;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.yvanchuxiuzhen.system.craft.content.CraftBlockEntities;
import net.yvanchuxiuzhen.system.craft.content.CraftMenus;
import net.yvanchuxiuzhen.system.craft.content.LoomBlockItem;
import net.yvanchuxiuzhen.system.craft.content.StoneMillBlockItem;

/** 工艺客户端装配研磨菜单以及各设备的方块和物品渲染。 */
public final class CraftClientSetup {
    private CraftClientSetup() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener((RegisterMenuScreensEvent event) ->
                event.register(CraftMenus.STONE_MILL.get(), StoneMillScreen::new));
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            event.registerBlockEntityRenderer(CraftBlockEntities.type("stonemill"), StoneMillRenderer::new);
            event.registerBlockEntityRenderer(CraftBlockEntities.type("stoneroller"), StoneMillRenderer::new);
            event.registerBlockEntityRenderer(CraftBlockEntities.LOOM.get(), LoomRenderer::new);
        });
        modBus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(() -> {
            for (Item item : BuiltInRegistries.ITEM) {
                if (item instanceof StoneMillBlockItem mill) mill.setRenderProvider(new StoneMillItemRenderer.Provider());
                if (item instanceof LoomBlockItem loom) loom.setRenderProvider(new LoomItemRenderer.Provider());
            }
        }));
    }
}
