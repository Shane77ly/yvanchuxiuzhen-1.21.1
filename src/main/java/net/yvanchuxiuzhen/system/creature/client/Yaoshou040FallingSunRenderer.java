package net.yvanchuxiuzhen.system.creature.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.Yaoshou040skill02Entity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

/** 落阳的空白半透明基础层与固定纹理发光层，使用独立几何和旋转动画。 */
final class Yaoshou040FallingSunRenderer extends GeoEntityRenderer<Yaoshou040skill02Entity> {
    private static final ResourceLocation GEOMETRY = resource("geo/yaoshou040skill02.geo.json");
    private static final ResourceLocation ANIMATION = resource("animations/yaoshou040skill02.animation.json");
    private static final ResourceLocation GLOW = resource("textures/entities/yaoshou040skill02.png");

    Yaoshou040FallingSunRenderer(EntityRendererProvider.Context context) {
        super(context, new Model());
        shadowRadius = 0.1F;
        addRenderLayer(new Glow(this));
    }

    @Override
    public RenderType getRenderType(Yaoshou040skill02Entity entity, ResourceLocation texture,
            MultiBufferSource buffers, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    @Override
    protected float getDeathMaxRotation(Yaoshou040skill02Entity entity) {
        return 0;
    }

    private static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }

    private static final class Model extends GeoModel<Yaoshou040skill02Entity> {
        @Override
        public ResourceLocation getModelResource(Yaoshou040skill02Entity entity) {
            return GEOMETRY;
        }

        @Override
        public ResourceLocation getTextureResource(Yaoshou040skill02Entity entity) {
            return resource("textures/entities/" + entity.texture() + ".png");
        }

        @Override
        public ResourceLocation getAnimationResource(Yaoshou040skill02Entity entity) {
            return ANIMATION;
        }
    }

    private static final class Glow extends GeoRenderLayer<Yaoshou040skill02Entity> {
        private Glow(GeoEntityRenderer<Yaoshou040skill02Entity> renderer) {
            super(renderer);
        }

        @Override
        public void render(PoseStack poses, Yaoshou040skill02Entity entity, BakedGeoModel model,
                RenderType renderType, MultiBufferSource buffers, VertexConsumer buffer,
                float partialTick, int light, int overlay) {
            RenderType glow = RenderType.eyes(GLOW);
            getRenderer().reRender(model, poses, buffers, entity, glow, buffers.getBuffer(glow), partialTick,
                    light, OverlayTexture.NO_OVERLAY, -1);
        }
    }
}
