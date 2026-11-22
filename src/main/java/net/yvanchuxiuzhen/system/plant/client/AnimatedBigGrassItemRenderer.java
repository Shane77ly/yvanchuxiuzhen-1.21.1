package net.yvanchuxiuzhen.system.plant.client;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.plant.AnimatedBigGrassBlockItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/** 高草动画方块物品沿用来源几何与半透明材质。 */
public final class AnimatedBigGrassItemRenderer extends GeoItemRenderer<AnimatedBigGrassBlockItem> {
    public AnimatedBigGrassItemRenderer() {
        super(new Model());
    }

    @Override
    public RenderType getRenderType(AnimatedBigGrassBlockItem grass, ResourceLocation texture,
            MultiBufferSource source, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    public static final class Provider implements GeoRenderProvider {
        private AnimatedBigGrassItemRenderer renderer;

        @Override
        public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
            if (renderer == null) renderer = new AnimatedBigGrassItemRenderer();
            return renderer;
        }
    }

    private static final class Model extends GeoModel<AnimatedBigGrassBlockItem> {
        @Override
        public ResourceLocation getModelResource(AnimatedBigGrassBlockItem grass) {
            return resource("geo/big_grass_03.geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(AnimatedBigGrassBlockItem grass) {
            return resource("textures/block/big_grass03.png");
        }

        @Override
        public ResourceLocation getAnimationResource(AnimatedBigGrassBlockItem grass) {
            return resource("animations/big_grass_03.animation.json");
        }
    }

    private static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
