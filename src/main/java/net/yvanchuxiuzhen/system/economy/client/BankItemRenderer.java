package net.yvanchuxiuzhen.system.economy.client;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.economy.content.BankBlockItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/** 天地银行物品使用来源模型与半透明层。 */
public final class BankItemRenderer extends GeoItemRenderer<BankBlockItem> {
    public BankItemRenderer() {
        super(new Model());
    }

    @Override
    public RenderType getRenderType(BankBlockItem bank, ResourceLocation texture,
            MultiBufferSource source, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    public static final class Provider implements GeoRenderProvider {
        private BankItemRenderer renderer;

        @Override
        public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
            if (renderer == null) renderer = new BankItemRenderer();
            return renderer;
        }
    }

    private static final class Model extends GeoModel<BankBlockItem> {
        @Override
        public ResourceLocation getModelResource(BankBlockItem bank) {
            return resource("geo/tiandiyinhang.geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(BankBlockItem bank) {
            return resource("textures/block/tiandiyinhang.png");
        }

        @Override
        public ResourceLocation getAnimationResource(BankBlockItem bank) {
            return resource("animations/tiandiyinhang.animation.json");
        }
    }

    private static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
