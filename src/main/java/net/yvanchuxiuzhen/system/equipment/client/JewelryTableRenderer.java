package net.yvanchuxiuzhen.system.equipment.client;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.equipment.content.JewelryTableBlockEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoBlockRenderer;

/** 饰品制作台沿用来源几何、动画与半透明渲染层，朝向由 GeoBlockRenderer 读取方块状态。 */
public final class JewelryTableRenderer extends GeoBlockRenderer<JewelryTableBlockEntity> {
    public JewelryTableRenderer(BlockEntityRendererProvider.Context context) {
        super(new Model());
    }

    @Override
    public RenderType getRenderType(JewelryTableBlockEntity table, ResourceLocation texture,
            MultiBufferSource source, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    private static final class Model extends GeoModel<JewelryTableBlockEntity> {
        private static final ResourceLocation GEOMETRY = resource("geo/shipintai.geo.json");
        private static final ResourceLocation TEXTURE = resource("textures/block/shipintai.png");
        private static final ResourceLocation ANIMATION = resource("animations/shipintai.animation.json");

        @Override
        public ResourceLocation getModelResource(JewelryTableBlockEntity table) {
            return GEOMETRY;
        }

        @Override
        public ResourceLocation getTextureResource(JewelryTableBlockEntity table) {
            return TEXTURE;
        }

        @Override
        public ResourceLocation getAnimationResource(JewelryTableBlockEntity table) {
            return ANIMATION;
        }

        private static ResourceLocation resource(String path) {
            return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
        }
    }
}
