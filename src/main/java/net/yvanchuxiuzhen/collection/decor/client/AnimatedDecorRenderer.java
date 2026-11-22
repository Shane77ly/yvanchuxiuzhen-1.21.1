package net.yvanchuxiuzhen.collection.decor.client;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.collection.decor.AnimatedDecorBlockEntity;
import net.yvanchuxiuzhen.platform.ModId;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/** 签筒和梯子共用来源几何、动画和裁切纹理渲染。 */
public final class AnimatedDecorRenderer extends GeoBlockRenderer<AnimatedDecorBlockEntity> {
    public AnimatedDecorRenderer(BlockEntityRendererProvider.Context context) { super(new Model()); }
    @Override public RenderType getRenderType(AnimatedDecorBlockEntity block, ResourceLocation texture, MultiBufferSource buffers, float partialTick) {
        return RenderType.entityCutoutNoCull(texture);
    }

    private static final class Model extends GeoModel<AnimatedDecorBlockEntity> {
        @Override public ResourceLocation getModelResource(AnimatedDecorBlockEntity block) { return resource("geo/" + block.variant().asset() + ".geo.json"); }
        @Override public ResourceLocation getAnimationResource(AnimatedDecorBlockEntity block) { return resource("animations/" + block.variant().asset() + ".animation.json"); }
        @Override public ResourceLocation getTextureResource(AnimatedDecorBlockEntity block) { return resource("textures/block/" + block.variant().asset() + ".png"); }
    }

    static ResourceLocation resource(String path) { return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path); }
}
