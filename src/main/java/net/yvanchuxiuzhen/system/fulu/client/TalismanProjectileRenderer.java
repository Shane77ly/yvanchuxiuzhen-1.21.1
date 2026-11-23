package net.yvanchuxiuzhen.system.fulu.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.fulu.content.TalismanProjectile;

/** 05 号剑气符与 19 号飞剑符弹体的原生 Blockbench 实体模型渲染。 */
final class TalismanProjectileRenderer extends EntityRenderer<TalismanProjectile> {
    static final ModelLayerLocation LAYER_QI_SWORD = layer("modelfulutexiao_05");
    static final ModelLayerLocation LAYER_SWORD = layer("modelfulutexiao_19");
    private static final ResourceLocation TEX_QI_SWORD = ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/entities/fulu05texiao.png");
    private static final ResourceLocation TEX_SWORD = ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/entities/fulutexiao19.png");

    private final ModelPart model;
    private final ResourceLocation texture;

    TalismanProjectileRenderer(EntityRendererProvider.Context context, TalismanProjectile.Kind kind) {
        super(context);
        boolean qi = kind == TalismanProjectile.Kind.QI_SWORD;
        model = context.bakeLayer(qi ? LAYER_QI_SWORD : LAYER_SWORD);
        texture = qi ? TEX_QI_SWORD : TEX_SWORD;
    }

    private static ModelLayerLocation layer(String id) {
        return new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, id), "main");
    }

    @Override
    public void render(TalismanProjectile entity, float yaw, float partialTick, PoseStack poses, MultiBufferSource buffers, int light) {
        poses.pushPose();
        poses.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, entity.yRotO, entity.getYRot()) - 90));
        poses.mulPose(Axis.ZP.rotationDegrees(90 + Mth.lerp(partialTick, entity.xRotO, entity.getXRot())));
        model.render(poses, buffers.getBuffer(RenderType.entityCutout(texture)), light, OverlayTexture.NO_OVERLAY, 0x0FFFFFFF);
        poses.popPose();
        super.render(entity, yaw, partialTick, poses, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(TalismanProjectile entity) {
        return texture;
    }

    static LayerDefinition qiSwordLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition main = mesh.getRoot().addOrReplaceChild("bb_main", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.0F, -8.0F, -2.0F, 8.0F, 6.0F, 4.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        main.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(16, 18).addBox(-4.0F, 0.0F, -0.5F, 4.0F, 3.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(-16.5F, -2.5F, 0.0F, 0.0F, 0.0F, -0.8727F));
        main.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(16, 18).addBox(0.0F, 0.0F, -0.5F, 4.0F, 3.0F, 1.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(16.5F, -2.5F, 0.0F, 0.0F, 0.0F, 0.8727F));
        main.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(0, 18).addBox(-5.8F, 0.2F, -1.0F, 6.0F, 4.0F, 2.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(-12.0F, -6.0F, 0.0F, 0.0F, 0.0F, -0.6109F));
        main.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(0, 18).addBox(-0.2F, 0.2F, -1.0F, 6.0F, 4.0F, 2.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(12.0F, -6.0F, 0.0F, 0.0F, 0.0F, 0.6109F));
        main.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(0, 10).addBox(-8.0F, 0.0F, -1.5F, 8.0F, 5.0F, 3.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(-4.0F, -8.0F, 0.0F, 0.0F, 0.0F, -0.2618F));
        main.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(0, 10).addBox(0.0F, 0.0F, -1.5F, 8.0F, 5.0F, 3.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(4.0F, -8.0F, 0.0F, 0.0F, 0.0F, 0.2618F));
        return LayerDefinition.create(mesh, 26, 24);
    }

    static LayerDefinition swordLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition sword = mesh.getRoot().addOrReplaceChild("sword", CubeListBuilder.create()
                        .texOffs(10, 5).addBox(-1.0F, 1.0F, -1.0F, 2.0F, 6.0F, 2.0F, CubeDeformation.NONE)
                        .texOffs(0, 17).addBox(-2.5F, -2.0F, -1.0F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.2F))
                        .texOffs(0, 21).addBox(-2.0F, 0.0F, -1.0F, 4.0F, 1.0F, 2.0F, CubeDeformation.NONE)
                        .texOffs(0, 0).addBox(-2.0F, -18.0F, -0.5F, 4.0F, 16.0F, 1.0F, CubeDeformation.NONE)
                        .texOffs(10, 0).addBox(-1.0F, -20.0F, -0.5F, 2.0F, 2.0F, 1.0F, CubeDeformation.NONE)
                        .texOffs(10, 13).addBox(-1.0F, 7.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.2F)),
                PartPose.offset(0.0F, 24.0F, 0.0F));
        sword.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(16, 0).addBox(-0.7F, -0.7F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.05F)),
                PartPose.offsetAndRotation(0.0F, -21.5F, 0.0F, 0.0F, 0.0F, 0.7854F));
        sword.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(16, 0).addBox(0.0F, -4.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.1F)),
                PartPose.offsetAndRotation(-2.0F, -18.0F, 0.0F, 0.0F, 0.0F, 0.3927F));
        sword.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(16, 0).addBox(-1.0F, -4.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.1F)),
                PartPose.offsetAndRotation(2.0F, -18.0F, 0.0F, 0.0F, 0.0F, -0.3927F));
        return LayerDefinition.create(mesh, 20, 24);
    }
}
