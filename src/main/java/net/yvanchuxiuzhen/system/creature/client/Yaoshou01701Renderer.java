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
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.Yaoshou01701Entity;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.YaoshouAttributes;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/** 蜕壳动物形态使用独立GeoEntity渲染器，保持与017主形态分离。 */
public final class Yaoshou01701Renderer extends GeoEntityRenderer<Yaoshou01701Entity> {
    public Yaoshou01701Renderer(EntityRendererProvider.Context context, EntityModelRow row) {
        super(context, new Model(row));
        shadowRadius = row.rendering().shadow();
    }

    @Override
    public void render(Yaoshou01701Entity entity, float yaw, float partialTick, PoseStack poses,
            MultiBufferSource buffer, int light) {
        poses.pushPose();
        float size = (float) entity.getAttributeValue(YaoshouAttributes.SIZE);
        poses.scale(size, size, size);
        super.render(entity, yaw, partialTick, poses, buffer, light);
        poses.popPose();
    }

    @Override
    public RenderType getRenderType(Yaoshou01701Entity entity, ResourceLocation texture,
            MultiBufferSource buffer, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    @Override
    protected float getDeathMaxRotation(Yaoshou01701Entity entity) {
        return 0;
    }

    private static final class Model extends GeoModel<Yaoshou01701Entity> {
        private final EntityModelRow row;

        private Model(EntityModelRow row) {
            this.row = row;
        }

        @Override
        public ResourceLocation getModelResource(Yaoshou01701Entity entity) {
            return resource("geo/" + row.geo() + ".geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(Yaoshou01701Entity entity) {
            return resource("textures/entities/" + entity.texture() + ".png");
        }

        @Override
        public ResourceLocation getAnimationResource(Yaoshou01701Entity entity) {
            return resource("animations/" + row.animation() + ".animation.json");
        }

        @Override
        public void setCustomAnimations(Yaoshou01701Entity entity, long instanceId,
                AnimationState<Yaoshou01701Entity> state) {
            super.setCustomAnimations(entity, instanceId, state);
            if (Minecraft.getInstance().isPaused()) return;
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
