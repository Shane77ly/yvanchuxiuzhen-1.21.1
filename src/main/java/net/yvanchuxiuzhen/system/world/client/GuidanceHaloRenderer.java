package net.yvanchuxiuzhen.system.world.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.system.world.entity.GuidanceHalo;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

/** 光环保留空白底纹、独立发光纹理、零阴影和零死亡旋转。 */
final class GuidanceHaloRenderer extends GeoEntityRenderer<GuidanceHalo> {
    GuidanceHaloRenderer(EntityRendererProvider.Context context) {
        super(context, new Model());
        shadowRadius = 0;
        addRenderLayer(new Glow(this));
    }

    @Override public RenderType getRenderType(GuidanceHalo halo, ResourceLocation texture, MultiBufferSource buffers, float partialTick) {
        return halo.isInvisibleTo(Minecraft.getInstance().player) ? null : RenderType.entityTranslucent(texture);
    }
    @Override protected float getDeathMaxRotation(GuidanceHalo halo) { return 0; }

    private static final class Model extends GeoModel<GuidanceHalo> {
        @Override public ResourceLocation getModelResource(GuidanceHalo halo) { return RiftRenderer.resource("geo/guanghuan.geo.json"); }
        @Override public ResourceLocation getAnimationResource(GuidanceHalo halo) { return RiftRenderer.resource("animations/guanghuan.animation.json"); }
        @Override public ResourceLocation getTextureResource(GuidanceHalo halo) { return RiftRenderer.resource("textures/entities/" + halo.texture() + ".png"); }
    }

    private static final class Glow extends GeoRenderLayer<GuidanceHalo> {
        Glow(GeoEntityRenderer<GuidanceHalo> renderer) { super(renderer); }
        @Override public void render(PoseStack poses, GuidanceHalo halo, BakedGeoModel model, RenderType renderType,
                MultiBufferSource buffers, VertexConsumer buffer, float partialTick, int light, int overlay) {
            RenderType glow = RenderType.eyes(RiftRenderer.resource("textures/entities/guanghuan.png"));
            getRenderer().reRender(model, poses, buffers, halo, glow, buffers.getBuffer(glow), partialTick, light, OverlayTexture.NO_OVERLAY, -1);
        }
    }
}
