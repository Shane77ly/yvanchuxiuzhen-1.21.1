package net.yvanchuxiuzhen.system.plant.client;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.yvanchuxiuzhen.system.plant.AnimatedBigGrassBlockEntities;
import net.yvanchuxiuzhen.system.plant.AnimatedBigGrassBlockItem;
import net.yvanchuxiuzhen.system.plant.GourdBlockEntities;
import net.yvanchuxiuzhen.system.plant.GourdBlockItem;

/** 植物客户端装配葫芦与高草动画方块实体和物品的来源模型渲染。 */
public final class PlantClientSetup {
    private PlantClientSetup() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            event.registerBlockEntityRenderer(GourdBlockEntities.GOURD.get(), GourdRenderer::new);
            event.registerBlockEntityRenderer(AnimatedBigGrassBlockEntities.GRASS.get(), AnimatedBigGrassRenderer::new);
        });
        modBus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(() -> {
            for (Item item : BuiltInRegistries.ITEM) {
                if (item instanceof GourdBlockItem gourd) gourd.setRenderProvider(new GourdItemRenderer.Provider());
                if (item instanceof AnimatedBigGrassBlockItem grass) grass.setRenderProvider(new AnimatedBigGrassItemRenderer.Provider());
            }
        }));
    }
}
