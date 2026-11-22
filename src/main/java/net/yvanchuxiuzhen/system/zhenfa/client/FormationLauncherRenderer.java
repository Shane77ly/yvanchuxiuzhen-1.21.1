package net.yvanchuxiuzhen.system.zhenfa.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.zhenfa.entity.FormationLauncher;

/** 阳一发射体原生模型：2像素立方体、8×4贴图，无阴影。 */
final class FormationLauncherRenderer extends MobRenderer<FormationLauncher, FormationLauncherRenderer.Model> {
    static final ModelLayerLocation LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "modelzhenfashiti"), "main");

    FormationLauncherRenderer(EntityRendererProvider.Context context) {
        super(context, new Model(context.bakeLayer(LAYER)), 0);
    }

    @Override public ResourceLocation getTextureLocation(FormationLauncher entity) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/entities/zhenfashiti.png");
    }

    static LayerDefinition layer() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-1, -9, -1, 2, 2, 2), PartPose.offset(0, 24, 0));
        return LayerDefinition.create(mesh, 8, 4);
    }

    static final class Model extends EntityModel<FormationLauncher> {
        private final ModelPart root;
        Model(ModelPart root) { this.root = root.getChild("bb_main"); }
        @Override public void renderToBuffer(PoseStack pose, VertexConsumer vertex, int light, int overlay, int color) {
            root.render(pose, vertex, light, overlay, color);
        }
        @Override public void setupAnim(FormationLauncher entity, float swing, float amount, float age, float yaw, float pitch) { }
    }
}
