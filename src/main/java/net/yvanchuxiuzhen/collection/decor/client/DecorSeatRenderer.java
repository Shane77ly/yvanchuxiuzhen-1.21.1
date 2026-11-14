package net.yvanchuxiuzhen.collection.decor.client;

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
import net.yvanchuxiuzhen.collection.decor.DecorSeat;
import net.yvanchuxiuzhen.platform.ModId;

/** 来源实体椅子的40×20贴图、10像素立方体与0.5阴影。 */
final class DecorSeatRenderer extends MobRenderer<DecorSeat, DecorSeatRenderer.Model> {
    static final ModelLayerLocation LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "modelshiti_yizi"), "main");
    DecorSeatRenderer(EntityRendererProvider.Context context) { super(context, new Model(context.bakeLayer(LAYER)), 0.5F); }
    @Override public ResourceLocation getTextureLocation(DecorSeat entity) { return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/entities/shiti_yizi.png"); }

    static LayerDefinition layer() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("bb_main", CubeListBuilder.create().texOffs(0, 0).addBox(-5, -10, -5, 10, 10, 10), PartPose.offset(0, 24, 0));
        return LayerDefinition.create(mesh, 40, 20);
    }
    static final class Model extends EntityModel<DecorSeat> {
        private final ModelPart root;
        Model(ModelPart root) { this.root = root.getChild("bb_main"); }
        @Override public void renderToBuffer(PoseStack pose, VertexConsumer vertex, int light, int overlay, int color) { root.render(pose, vertex, light, overlay, color); }
        @Override public void setupAnim(DecorSeat entity, float swing, float amount, float age, float yaw, float pitch) { }
    }
}
