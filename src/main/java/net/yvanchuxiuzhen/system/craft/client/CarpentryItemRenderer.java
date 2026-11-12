package net.yvanchuxiuzhen.system.craft.client;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.craft.content.CarpentryBlockItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/** 木匠台物品使用来源模型与半透明层，持物变换由物品模型父链承担。 */
public final class CarpentryItemRenderer extends GeoItemRenderer<CarpentryBlockItem> {
    public CarpentryItemRenderer() {
        super(new Model());
    }

    @Override
    public RenderType getRenderType(CarpentryBlockItem carpentry, ResourceLocation texture,
            MultiBufferSource source, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    public static final class Provider implements GeoRenderProvider {
        private CarpentryItemRenderer renderer;

        @Override
        public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
            if (renderer == null) renderer = new CarpentryItemRenderer();
            return renderer;
        }
    }

    private static final class Model extends GeoModel<CarpentryBlockItem> {
        @Override
        public ResourceLocation getModelResource(CarpentryBlockItem carpentry) {
            return resource("geo/mugongtai.geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(CarpentryBlockItem carpentry) {
            return resource("textures/block/mugongtai.png");
        }

        @Override
        public ResourceLocation getAnimationResource(CarpentryBlockItem carpentry) {
            return resource("animations/mugongtai.animation.json");
        }
    }

    private static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
