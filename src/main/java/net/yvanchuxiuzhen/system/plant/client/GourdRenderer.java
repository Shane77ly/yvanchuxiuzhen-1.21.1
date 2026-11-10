package net.yvanchuxiuzhen.system.plant.client;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.plant.GourdBlockEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/** 葫芦果实沿用来源几何、数字动画与半透明材质。 */
public final class GourdRenderer extends GeoBlockRenderer<GourdBlockEntity> {
    public GourdRenderer(BlockEntityRendererProvider.Context context) {
        super(new Model());
    }

    @Override
    public RenderType getRenderType(GourdBlockEntity gourd, ResourceLocation texture,
            MultiBufferSource source, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    private static final class Model extends GeoModel<GourdBlockEntity> {
        @Override
        public ResourceLocation getModelResource(GourdBlockEntity gourd) {
            return resource("geo/shicai03905.geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(GourdBlockEntity gourd) {
            return resource("textures/block/shicai03905.png");
        }

        @Override
        public ResourceLocation getAnimationResource(GourdBlockEntity gourd) {
            return resource("animations/shicai03905.animation.json");
        }
    }

    private static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
