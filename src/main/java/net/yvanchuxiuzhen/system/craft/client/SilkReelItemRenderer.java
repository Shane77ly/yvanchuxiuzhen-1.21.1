package net.yvanchuxiuzhen.system.craft.client;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.craft.content.SilkReelBlockItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/** 缫丝轮物品使用来源模型与半透明层，持物变换由物品模型父链承担。 */
public final class SilkReelItemRenderer extends GeoItemRenderer<SilkReelBlockItem> {
    public SilkReelItemRenderer() {
        super(new Model());
    }

    @Override
    public RenderType getRenderType(SilkReelBlockItem reel, ResourceLocation texture,
            MultiBufferSource source, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    public static final class Provider implements GeoRenderProvider {
        private SilkReelItemRenderer renderer;

        @Override
        public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
            if (renderer == null) renderer = new SilkReelItemRenderer();
            return renderer;
        }
    }

    private static final class Model extends GeoModel<SilkReelBlockItem> {
        @Override
        public ResourceLocation getModelResource(SilkReelBlockItem reel) {
            return resource("geo/saosilun.geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(SilkReelBlockItem reel) {
            return resource("textures/block/zhibuji.png");
        }

        @Override
        public ResourceLocation getAnimationResource(SilkReelBlockItem reel) {
            return resource("animations/saosilun.animation.json");
        }
    }

    private static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
