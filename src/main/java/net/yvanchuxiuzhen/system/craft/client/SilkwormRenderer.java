package net.yvanchuxiuzhen.system.craft.client;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.craft.content.SilkwormMob;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/** 蚕与蛾使用各自来源几何和动画，当前纹理由实体同步字段决定。 */
public final class SilkwormRenderer extends GeoEntityRenderer<SilkwormMob> {
    public SilkwormRenderer(EntityRendererProvider.Context context, SilkwormMob.Kind kind) {
        super(context, new Model());
        shadowRadius = kind.larva() ? 0.2F : 0.5F;
    }

    @Override
    public RenderType getRenderType(SilkwormMob entity, ResourceLocation texture, MultiBufferSource buffers, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    private static final class Model extends GeoModel<SilkwormMob> {
        @Override
        public ResourceLocation getModelResource(SilkwormMob entity) {
            return resource("geo/" + entity.kind().resource() + ".geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(SilkwormMob entity) {
            return resource("textures/entities/" + entity.texture() + ".png");
        }

        @Override
        public ResourceLocation getAnimationResource(SilkwormMob entity) {
            return resource("animations/" + entity.kind().resource() + ".animation.json");
        }
    }

    private static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
