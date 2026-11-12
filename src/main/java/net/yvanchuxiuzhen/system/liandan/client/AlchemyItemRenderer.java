package net.yvanchuxiuzhen.system.liandan.client;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.liandan.content.AlchemyBlockItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/** 丹炉背包、落地及持物显示共用来源模型，具体变换由物品模型 JSON 提供。 */
public final class AlchemyItemRenderer extends GeoItemRenderer<AlchemyBlockItem> {
    public AlchemyItemRenderer() {
        super(new Model());
    }

    @Override
    public RenderType getRenderType(AlchemyBlockItem item, ResourceLocation texture, MultiBufferSource source, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    public static final class Provider implements GeoRenderProvider {
        private AlchemyItemRenderer renderer;

        @Override
        public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
            if (renderer == null) renderer = new AlchemyItemRenderer();
            return renderer;
        }
    }

    private static final class Model extends GeoModel<AlchemyBlockItem> {
        @Override
        public ResourceLocation getModelResource(AlchemyBlockItem item) {
            return resource("geo/alchemy_0" + item.tier() + ".geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(AlchemyBlockItem item) {
            return resource("textures/block/alchemy_0" + item.tier() + ".png");
        }

        @Override
        public ResourceLocation getAnimationResource(AlchemyBlockItem item) {
            return resource("animations/alchemy_0" + item.tier() + ".animation.json");
        }

        private static ResourceLocation resource(String path) {
            return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
        }
    }
}
