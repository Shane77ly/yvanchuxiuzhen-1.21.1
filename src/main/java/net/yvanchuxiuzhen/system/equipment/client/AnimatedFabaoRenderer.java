package net.yvanchuxiuzhen.system.equipment.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.equipment.content.AnimatedFabaoItem;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/** 文王鼓与赶仙鞭共用来源半透明渲染，持物与物品栏变换由模型 JSON 提供。 */
final class AnimatedFabaoRenderer extends GeoItemRenderer<AnimatedFabaoItem> {
    AnimatedFabaoRenderer() {
        super(new Model());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poses, MultiBufferSource source,
            int light, int overlay) {
        super.renderByItem(stack, context, poses, source, light, overlay);
        ((AnimatedFabaoItem) stack.getItem()).markRendered();
    }

    @Override
    public RenderType getRenderType(AnimatedFabaoItem item, ResourceLocation texture, MultiBufferSource source,
            float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    static final class Provider implements AnimatedFabaoItem.RenderProvider {
        private AnimatedFabaoRenderer renderer;

        @Override
        public BlockEntityWithoutLevelRenderer getGeoItemRenderer() {
            if (renderer == null) renderer = new AnimatedFabaoRenderer();
            return renderer;
        }

        @Override
        public void resetHandHeight() {
            EquipmentItemAnimationClient.resetHandHeight();
        }
    }

    private static final class Model extends GeoModel<AnimatedFabaoItem> {
        @Override
        public ResourceLocation getModelResource(AnimatedFabaoItem item) {
            return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "geo/" + item.modelName() + ".geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(AnimatedFabaoItem item) {
            return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/item/" + item.modelName() + ".png");
        }

        @Override
        public ResourceLocation getAnimationResource(AnimatedFabaoItem item) {
            return ResourceLocation.fromNamespaceAndPath(ModId.VALUE,
                    "animations/" + item.modelName() + ".animation.json");
        }
    }
}
