package net.yvanchuxiuzhen.system.creature.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.YaoshouAttributes;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.YaoshouEntity;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/** 妖兽来源模型、纹理与头部视线，按同步体型属性等比缩放。 */
public final class YaoshouRenderer<T extends YaoshouEntity> extends GeoEntityRenderer<T> {
    public YaoshouRenderer(EntityRendererProvider.Context context, EntityModelRow row) {
        super(context, new Model<>(row));
        shadowRadius = row.rendering().shadow();
    }

    @Override
    public void render(T entity, float yaw, float partialTick, PoseStack poses, MultiBufferSource buffer, int light) {
        poses.pushPose();
        float size = (float) entity.getAttributeValue(YaoshouAttributes.SIZE);
        poses.scale(size, size, size);
        super.render(entity, yaw, partialTick, poses, buffer, light);
        poses.popPose();
    }

    @Override
    public RenderType getRenderType(T entity, ResourceLocation texture, MultiBufferSource buffer, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    @Override
    protected float getDeathMaxRotation(T entity) {
        return 0;
    }

    private static final class Model<T extends YaoshouEntity> extends GeoModel<T> {
        private final EntityModelRow row;

        private Model(EntityModelRow row) {
            this.row = row;
        }

        @Override
        public ResourceLocation getModelResource(T entity) {
            return resource("geo/" + row.geo() + ".geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(T entity) {
            return resource("textures/entities/" + entity.texture() + ".png");
        }

        @Override
        public ResourceLocation getAnimationResource(T entity) {
            return resource("animations/" + row.animation() + ".animation.json");
        }

        @Override
        public void setCustomAnimations(T entity, long instanceId, AnimationState<T> state) {
            super.setCustomAnimations(entity, instanceId, state);
            if (Minecraft.getInstance().isPaused() && !entity.shouldPlayAnimsWhileGamePaused()) return;
            row.head().flatMap(this::getBone).ifPresent(head -> {
                EntityModelData look = state.getData(DataTickets.ENTITY_MODEL_DATA);
                head.setRotX(head.getRotX() + look.headPitch() * Mth.DEG_TO_RAD);
                head.setRotY(head.getRotY() + look.netHeadYaw() * Mth.DEG_TO_RAD);
            });
        }

        private static ResourceLocation resource(String path) {
            return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
        }
    }
}
