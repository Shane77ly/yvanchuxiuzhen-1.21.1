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
import net.yvanchuxiuzhen.system.creature.entity.xiuzhe.Xiuzhe0101Entity;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.model.data.EntityModelData;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/** 修者来源模型、动态体型纹理、头部视线与零死亡旋转。 */
public final class Xiuzhe0101Renderer extends GeoEntityRenderer<Xiuzhe0101Entity> {
    public Xiuzhe0101Renderer(EntityRendererProvider.Context context, EntityModelRow row) {
        super(context, new Model(row));
        shadowRadius = 0.5F;
    }

    @Override public void render(Xiuzhe0101Entity entity, float yaw, float partialTick, PoseStack poses, MultiBufferSource buffer, int light) {
        poses.pushPose();
        float size = (float) entity.getAttributeValue(YaoshouAttributes.SIZE);
        poses.scale(size, size, size);
        super.render(entity, yaw, partialTick, poses, buffer, light);
        poses.popPose();
    }

    @Override public RenderType getRenderType(Xiuzhe0101Entity entity, ResourceLocation texture, MultiBufferSource buffer, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    @Override protected float getDeathMaxRotation(Xiuzhe0101Entity entity) { return 0; }

    private static final class Model extends GeoModel<Xiuzhe0101Entity> {
        private final EntityModelRow row;

        private Model(EntityModelRow row) { this.row = row; }

        @Override public ResourceLocation getModelResource(Xiuzhe0101Entity entity) {
            return resource("geo/" + row.geo() + ".geo.json");
        }

        @Override public ResourceLocation getTextureResource(Xiuzhe0101Entity entity) {
            return resource("textures/entities/" + entity.texture() + ".png");
        }

        @Override public ResourceLocation getAnimationResource(Xiuzhe0101Entity entity) {
            return resource("animations/" + row.animation() + ".animation.json");
        }

        @Override public void setCustomAnimations(Xiuzhe0101Entity entity, long instanceId, AnimationState<Xiuzhe0101Entity> state) {
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
