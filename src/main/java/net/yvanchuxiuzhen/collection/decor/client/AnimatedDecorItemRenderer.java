package net.yvanchuxiuzhen.collection.decor.client;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.collection.decor.AnimatedDecorItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/** 签筒与梯子物品保持来源的三维显示变换和静态初始姿态。 */
public final class AnimatedDecorItemRenderer extends GeoItemRenderer<AnimatedDecorItem> {
    public AnimatedDecorItemRenderer() { super(new Model()); }
    @Override public RenderType getRenderType(AnimatedDecorItem item, ResourceLocation texture, MultiBufferSource buffers, float partialTick) {
        return RenderType.entityCutoutNoCull(texture);
    }

    public static final class Provider implements GeoRenderProvider {
        private AnimatedDecorItemRenderer renderer;
        @Override public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
            if (renderer == null) renderer = new AnimatedDecorItemRenderer();
            return renderer;
        }
    }

    private static final class Model extends GeoModel<AnimatedDecorItem> {
        @Override public ResourceLocation getModelResource(AnimatedDecorItem item) { return AnimatedDecorRenderer.resource("geo/" + item.variant().asset() + ".geo.json"); }
        @Override public ResourceLocation getAnimationResource(AnimatedDecorItem item) { return AnimatedDecorRenderer.resource("animations/" + item.variant().asset() + ".animation.json"); }
        @Override public ResourceLocation getTextureResource(AnimatedDecorItem item) { return AnimatedDecorRenderer.resource("textures/block/" + item.variant().asset() + ".png"); }
    }
}
