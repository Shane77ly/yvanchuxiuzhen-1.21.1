package net.yvanchuxiuzhen.system.guchong.client;

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
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.guchong.entity.Guchong05Entity;

/** 荧光蛊来源原生模型：16×16贴图上的2×2×2主体及其来源离地姿态。 */
final class Guchong05Model extends EntityModel<Guchong05Entity> {
    static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "modelguchong_05"), "main");
    private final ModelPart body;

    Guchong05Model(ModelPart root) {
        body = root.getChild("body");
    }

    static LayerDefinition layer() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-1, -1, -1, 2, 2, 2, new CubeDeformation(0)), PartPose.offset(0, 16, 0));
        return LayerDefinition.create(mesh, 16, 16);
    }

    @Override
    public void renderToBuffer(PoseStack poses, VertexConsumer vertices, int light, int overlay, int color) {
        body.render(poses, vertices, light, overlay, color);
    }

    @Override
    public void setupAnim(Guchong05Entity entity, float limbSwing, float limbSwingAmount,
            float ageInTicks, float netHeadYaw, float headPitch) {
    }
}
