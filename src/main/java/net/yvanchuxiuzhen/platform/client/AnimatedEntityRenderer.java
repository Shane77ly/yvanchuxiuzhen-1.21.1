package net.yvanchuxiuzhen.platform.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.entity.AnimatedLook;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.util.Color;

/**
 * 动画实体的渲染（来源各 Renderer、Model 与 Layer）：GeckoLib 几何与动画由模型表给出，
 * 纹理是实体当前的纹理名（{@code textures/entities/<名>.png}），半透明渲染；有头部骨骼的实体让它随视线转动；
 * 有发光层的实体再用 {@code RenderType.eyes} 以固定纹理画一遍同一几何。阴影、缩放与死亡旋转按来源配置。
 */
public final class AnimatedEntityRenderer<T extends Entity & GeoAnimatable & AnimatedLook> extends GeoEntityRenderer<T> {
    private final EntityModelRow.Rendering rendering;

    public AnimatedEntityRenderer(EntityRendererProvider.Context context, EntityModelRow row) {
        super(context, new Model<>(row));
        rendering = row.rendering();
        shadowRadius = rendering.shadow();
        withScale(rendering.scale());
        row.glow().ifPresent(glow -> addRenderLayer(new Glow<>(this, entityTexture(glow))));
    }

    private static ResourceLocation entityTexture(String name) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/entities/" + name + ".png");
    }

    @Override
    public RenderType getRenderType(T entity, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return entity.isInvisibleTo(Minecraft.getInstance().player) ? null : RenderType.entityTranslucent(texture);
    }

    @Override
    public Color getRenderColor(T entity, float partialTick, int packedLight) {
        return Color.WHITE;
    }

    @Override
    protected float getDeathMaxRotation(T entity) {
        return rendering.deathRotation().orElse(super.getDeathMaxRotation(entity));
    }

    private static final class Model<T extends Entity & GeoAnimatable & AnimatedLook> extends GeoModel<T> {
        private final EntityModelRow row;

        Model(EntityModelRow row) {
            this.row = row;
        }

        @Override
        public ResourceLocation getModelResource(T entity) {
            return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "geo/" + row.geo() + ".geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(T entity) {
            return entityTexture(entity.texture());
        }

        @Override
        public ResourceLocation getAnimationResource(T entity) {
            return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "animations/" + row.animation() + ".animation.json");
        }

        @Override
        public void setCustomAnimations(T animatable, long instanceId, AnimationState<T> state) {
            super.setCustomAnimations(animatable, instanceId, state);
            if (Minecraft.getInstance().isPaused() && !animatable.shouldPlayAnimsWhileGamePaused()) {
                return;
            }
            row.head().flatMap(this::getBone).ifPresent(head -> {
                EntityModelData data = state.getData(DataTickets.ENTITY_MODEL_DATA);
                head.setRotX(head.getRotX() + data.headPitch() * Mth.DEG_TO_RAD);
                head.setRotY(head.getRotY() + data.netHeadYaw() * Mth.DEG_TO_RAD);
            });
        }
    }

    /** 发光层：以固定纹理、{@code eyes} 渲染类型重画几何。 */
    private static final class Glow<T extends Entity & GeoAnimatable & AnimatedLook> extends GeoRenderLayer<T> {
        private final ResourceLocation texture;

        Glow(GeoEntityRenderer<T> renderer, ResourceLocation texture) {
            super(renderer);
            this.texture = texture;
        }

        @Override
        public void render(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource,
                VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
            RenderType glow = RenderType.eyes(texture);
            getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, glow, bufferSource.getBuffer(glow), partialTick, packedLight,
                    OverlayTexture.NO_OVERLAY, -1);
        }
    }
}
