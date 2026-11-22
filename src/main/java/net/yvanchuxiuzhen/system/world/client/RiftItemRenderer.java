package net.yvanchuxiuzhen.system.world.client;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.system.world.content.RiftItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/** 裂隙物品使用独立来源显示变换并复用方块几何。 */
final class RiftItemRenderer extends GeoItemRenderer<RiftItem> {
    RiftItemRenderer() { super(new Model()); }
    @Override public RenderType getRenderType(RiftItem item, ResourceLocation texture, MultiBufferSource buffers, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    static final class Provider implements GeoRenderProvider {
        private RiftItemRenderer renderer;
        @Override public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
            if (renderer == null) renderer = new RiftItemRenderer();
            return renderer;
        }
    }

    private static final class Model extends GeoModel<RiftItem> {
        @Override public ResourceLocation getModelResource(RiftItem item) { return RiftRenderer.resource("geo/liexi.geo.json"); }
        @Override public ResourceLocation getTextureResource(RiftItem item) { return RiftRenderer.resource("textures/block/liexi.png"); }
        @Override public ResourceLocation getAnimationResource(RiftItem item) { return RiftRenderer.resource("animations/liexi.animation.json"); }
    }
}
