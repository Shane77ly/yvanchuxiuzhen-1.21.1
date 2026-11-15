package net.yvanchuxiuzhen.system.zhenfa.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.zhenfa.entity.FormationArrow;

/** 阳一弹体的七段立方模型，保留来源轴旋转、cutout渲染和0.0625透明度。 */
final class FormationArrowRenderer extends EntityRenderer<FormationArrow> {
    static final ModelLayerLocation LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "modelzhenjineng_01"), "main");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/entities/zhenjineng01.png");
    private final ModelPart model;

    FormationArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = context.bakeLayer(LAYER).getChild("bb_main");
    }

    @Override
    public void render(FormationArrow entity, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, entity.yRotO, entity.getYRot()) - 90));
        pose.mulPose(Axis.ZP.rotationDegrees(90 + Mth.lerp(partialTick, entity.xRotO, entity.getXRot())));
        model.render(pose, buffers.getBuffer(RenderType.entityCutout(TEXTURE)), light, OverlayTexture.NO_OVERLAY, 0x0FFFFFFF);
        pose.popPose();
        super.render(entity, yaw, partialTick, pose, buffers, light);
    }

    @Override public ResourceLocation getTextureLocation(FormationArrow entity) { return TEXTURE; }

    static LayerDefinition layer() {
        MeshDefinition mesh = new MeshDefinition();
        CubeListBuilder cubes = CubeListBuilder.create().texOffs(0, 0).addBox(-4, -8, -4, 8, 8, 8)
                .texOffs(0, 0).addBox(-3, 0, -3, 6, 6, 6)
                .texOffs(0, 0).addBox(-3, -14, -3, 6, 6, 6)
                .texOffs(0, 0).addBox(-2, -18, -2, 4, 4, 4)
                .texOffs(0, 0).addBox(-2, 6, -2, 4, 4, 4)
                .texOffs(0, 0).addBox(-1, 10, -1, 2, 2, 2)
                .texOffs(0, 0).addBox(-1, -20, -1, 2, 2, 2);
        mesh.getRoot().addOrReplaceChild("bb_main", cubes, PartPose.offset(0, 24, 0));
        return LayerDefinition.create(mesh, 32, 16);
    }
}
