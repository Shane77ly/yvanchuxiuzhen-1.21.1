package net.yvanchuxiuzhen.system.creature.client;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.creature.content.CoffinBlockEntity;
import net.yvanchuxiuzhen.system.creature.content.CoffinBlockItem;
import net.yvanchuxiuzhen.system.creature.content.CoffinContent;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/** 棺材的方块实体和手持物品共用来源模型、动画及半透明纹理。 */
public final class CoffinClient {
    private CoffinClient() { }

    public static void register(IEventBus modBus) {
        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> event.registerBlockEntityRenderer(
                CoffinContent.TYPE.get(), context -> new BlockRenderer()));
        modBus.addListener((FMLClientSetupEvent event) -> event.enqueueWork(() -> {
            for (Item item : BuiltInRegistries.ITEM) {
                if (item instanceof CoffinBlockItem coffin) coffin.setRenderProvider(new ItemProvider());
            }
        }));
    }

    private static ResourceLocation resource(String path) { return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path); }

    private static final class BlockModel extends GeoModel<CoffinBlockEntity> {
        @Override public ResourceLocation getModelResource(CoffinBlockEntity block) { return resource("geo/guancai.geo.json"); }
        @Override public ResourceLocation getTextureResource(CoffinBlockEntity block) { return resource("textures/block/guancai01.png"); }
        @Override public ResourceLocation getAnimationResource(CoffinBlockEntity block) { return resource("animations/guancai.animation.json"); }
    }

    private static final class BlockRenderer extends GeoBlockRenderer<CoffinBlockEntity> {
        private BlockRenderer() { super(new BlockModel()); }
        @Override public RenderType getRenderType(CoffinBlockEntity block, ResourceLocation texture, MultiBufferSource buffers, float partialTick) {
            return RenderType.entityTranslucent(texture);
        }
    }

    private static final class ItemModel extends GeoModel<CoffinBlockItem> {
        @Override public ResourceLocation getModelResource(CoffinBlockItem item) { return resource("geo/guancai.geo.json"); }
        @Override public ResourceLocation getTextureResource(CoffinBlockItem item) { return resource("textures/block/guancai01.png"); }
        @Override public ResourceLocation getAnimationResource(CoffinBlockItem item) { return resource("animations/guancai.animation.json"); }
    }

    private static final class ItemRenderer extends GeoItemRenderer<CoffinBlockItem> {
        private ItemRenderer() { super(new ItemModel()); }
        @Override public RenderType getRenderType(CoffinBlockItem item, ResourceLocation texture, MultiBufferSource buffers, float partialTick) {
            return RenderType.entityTranslucent(texture);
        }
    }

    private static final class ItemProvider implements GeoRenderProvider {
        private ItemRenderer renderer;
        @Override public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
            if (renderer == null) renderer = new ItemRenderer();
            return renderer;
        }
    }
}
