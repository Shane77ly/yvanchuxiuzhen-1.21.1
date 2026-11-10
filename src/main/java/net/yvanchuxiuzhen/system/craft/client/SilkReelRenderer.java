package net.yvanchuxiuzhen.system.craft.client;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.craft.content.SilkReelBlockEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/** 缫丝轮方块使用来源几何、半透明纹理和数字动画。 */
public final class SilkReelRenderer extends GeoBlockRenderer<SilkReelBlockEntity> {
    public SilkReelRenderer(BlockEntityRendererProvider.Context context) {
        super(new Model());
    }

    @Override
    public RenderType getRenderType(SilkReelBlockEntity reel, ResourceLocation texture,
            MultiBufferSource source, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    private static final class Model extends GeoModel<SilkReelBlockEntity> {
        @Override
        public ResourceLocation getModelResource(SilkReelBlockEntity reel) {
            return resource("geo/saosilun.geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(SilkReelBlockEntity reel) {
            return resource("textures/block/zhibuji.png");
        }

        @Override
        public ResourceLocation getAnimationResource(SilkReelBlockEntity reel) {
            return resource("animations/saosilun.animation.json");
        }
    }

    private static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
