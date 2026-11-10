package net.yvanchuxiuzhen.system.craft.client;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.craft.content.LoomBlockItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/** 织布机物品使用来源模型与半透明层，持物变换由物品模型父链承担。 */
public final class LoomItemRenderer extends GeoItemRenderer<LoomBlockItem> {
    public LoomItemRenderer() {
        super(new Model());
    }

    @Override
    public RenderType getRenderType(LoomBlockItem loom, ResourceLocation texture,
            MultiBufferSource source, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    public static final class Provider implements GeoRenderProvider {
        private LoomItemRenderer renderer;

        @Override
        public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
            if (renderer == null) renderer = new LoomItemRenderer();
            return renderer;
        }
    }

    private static final class Model extends GeoModel<LoomBlockItem> {
        @Override
        public ResourceLocation getModelResource(LoomBlockItem loom) {
            return resource("geo/zhibuji.geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(LoomBlockItem loom) {
            return resource("textures/block/zhibuji.png");
        }

        @Override
        public ResourceLocation getAnimationResource(LoomBlockItem loom) {
            return resource("animations/zhibuji.animation.json");
        }
    }

    private static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
