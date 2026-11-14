package net.yvanchuxiuzhen.system.world.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.world.content.SpiritCompassItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/** 寻灵盘沿用来源模型、空闲动画、半透明纹理和物品显示变换。 */
final class SpiritCompassRenderer extends GeoItemRenderer<SpiritCompassItem> {
    SpiritCompassRenderer() { super(new Model()); }

    @Override public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poses,
            MultiBufferSource source, int light, int overlay) {
        super.renderByItem(stack, context, poses, source, light, overlay);
        ((SpiritCompassItem) stack.getItem()).markRendered();
    }

    @Override public RenderType getRenderType(SpiritCompassItem item, ResourceLocation texture, MultiBufferSource source, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    static final class Provider implements GeoRenderProvider {
        private SpiritCompassRenderer renderer;
        @Override public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
            if (renderer == null) renderer = new SpiritCompassRenderer();
            return renderer;
        }
    }

    private static final class Model extends GeoModel<SpiritCompassItem> {
        @Override public ResourceLocation getModelResource(SpiritCompassItem item) { return resource("geo/ling_14.geo.json"); }
        @Override public ResourceLocation getTextureResource(SpiritCompassItem item) { return resource("textures/item/ling14item.png"); }
        @Override public ResourceLocation getAnimationResource(SpiritCompassItem item) { return resource("animations/ling_14.animation.json"); }
    }

    private static ResourceLocation resource(String path) { return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path); }
}
