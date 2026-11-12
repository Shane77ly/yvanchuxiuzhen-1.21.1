package net.yvanchuxiuzhen.system.plant.client;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.plant.GourdBlockItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/** 葫芦方块物品沿用来源几何与半透明材质。 */
public final class GourdItemRenderer extends GeoItemRenderer<GourdBlockItem> {
    public GourdItemRenderer() {
        super(new Model());
    }

    @Override
    public RenderType getRenderType(GourdBlockItem gourd, ResourceLocation texture,
            MultiBufferSource source, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    public static final class Provider implements GeoRenderProvider {
        private GourdItemRenderer renderer;

        @Override
        public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
            if (renderer == null) renderer = new GourdItemRenderer();
            return renderer;
        }
    }

    private static final class Model extends GeoModel<GourdBlockItem> {
        @Override
        public ResourceLocation getModelResource(GourdBlockItem gourd) {
            return resource("geo/shicai03905.geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(GourdBlockItem gourd) {
            return resource("textures/block/shicai03905.png");
        }

        @Override
        public ResourceLocation getAnimationResource(GourdBlockItem gourd) {
            return resource("animations/shicai03905.animation.json");
        }
    }

    private static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
