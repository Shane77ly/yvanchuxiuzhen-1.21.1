package net.yvanchuxiuzhen.system.creature.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.Yaoshou014skill03Entity;

/** 飞羽的独立 Java 模型，保留来源四个立方体及其纹理坐标、收缩量与部件姿态。 */
final class Yaoshou014FeatherModel extends EntityModel<Yaoshou014skill03Entity> {
    static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "modelyaoshou_014skill_03"), "main");
    private final ModelPart arrow;

    Yaoshou014FeatherModel(ModelPart root) {
        arrow = root.getChild("arrow");
    }

    static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition arrow = mesh.getRoot().addOrReplaceChild("arrow", CubeListBuilder.create()
                .texOffs(16, 0).addBox(-1, -14, -1, 2, 14, 2, new CubeDeformation(-0.5F))
                .texOffs(16, 0).addBox(-1, -26, -1, 2, 14, 2, new CubeDeformation(-0.75F)),
                PartPose.offset(0, 24, 0));
        arrow.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-4, -8, 0, 8, 16, 0, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0, -2, 0, 0, -0.2618F, 0));
        arrow.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-4, -8, 0, 8, 16, 0, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0, -2, 0, 0, 0.2618F, 0));
        return LayerDefinition.create(mesh, 24, 16);
    }

    @Override
    public void renderToBuffer(PoseStack poses, VertexConsumer vertices, int light, int overlay, int color) {
        arrow.render(poses, vertices, light, overlay, color);
    }

    @Override
    public void setupAnim(Yaoshou014skill03Entity entity, float limbSwing, float limbSwingAmount,
            float ageInTicks, float netHeadYaw, float headPitch) { }
}
