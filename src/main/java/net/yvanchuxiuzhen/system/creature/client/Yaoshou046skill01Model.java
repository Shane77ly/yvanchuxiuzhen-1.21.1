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
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.creature.entity.Yaoshou046skill01Entity;

/** 飞箭豪猪骨刺的两个原生立方体与16×16纹理布局。 */
public final class Yaoshou046skill01Model extends EntityModel<Yaoshou046skill01Entity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "modelyaoshou_046skill_01"), "main");
    private final ModelPart root;

    public Yaoshou046skill01Model(ModelPart root) {
        this.root = root.getChild("bb_main");
    }

    public static LayerDefinition layer() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("bb_main", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1, -14, -1, 2, 14, 2, new CubeDeformation(0))
                .texOffs(9, 1).addBox(-0.5F, -28, -0.5F, 1, 14, 1, new CubeDeformation(0)),
                PartPose.offset(0, 24, 0));
        return LayerDefinition.create(mesh, 16, 16);
    }

    @Override
    public void renderToBuffer(PoseStack poses, VertexConsumer buffer, int light, int overlay, int color) {
        root.render(poses, buffer, light, overlay, color);
    }

    @Override
    public void setupAnim(Yaoshou046skill01Entity entity, float limbSwing, float limbAmount, float age, float yaw, float pitch) {
    }
}
