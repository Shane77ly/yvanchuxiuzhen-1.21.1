package net.yvanchuxiuzhen.collection.decor.client;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.yvanchuxiuzhen.collection.decor.DecorSetup;
import net.yvanchuxiuzhen.collection.decor.DisplayFurnitureContent;
import net.yvanchuxiuzhen.collection.decor.DisplayFurnitureVariant;
import net.yvanchuxiuzhen.collection.decor.FurnitureStorageContent;
import net.yvanchuxiuzhen.collection.decor.FurnitureStorageVariant.Layout;
import net.yvanchuxiuzhen.collection.decor.InscribedDecorContent;
import net.yvanchuxiuzhen.collection.decor.InscribedDecorMenu;
import net.yvanchuxiuzhen.collection.decor.InscribedDecorVariant;
import net.yvanchuxiuzhen.collection.decor.RabbitCageBlockEntities;
import net.yvanchuxiuzhen.platform.ModId;

/** 装饰内容族的客户端装配：家具菜单、草色方块、座椅、诱饵和展示物渲染。 */
public final class DecorClientSetup {
    private static final BlockColor GRASS_COLOR = (state, level, pos, tintIndex) ->
            level != null && pos != null ? BiomeColors.getAverageGrassColor(level, pos) : GrassColor.get(0.5D, 1.0D);

    private DecorClientSetup() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(DecorClientSetup::registerBlockColors);
        modBus.addListener((RegisterMenuScreensEvent event) -> {
            for (Layout layout : Layout.values()) event.register(FurnitureStorageContent.menuType(layout), FurnitureStorageScreen::new);
            event.register(DisplayFurnitureContent.CABINET.get(), DisplayFurnitureScreen::new);
            event.register(DisplayFurnitureContent.STAND.get(), DisplayFurnitureScreen::new);
            for (InscribedDecorMenu.Kind kind : InscribedDecorMenu.Kind.values()) event.register(InscribedDecorContent.menu(kind), InscribedDecorScreen::new);
        });
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> event.registerEntityRenderer(DecorSetup.seatType(), DecorSeatRenderer::new));
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> event.registerBlockEntityRenderer(RabbitCageBlockEntities.OPEN.get(), RabbitCageRenderer::new));
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            for (DisplayFurnitureVariant variant : DisplayFurnitureVariant.values()) event.registerBlockEntityRenderer(DisplayFurnitureContent.type(variant), DisplayFurnitureRenderer::new);
            for (InscribedDecorVariant variant : InscribedDecorVariant.VALUES) event.registerBlockEntityRenderer(InscribedDecorContent.type(variant), InscribedDecorRenderer::new);
        });
        modBus.addListener((EntityRenderersEvent.RegisterLayerDefinitions event) -> event.registerLayerDefinition(DecorSeatRenderer.LAYER, DecorSeatRenderer::layer));
    }

    private static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register(GRASS_COLOR, block("cloudblock"), block("grassblock"));
    }

    private static Block block(String path) {
        return BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path));
    }
}
