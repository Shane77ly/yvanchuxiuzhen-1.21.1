package net.yvanchuxiuzhen.system.craft.client;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.craft.content.PotteryWheelBlockEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/** 陶轮方块使用来源几何、半透明纹理和数字动画。 */
public final class PotteryWheelRenderer extends GeoBlockRenderer<PotteryWheelBlockEntity> {
    public PotteryWheelRenderer(BlockEntityRendererProvider.Context context) {
        super(new Model());
    }

    @Override
    public RenderType getRenderType(PotteryWheelBlockEntity wheel, ResourceLocation texture,
            MultiBufferSource source, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    private static final class Model extends GeoModel<PotteryWheelBlockEntity> {
        @Override
        public ResourceLocation getModelResource(PotteryWheelBlockEntity wheel) {
            return resource("geo/taolun.geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(PotteryWheelBlockEntity wheel) {
            return resource("textures/block/taolun.png");
        }

        @Override
        public ResourceLocation getAnimationResource(PotteryWheelBlockEntity wheel) {
            return resource("animations/taolun.animation.json");
        }
    }

    private static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
