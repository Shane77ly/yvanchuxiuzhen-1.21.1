package net.yvanchuxiuzhen.system.lianqi.client;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.lianqi.content.RepairTableBlockItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/** 修理台背包、落地及持物显示共用来源模型，变换由物品模型定义。 */
public final class RepairTableItemRenderer extends GeoItemRenderer<RepairTableBlockItem> {
    public RepairTableItemRenderer() {
        super(new Model());
    }

    @Override
    public RenderType getRenderType(RepairTableBlockItem item, ResourceLocation texture, MultiBufferSource source, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    public static final class Provider implements GeoRenderProvider {
        private RepairTableItemRenderer renderer;

        @Override
        public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
            if (renderer == null) renderer = new RepairTableItemRenderer();
            return renderer;
        }
    }

    private static final class Model extends GeoModel<RepairTableBlockItem> {
        @Override
        public ResourceLocation getModelResource(RepairTableBlockItem item) {
            return resource("geo/xiulitai.geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(RepairTableBlockItem item) {
            return resource("textures/block/xiulitai.png");
        }

        @Override
        public ResourceLocation getAnimationResource(RepairTableBlockItem item) {
            return resource("animations/xiulitai.animation.json");
        }

        private static ResourceLocation resource(String path) {
            return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
        }
    }
}
