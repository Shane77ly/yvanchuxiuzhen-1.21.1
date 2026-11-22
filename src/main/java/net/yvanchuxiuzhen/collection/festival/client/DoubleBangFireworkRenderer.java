package net.yvanchuxiuzhen.collection.festival.client;

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
import net.yvanchuxiuzhen.collection.festival.DoubleBangFirework;
import net.yvanchuxiuzhen.platform.ModId;

/** 二踢脚原生模型：4×10×4像素主体、16×16贴图与0.1阴影。 */
final class DoubleBangFireworkRenderer extends MobRenderer<DoubleBangFirework, DoubleBangFireworkRenderer.Model> {
    static final ModelLayerLocation LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "modelpao_0901"), "main");

    DoubleBangFireworkRenderer(EntityRendererProvider.Context context) {
        super(context, new Model(context.bakeLayer(LAYER)), .1F);
    }

    @Override
    public ResourceLocation getTextureLocation(DoubleBangFirework entity) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/entities/pao0901.png");
    }

    static LayerDefinition layer() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("pao0901", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-2, -10, -2, 4, 10, 4), PartPose.offset(0, 24, 0));
        return LayerDefinition.create(mesh, 16, 16);
    }

    static final class Model extends EntityModel<DoubleBangFirework> {
        private final ModelPart root;

        Model(ModelPart root) {
            this.root = root.getChild("pao0901");
        }

        @Override
        public void renderToBuffer(PoseStack pose, VertexConsumer vertex, int light, int overlay, int color) {
            root.render(pose, vertex, light, overlay, color);
        }

        @Override
        public void setupAnim(DoubleBangFirework entity, float swing, float amount, float age, float yaw, float pitch) {
        }
    }
}
