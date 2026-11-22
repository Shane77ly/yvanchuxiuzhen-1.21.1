package net.yvanchuxiuzhen.system.world.client;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.world.content.RiftBlockEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/** 裂隙方块以来源几何和半透明纹理播放封印动画。 */
final class RiftRenderer extends GeoBlockRenderer<RiftBlockEntity> {
    RiftRenderer(BlockEntityRendererProvider.Context context) { super(new Model()); }
    @Override public RenderType getRenderType(RiftBlockEntity block, ResourceLocation texture, MultiBufferSource buffers, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    private static final class Model extends GeoModel<RiftBlockEntity> {
        @Override public ResourceLocation getModelResource(RiftBlockEntity block) { return resource("geo/liexi.geo.json"); }
        @Override public ResourceLocation getTextureResource(RiftBlockEntity block) { return resource("textures/block/liexi.png"); }
        @Override public ResourceLocation getAnimationResource(RiftBlockEntity block) { return resource("animations/liexi.animation.json"); }
    }

    static ResourceLocation resource(String path) { return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path); }
}
