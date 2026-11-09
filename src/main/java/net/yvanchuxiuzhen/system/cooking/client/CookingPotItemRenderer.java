package net.yvanchuxiuzhen.system.cooking.client;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.cooking.content.CookingPotBlockItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/** 两锅物品沿用来源几何和物品模型中的持物变换。 */
public final class CookingPotItemRenderer extends GeoItemRenderer<CookingPotBlockItem> {
    public CookingPotItemRenderer() {
        super(new Model());
    }

    @Override
    public RenderType getRenderType(CookingPotBlockItem pot, ResourceLocation texture,
            MultiBufferSource source, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    public static final class Provider implements GeoRenderProvider {
        private CookingPotItemRenderer renderer;

        @Override
        public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
            if (renderer == null) renderer = new CookingPotItemRenderer();
            return renderer;
        }
    }

    private static final class Model extends GeoModel<CookingPotBlockItem> {
        @Override
        public ResourceLocation getModelResource(CookingPotBlockItem pot) {
            return resource("geo/" + pot.id() + ".geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(CookingPotBlockItem pot) {
            return resource("textures/block/" + pot.id() + ".png");
        }

        @Override
        public ResourceLocation getAnimationResource(CookingPotBlockItem pot) {
            return resource("animations/" + pot.id() + ".animation.json");
        }
    }

    private static ResourceLocation resource(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
