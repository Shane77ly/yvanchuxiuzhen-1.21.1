package net.yvanchuxiuzhen.system.cooking.client;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.cooking.content.CleaverBlockItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/** 菜刀方块物品沿用来源几何和物品模型中的持物变换。 */
public final class CleaverItemRenderer extends GeoItemRenderer<CleaverBlockItem> {
    public CleaverItemRenderer() {
        super(new Model());
    }

    @Override
    public RenderType getRenderType(CleaverBlockItem pot, ResourceLocation texture,
            MultiBufferSource source, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    public static final class Provider implements GeoRenderProvider {
        private CleaverItemRenderer renderer;

        @Override
        public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
            if (renderer == null) renderer = new CleaverItemRenderer();
            return renderer;
        }
    }

    private static final class Model extends GeoModel<CleaverBlockItem> {
        @Override
        public ResourceLocation getModelResource(CleaverBlockItem pot) {
            return resource("geo/" + pot.id() + ".geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(CleaverBlockItem pot) {
            return resource("textures/block/caidaoblock.png");
        }

        @Override
        public ResourceLocation getAnimationResource(CleaverBlockItem pot) {
            return resource("animations/" + pot.id() + ".animation.json");
        }
    }

    private static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
