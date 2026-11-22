package net.yvanchuxiuzhen.system.treasure.client;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.treasure.content.TreasureChestBlockItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/** 宝箱物品保留来源显示父链及各自几何、纹理与动画文件。 */
public final class TreasureChestItemRenderer extends GeoItemRenderer<TreasureChestBlockItem> {
    public TreasureChestItemRenderer() {
        super(new Model());
    }

    @Override
    public RenderType getRenderType(TreasureChestBlockItem chest, ResourceLocation texture,
            MultiBufferSource source, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    public static final class Provider implements GeoRenderProvider {
        private TreasureChestItemRenderer renderer;

        @Override
        public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
            if (renderer == null) renderer = new TreasureChestItemRenderer();
            return renderer;
        }
    }

    private static final class Model extends GeoModel<TreasureChestBlockItem> {
        @Override
        public ResourceLocation getModelResource(TreasureChestBlockItem chest) {
            return resource("geo/", chest, ".geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(TreasureChestBlockItem chest) {
            return resource("textures/block/", chest, ".png");
        }

        @Override
        public ResourceLocation getAnimationResource(TreasureChestBlockItem chest) {
            return resource("animations/", chest, ".animation.json");
        }

        private static ResourceLocation resource(String prefix, TreasureChestBlockItem chest, String suffix) {
            return ResourceLocation.fromNamespaceAndPath(ModId.VALUE,
                    prefix + BuiltInRegistries.ITEM.getKey(chest).getPath() + suffix);
        }
    }
}
