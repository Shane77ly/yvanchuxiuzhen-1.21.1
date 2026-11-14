package net.yvanchuxiuzhen.system.treasure.client;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.treasure.content.TreasureChestBlockEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/** 宝箱沿用各自来源模型、动画与半透明层；朝向取方块状态。 */
public final class TreasureChestRenderer extends GeoBlockRenderer<TreasureChestBlockEntity> {
    public TreasureChestRenderer(BlockEntityRendererProvider.Context context) {
        super(new Model());
    }

    @Override
    public RenderType getRenderType(TreasureChestBlockEntity chest, ResourceLocation texture,
            MultiBufferSource source, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    private static final class Model extends GeoModel<TreasureChestBlockEntity> {
        @Override
        public ResourceLocation getModelResource(TreasureChestBlockEntity chest) {
            return resource("geo/", chest, ".geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(TreasureChestBlockEntity chest) {
            return resource("textures/block/", chest, ".png");
        }

        @Override
        public ResourceLocation getAnimationResource(TreasureChestBlockEntity chest) {
            return resource("animations/", chest, ".animation.json");
        }

        private static ResourceLocation resource(String prefix, TreasureChestBlockEntity chest, String suffix) {
            return ResourceLocation.fromNamespaceAndPath(ModId.VALUE,
                    prefix + BuiltInRegistries.BLOCK.getKey(chest.getBlockState().getBlock()).getPath() + suffix);
        }
    }
}
