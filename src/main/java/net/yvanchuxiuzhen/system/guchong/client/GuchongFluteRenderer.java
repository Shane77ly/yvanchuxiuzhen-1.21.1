package net.yvanchuxiuzhen.system.guchong.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.guchong.content.GuchongFluteItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/** 蛊笛沿用来源 Gecko 模型、空闲动画、半透明纹理及显示变换。 */
final class GuchongFluteRenderer extends GeoItemRenderer<GuchongFluteItem> {
    GuchongFluteRenderer() {
        super(new Model());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poses, MultiBufferSource source,
            int light, int overlay) {
        super.renderByItem(stack, context, poses, source, light, overlay);
        ((GuchongFluteItem) stack.getItem()).markRendered();
    }

    @Override
    public RenderType getRenderType(GuchongFluteItem item, ResourceLocation texture, MultiBufferSource source,
            float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    static final class Provider implements GeoRenderProvider {
        private GuchongFluteRenderer renderer;

        @Override
        public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
            if (renderer == null) renderer = new GuchongFluteRenderer();
            return renderer;
        }
    }

    private static final class Model extends GeoModel<GuchongFluteItem> {
        @Override
        public ResourceLocation getModelResource(GuchongFluteItem item) {
            return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "geo/fabao018.geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(GuchongFluteItem item) {
            return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/item/fabao018.png");
        }

        @Override
        public ResourceLocation getAnimationResource(GuchongFluteItem item) {
            return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "animations/fabao018.animation.json");
        }
    }
}
