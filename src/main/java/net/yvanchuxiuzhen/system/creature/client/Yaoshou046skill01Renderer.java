package net.yvanchuxiuzhen.system.creature.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.creature.entity.Yaoshou046skill01Entity;

/** 骨刺模型沿插值后的箭旋转绘制，保留来源cutout层及1/16顶点透明度。 */
public final class Yaoshou046skill01Renderer extends EntityRenderer<Yaoshou046skill01Entity> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            ModId.VALUE, "textures/entities/yaoshou046skill01.png");
    private final Yaoshou046skill01Model model;

    public Yaoshou046skill01Renderer(EntityRendererProvider.Context context) {
        super(context);
        model = new Yaoshou046skill01Model(context.bakeLayer(Yaoshou046skill01Model.LAYER));
    }

    @Override
    public void render(Yaoshou046skill01Entity entity, float yaw, float partialTick, PoseStack poses,
                       MultiBufferSource buffer, int light) {
        poses.pushPose();
        poses.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, entity.yRotO, entity.getYRot()) - 90));
        poses.mulPose(Axis.ZP.rotationDegrees(90 + Mth.lerp(partialTick, entity.xRotO, entity.getXRot())));
        model.renderToBuffer(poses, buffer.getBuffer(RenderType.entityCutout(TEXTURE)), light, OverlayTexture.NO_OVERLAY, 0x0FFFFFFF);
        poses.popPose();
        super.render(entity, yaw, partialTick, poses, buffer, light);
    }

    @Override
    public ResourceLocation getTextureLocation(Yaoshou046skill01Entity entity) {
        return TEXTURE;
    }
}
