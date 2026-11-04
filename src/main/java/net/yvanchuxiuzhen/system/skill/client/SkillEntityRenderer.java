package net.yvanchuxiuzhen.system.skill.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.skill.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.skill.entity.SkillEntity;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

/**
 * 技能动画实体的渲染（来源各 {@code SkillXxxRenderer}、{@code SkillXxxModel}、{@code SkillXxxLayer}）：GeckoLib 几何与动画由模型表给出，
 * 纹理是实体当前的纹理名（{@code textures/entities/<名>.png}），半透明渲染；有头部骨骼的实体让它随视线转动；
 * 有发光层的实体再用 {@code RenderType.eyes} 以固定纹理画一遍同一几何。
 */
final class SkillEntityRenderer extends GeoEntityRenderer<SkillEntity> {
    SkillEntityRenderer(EntityRendererProvider.Context context, EntityModelRow row) {
        super(context, new Model(row));
        shadowRadius = row.shadow();
        row.glow().ifPresent(glow -> addRenderLayer(new Glow(this, entityTexture(glow))));
    }

    private static ResourceLocation entityTexture(String name) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/entities/" + name + ".png");
    }

    @Override
    public RenderType getRenderType(SkillEntity entity, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    private static final class Model extends GeoModel<SkillEntity> {
        private final EntityModelRow row;

        Model(EntityModelRow row) {
            this.row = row;
        }

        @Override
        public ResourceLocation getModelResource(SkillEntity entity) {
            return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "geo/" + row.geo() + ".geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(SkillEntity entity) {
            return entityTexture(entity.texture());
        }

        @Override
        public ResourceLocation getAnimationResource(SkillEntity entity) {
            return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "animations/" + row.animation() + ".animation.json");
        }

        @Override
        public void setCustomAnimations(SkillEntity animatable, long instanceId, AnimationState<SkillEntity> state) {
            super.setCustomAnimations(animatable, instanceId, state);
            row.head().flatMap(this::getBone).ifPresent(head -> {
                EntityModelData data = state.getData(DataTickets.ENTITY_MODEL_DATA);
                head.setRotX(head.getRotX() + data.headPitch() * Mth.DEG_TO_RAD);
                head.setRotY(head.getRotY() + data.netHeadYaw() * Mth.DEG_TO_RAD);
            });
        }
    }

    /** 发光层：以固定纹理、{@code eyes} 渲染类型重画几何。 */
    private static final class Glow extends GeoRenderLayer<SkillEntity> {
        private final ResourceLocation texture;

        Glow(GeoEntityRenderer<SkillEntity> renderer, ResourceLocation texture) {
            super(renderer);
            this.texture = texture;
        }

        @Override
        public void render(PoseStack poseStack, SkillEntity animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource,
                VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
            RenderType glow = RenderType.eyes(texture);
            getRenderer().reRender(bakedModel, poseStack, bufferSource, animatable, glow, bufferSource.getBuffer(glow), partialTick, packedLight,
                    OverlayTexture.NO_OVERLAY, -1);
        }
    }
}
