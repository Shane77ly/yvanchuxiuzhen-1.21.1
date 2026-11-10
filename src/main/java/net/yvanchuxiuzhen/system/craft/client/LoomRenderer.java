package net.yvanchuxiuzhen.system.craft.client;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.craft.content.LoomBlockEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/** 织布机方块使用来源几何、半透明纹理和数字动画。 */
public final class LoomRenderer extends GeoBlockRenderer<LoomBlockEntity> {
    public LoomRenderer(BlockEntityRendererProvider.Context context) {
        super(new Model());
    }

    @Override
    public RenderType getRenderType(LoomBlockEntity loom, ResourceLocation texture,
            MultiBufferSource source, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    private static final class Model extends GeoModel<LoomBlockEntity> {
        @Override
        public ResourceLocation getModelResource(LoomBlockEntity loom) {
            return resource("geo/zhibuji.geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(LoomBlockEntity loom) {
            return resource("textures/block/zhibuji.png");
        }

        @Override
        public ResourceLocation getAnimationResource(LoomBlockEntity loom) {
            return resource("animations/zhibuji.animation.json");
        }
    }

    private static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
