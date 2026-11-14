package net.yvanchuxiuzhen.system.economy.client;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.economy.content.BankBlockEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/** 天地银行方块使用来源几何、半透明纹理和数字动画。 */
public final class BankBlockRenderer extends GeoBlockRenderer<BankBlockEntity> {
    public BankBlockRenderer(BlockEntityRendererProvider.Context context) {
        super(new Model());
    }

    @Override
    public RenderType getRenderType(BankBlockEntity bank, ResourceLocation texture,
            MultiBufferSource source, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    private static final class Model extends GeoModel<BankBlockEntity> {
        @Override
        public ResourceLocation getModelResource(BankBlockEntity bank) {
            return resource("geo/tiandiyinhang.geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(BankBlockEntity bank) {
            return resource("textures/block/tiandiyinhang.png");
        }

        @Override
        public ResourceLocation getAnimationResource(BankBlockEntity bank) {
            return resource("animations/tiandiyinhang.animation.json");
        }
    }

    private static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
