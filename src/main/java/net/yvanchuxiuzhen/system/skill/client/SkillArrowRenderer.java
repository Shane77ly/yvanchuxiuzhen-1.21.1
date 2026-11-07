package net.yvanchuxiuzhen.system.skill.client;

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
import net.yvanchuxiuzhen.system.skill.projectile.SkillProjectile;

/** 技能箭的原生模型渲染：沿箭的偏航与俯仰旋转，保留来源的 1/16 透明度。 */
final class SkillArrowRenderer extends EntityRenderer<SkillProjectile> {
    private final ResourceLocation texture;
    private final ModelPart model;

    SkillArrowRenderer(EntityRendererProvider.Context context, int kind) {
        super(context);
        model = context.bakeLayer(layer(kind));
        texture = ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/entity/skillarrow%02d01.png".formatted(kind));
    }

    static ModelLayerLocation layer(int kind) {
        return new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "skill_arrow_%02d".formatted(kind)), "main");
    }

    @Override
    public void render(SkillProjectile arrow, float yaw, float partialTick, PoseStack poses, MultiBufferSource buffers, int light) {
        poses.pushPose();
        poses.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, arrow.yRotO, arrow.getYRot()) - 90));
        poses.mulPose(Axis.ZP.rotationDegrees(90 + Mth.lerp(partialTick, arrow.xRotO, arrow.getXRot())));
        model.render(poses, buffers.getBuffer(RenderType.entityCutout(texture)), light, OverlayTexture.NO_OVERLAY, 0x0FFFFFFF);
        poses.popPose();
        super.render(arrow, yaw, partialTick, poses, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(SkillProjectile arrow) {
        return texture;
    }
}
