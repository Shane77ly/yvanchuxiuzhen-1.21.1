package net.yvanchuxiuzhen.system.plant.client;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.plant.AnimatedBigGrassBlockEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/** 高草动画方块沿用来源几何、数字动画与半透明材质。 */
public final class AnimatedBigGrassRenderer extends GeoBlockRenderer<AnimatedBigGrassBlockEntity> {
    public AnimatedBigGrassRenderer(BlockEntityRendererProvider.Context context) {
        super(new Model());
    }

    @Override
    public RenderType getRenderType(AnimatedBigGrassBlockEntity grass, ResourceLocation texture,
            MultiBufferSource source, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    private static final class Model extends GeoModel<AnimatedBigGrassBlockEntity> {
        @Override
        public ResourceLocation getModelResource(AnimatedBigGrassBlockEntity grass) {
            return resource("geo/big_grass_03.geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(AnimatedBigGrassBlockEntity grass) {
            return resource("textures/block/big_grass03.png");
        }

        @Override
        public ResourceLocation getAnimationResource(AnimatedBigGrassBlockEntity grass) {
            return resource("animations/big_grass_03.animation.json");
        }
    }

    private static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
