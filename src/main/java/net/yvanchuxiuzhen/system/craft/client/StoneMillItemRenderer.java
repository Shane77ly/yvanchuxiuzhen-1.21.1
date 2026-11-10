package net.yvanchuxiuzhen.system.craft.client;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.craft.content.StoneMillBlockItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/** 石磨和石碾物品使用各自来源模型与半透明渲染层。 */
public final class StoneMillItemRenderer extends GeoItemRenderer<StoneMillBlockItem> {
    public StoneMillItemRenderer() {
        super(new Model());
    }

    @Override
    public RenderType getRenderType(StoneMillBlockItem mill, ResourceLocation texture,
            MultiBufferSource source, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    public static final class Provider implements GeoRenderProvider {
        private StoneMillItemRenderer renderer;

        @Override
        public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
            if (renderer == null) renderer = new StoneMillItemRenderer();
            return renderer;
        }
    }

    private static final class Model extends GeoModel<StoneMillBlockItem> {
        @Override
        public ResourceLocation getModelResource(StoneMillBlockItem mill) {
            return resource("geo/" + mill.id() + ".geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(StoneMillBlockItem mill) {
            return resource("textures/block/" + mill.id() + ".png");
        }

        @Override
        public ResourceLocation getAnimationResource(StoneMillBlockItem mill) {
            return resource("animations/" + mill.id() + ".animation.json");
        }
    }

    private static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
