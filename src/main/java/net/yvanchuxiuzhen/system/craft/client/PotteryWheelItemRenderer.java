package net.yvanchuxiuzhen.system.craft.client;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.craft.content.PotteryWheelBlockItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/** 陶轮物品使用来源模型与半透明层，持物变换由物品模型父链承担。 */
public final class PotteryWheelItemRenderer extends GeoItemRenderer<PotteryWheelBlockItem> {
    public PotteryWheelItemRenderer() {
        super(new Model());
    }

    @Override
    public RenderType getRenderType(PotteryWheelBlockItem wheel, ResourceLocation texture,
            MultiBufferSource source, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    public static final class Provider implements GeoRenderProvider {
        private PotteryWheelItemRenderer renderer;

        @Override
        public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
            if (renderer == null) renderer = new PotteryWheelItemRenderer();
            return renderer;
        }
    }

    private static final class Model extends GeoModel<PotteryWheelBlockItem> {
        @Override
        public ResourceLocation getModelResource(PotteryWheelBlockItem wheel) {
            return resource("geo/taolun.geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(PotteryWheelBlockItem wheel) {
            return resource("textures/block/taolun.png");
        }

        @Override
        public ResourceLocation getAnimationResource(PotteryWheelBlockItem wheel) {
            return resource("animations/taolun.animation.json");
        }
    }

    private static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
