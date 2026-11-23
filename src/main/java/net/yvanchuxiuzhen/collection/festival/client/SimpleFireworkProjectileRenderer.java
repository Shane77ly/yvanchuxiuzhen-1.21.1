package net.yvanchuxiuzhen.collection.festival.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
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
import net.yvanchuxiuzhen.collection.festival.SimpleFireworkProjectileEntity;
import net.yvanchuxiuzhen.platform.ModId;

/** 窜天猴弹体的 1.18.2 原生实体模型与贴图渲染。 */
final class SimpleFireworkProjectileRenderer extends EntityRenderer<SimpleFireworkProjectileEntity> {
    static final ModelLayerLocation LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "modelpao_0801"), "main");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/entities/pao0801.png");
    private final ModelPart model;

    SimpleFireworkProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = context.bakeLayer(LAYER);
    }

    @Override
    public void render(SimpleFireworkProjectileEntity entity, float yaw, float partialTick, PoseStack poses, MultiBufferSource buffers, int light) {
        poses.pushPose();
        poses.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, entity.yRotO, entity.getYRot()) - 90));
        poses.mulPose(Axis.ZP.rotationDegrees(90 + Mth.lerp(partialTick, entity.xRotO, entity.getXRot())));
        model.render(poses, buffers.getBuffer(RenderType.entityCutout(TEXTURE)), light, OverlayTexture.NO_OVERLAY, 0x0FFFFFFF);
        poses.popPose();
        super.render(entity, yaw, partialTick, poses, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(SimpleFireworkProjectileEntity entity) {
        return TEXTURE;
    }

    static LayerDefinition layer() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("bone", CubeListBuilder.create()
                        .texOffs(0, 0).addBox(1.0F, -2.0F, 1.0F, 1.0F, 20.0F, 1.0F, CubeDeformation.NONE)
                        .texOffs(4, 0).addBox(-1.0F, 0.0F, 0.5F, 2.0F, 4.0F, 2.0F, CubeDeformation.NONE)
                        .texOffs(4, 6).addBox(-0.5F, -1.0F, 1.0F, 1.0F, 1.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offset(-1.0F, 6.0F, -1.0F));
        return LayerDefinition.create(mesh, 12, 21);
    }
}
