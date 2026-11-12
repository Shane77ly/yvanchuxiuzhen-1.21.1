package net.yvanchuxiuzhen.system.craft.client;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.craft.content.SilkwormBoxBlockItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/** 蚕盒物品使用来源模型与半透明层，持物变换由物品模型父链承担。 */
public final class SilkwormBoxItemRenderer extends GeoItemRenderer<SilkwormBoxBlockItem> {
    public SilkwormBoxItemRenderer() {
        super(new Model());
    }

    @Override
    public RenderType getRenderType(SilkwormBoxBlockItem box, ResourceLocation texture,
            MultiBufferSource source, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    public static final class Provider implements GeoRenderProvider {
        private SilkwormBoxItemRenderer renderer;

        @Override
        public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
            if (renderer == null) renderer = new SilkwormBoxItemRenderer();
            return renderer;
        }
    }

    private static final class Model extends GeoModel<SilkwormBoxBlockItem> {
        @Override
        public ResourceLocation getModelResource(SilkwormBoxBlockItem box) {
            return resource("geo/canhe.geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(SilkwormBoxBlockItem box) {
            return resource("textures/block/canhe.png");
        }

        @Override
        public ResourceLocation getAnimationResource(SilkwormBoxBlockItem box) {
            return resource("animations/canhe.animation.json");
        }
    }

    private static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
