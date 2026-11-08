package net.yvanchuxiuzhen.system.equipment.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.equipment.projectile.FabaoProjectile;

/** 五至八号法宝的原生实体模型渲染，保留来源的旋转顺序、裁切渲染层与透明度。 */
final class FabaoProjectileRenderer extends EntityRenderer<FabaoProjectile> {
    private final ResourceLocation texture;
    private final ModelPart model;

    FabaoProjectileRenderer(EntityRendererProvider.Context context, String modelId) {
        super(context);
        model = context.bakeLayer(layer(modelId)).getChild("zong");
        texture = ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/entities/" + modelId + "01.png");
    }

    static ModelLayerLocation layer(String modelId) {
        return new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, modelId), "main");
    }

    @Override
    public void render(FabaoProjectile projectile, float yaw, float partialTick, PoseStack poses, MultiBufferSource buffers, int light) {
        poses.pushPose();
        poses.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, projectile.yRotO, projectile.getYRot()) - 90));
        poses.mulPose(Axis.ZP.rotationDegrees(90 + Mth.lerp(partialTick, projectile.xRotO, projectile.getXRot())));
        model.render(poses, buffers.getBuffer(RenderType.entityCutout(texture)), light, OverlayTexture.NO_OVERLAY, 0x0FFFFFFF);
        poses.popPose();
        super.render(projectile, yaw, partialTick, poses, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(FabaoProjectile projectile) {
        return texture;
    }
}
