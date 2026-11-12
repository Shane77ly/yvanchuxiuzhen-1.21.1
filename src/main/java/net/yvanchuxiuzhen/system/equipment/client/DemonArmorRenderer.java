package net.yvanchuxiuzhen.system.equipment.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.equipment.content.DemonArmorItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

/** 来源的八个 armor 骨骼沿用 GeckoLib 默认绑定，四个部位共用半透明妖化模型。 */
final class DemonArmorRenderer extends GeoArmorRenderer<DemonArmorItem> {
    DemonArmorRenderer() {
        super(new Model());
    }

    @Override
    public RenderType getRenderType(DemonArmorItem item, ResourceLocation texture, MultiBufferSource source, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    static final class Provider implements GeoRenderProvider {
        private DemonArmorRenderer renderer;

        @Override
        public <T extends LivingEntity> HumanoidModel<?> getGeoArmorRenderer(T entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<T> original) {
            if (renderer == null) renderer = new DemonArmorRenderer();
            return renderer;
        }
    }

    private static final class Model extends GeoModel<DemonArmorItem> {
        @Override
        public ResourceLocation getModelResource(DemonArmorItem item) {
            return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "geo/yaohua_0101.geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(DemonArmorItem item) {
            return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/item/yaohua_0101.png");
        }

        @Override
        public ResourceLocation getAnimationResource(DemonArmorItem item) {
            return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "animations/yaohua_0101.animation.json");
        }
    }
}
