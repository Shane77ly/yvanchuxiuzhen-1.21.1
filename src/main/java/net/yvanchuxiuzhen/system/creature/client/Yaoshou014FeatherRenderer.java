package net.yvanchuxiuzhen.system.creature.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.Yaoshou014skill03Entity;

/** 飞羽按来源使用 cutout 层与朝向插值，颜色 alpha 保持为 0.0625。 */
final class Yaoshou014FeatherRenderer extends EntityRenderer<Yaoshou014skill03Entity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ModId.VALUE, "textures/entities/yaoshou014skill03.png");
    private static final int COLOR = FastColor.ARGB32.colorFromFloat(0.0625F, 1, 1, 1);
    private final Yaoshou014FeatherModel model;

    Yaoshou014FeatherRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = new Yaoshou014FeatherModel(context.bakeLayer(Yaoshou014FeatherModel.LAYER));
    }

    @Override
    public void render(Yaoshou014skill03Entity entity, float yaw, float partialTick, PoseStack poses,
            MultiBufferSource buffers, int light) {
        VertexConsumer vertices = buffers.getBuffer(RenderType.entityCutout(getTextureLocation(entity)));
        poses.pushPose();
        poses.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, entity.yRotO, entity.getYRot()) - 90));
        poses.mulPose(Axis.ZP.rotationDegrees(90 + Mth.lerp(partialTick, entity.xRotO, entity.getXRot())));
        model.renderToBuffer(poses, vertices, light, OverlayTexture.NO_OVERLAY, COLOR);
        poses.popPose();
        super.render(entity, yaw, partialTick, poses, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(Yaoshou014skill03Entity entity) {
        return TEXTURE;
    }
}
