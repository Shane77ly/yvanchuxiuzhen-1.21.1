package net.yvanchuxiuzhen.system.cooking.client;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.cooking.content.SteamerBlockItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/** 蒸锅与笼屉物品沿用来源几何和物品模型中的持物变换。 */
public final class SteamerItemRenderer extends GeoItemRenderer<SteamerBlockItem> {
    public SteamerItemRenderer() {
        super(new Model());
    }

    @Override
    public RenderType getRenderType(SteamerBlockItem pot, ResourceLocation texture,
            MultiBufferSource source, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    public static final class Provider implements GeoRenderProvider {
        private SteamerItemRenderer renderer;

        @Override
        public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
            if (renderer == null) renderer = new SteamerItemRenderer();
            return renderer;
        }
    }

    private static final class Model extends GeoModel<SteamerBlockItem> {
        @Override
        public ResourceLocation getModelResource(SteamerBlockItem pot) {
            return resource("geo/" + pot.id() + ".geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(SteamerBlockItem pot) {
            return resource("textures/block/foodzhengguo.png");
        }

        @Override
        public ResourceLocation getAnimationResource(SteamerBlockItem pot) {
            return resource("animations/" + pot.id() + ".animation.json");
        }
    }

    private static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
