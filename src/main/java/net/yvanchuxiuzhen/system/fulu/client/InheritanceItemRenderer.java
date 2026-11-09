package net.yvanchuxiuzhen.system.fulu.client;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.fulu.content.InheritanceBlockItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/** 传承之光背包、落地及持物显示共用来源模型，变换由物品模型定义。 */
public final class InheritanceItemRenderer extends GeoItemRenderer<InheritanceBlockItem> {
    public InheritanceItemRenderer() {
        super(new Model());
    }

    @Override
    public RenderType getRenderType(InheritanceBlockItem item, ResourceLocation texture, MultiBufferSource source, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    public static final class Provider implements GeoRenderProvider {
        private InheritanceItemRenderer renderer;

        @Override
        public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
            if (renderer == null) renderer = new InheritanceItemRenderer();
            return renderer;
        }
    }

    private static final class Model extends GeoModel<InheritanceBlockItem> {
        @Override
        public ResourceLocation getModelResource(InheritanceBlockItem item) {
            return resource("geo/chuancheng.geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(InheritanceBlockItem item) {
            return resource("textures/block/chuancheng.png");
        }

        @Override
        public ResourceLocation getAnimationResource(InheritanceBlockItem item) {
            return resource("animations/chuancheng.animation.json");
        }

        private static ResourceLocation resource(String path) {
            return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
        }
    }
}
