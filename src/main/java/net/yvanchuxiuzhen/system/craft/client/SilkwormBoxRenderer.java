package net.yvanchuxiuzhen.system.craft.client;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.craft.content.SilkwormBoxBlockEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/** 蚕盒方块使用来源几何、半透明纹理和数字动画。 */
public final class SilkwormBoxRenderer extends GeoBlockRenderer<SilkwormBoxBlockEntity> {
    public SilkwormBoxRenderer(BlockEntityRendererProvider.Context context) {
        super(new Model());
    }

    @Override
    public RenderType getRenderType(SilkwormBoxBlockEntity box, ResourceLocation texture,
            MultiBufferSource source, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    private static final class Model extends GeoModel<SilkwormBoxBlockEntity> {
        @Override
        public ResourceLocation getModelResource(SilkwormBoxBlockEntity box) {
            return resource("geo/canhe.geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(SilkwormBoxBlockEntity box) {
            return resource("textures/block/canhe.png");
        }

        @Override
        public ResourceLocation getAnimationResource(SilkwormBoxBlockEntity box) {
            return resource("animations/canhe.animation.json");
        }
    }

    private static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
