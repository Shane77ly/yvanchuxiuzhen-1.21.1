package net.yvanchuxiuzhen.system.craft.client;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.craft.content.CarpentryBlockEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/** 木匠台方块使用来源几何、半透明纹理和数字动画。 */
public final class CarpentryRenderer extends GeoBlockRenderer<CarpentryBlockEntity> {
    public CarpentryRenderer(BlockEntityRendererProvider.Context context) {
        super(new Model());
    }

    @Override
    public RenderType getRenderType(CarpentryBlockEntity carpentry, ResourceLocation texture,
            MultiBufferSource source, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    private static final class Model extends GeoModel<CarpentryBlockEntity> {
        @Override
        public ResourceLocation getModelResource(CarpentryBlockEntity carpentry) {
            return resource("geo/mugongtai.geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(CarpentryBlockEntity carpentry) {
            return resource("textures/block/mugongtai.png");
        }

        @Override
        public ResourceLocation getAnimationResource(CarpentryBlockEntity carpentry) {
            return resource("animations/mugongtai.animation.json");
        }
    }

    private static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
