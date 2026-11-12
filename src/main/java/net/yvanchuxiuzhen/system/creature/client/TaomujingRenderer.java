package net.yvanchuxiuzhen.system.creature.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.creature.entity.TaomujingEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/** 桃木精按同步体型属性等比绘制来源模型。 */
public final class TaomujingRenderer extends GeoEntityRenderer<TaomujingEntity> {
    public TaomujingRenderer(EntityRendererProvider.Context context, EntityModelRow row) {
        super(context, new Model(row));
        shadowRadius = row.rendering().shadow();
    }

    @Override
    public void render(TaomujingEntity entity, float yaw, float partialTick, PoseStack poses, MultiBufferSource buffer, int light) {
        poses.pushPose();
        float scale = entity.getScale();
        poses.scale(scale, scale, scale);
        super.render(entity, yaw, partialTick, poses, buffer, light);
        poses.popPose();
    }

    @Override
    public RenderType getRenderType(TaomujingEntity entity, ResourceLocation texture, MultiBufferSource buffer, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    @Override
    protected float getDeathMaxRotation(TaomujingEntity entity) { return 0; }

    private static final class Model extends GeoModel<TaomujingEntity> {
        private final EntityModelRow row;
        private Model(EntityModelRow row) { this.row = row; }
        @Override public ResourceLocation getModelResource(TaomujingEntity entity) { return resource("geo/" + row.geo() + ".geo.json"); }
        @Override public ResourceLocation getTextureResource(TaomujingEntity entity) { return resource("textures/entities/" + entity.texture() + ".png"); }
        @Override public ResourceLocation getAnimationResource(TaomujingEntity entity) { return resource("animations/" + row.animation() + ".animation.json"); }
        private static ResourceLocation resource(String path) { return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path); }
    }
}
