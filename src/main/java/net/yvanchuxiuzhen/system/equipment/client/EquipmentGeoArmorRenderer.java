package net.yvanchuxiuzhen.system.equipment.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.equipment.content.EquipmentGeoArmorItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

/** 按来源八个部位骨骼绑定原生人形姿态，以半透明层绘制各材质的护甲几何。 */
final class EquipmentGeoArmorRenderer extends GeoArmorRenderer<EquipmentGeoArmorItem> {
    EquipmentGeoArmorRenderer() {
        super(new Model());
    }

    @Override
    public RenderType getRenderType(EquipmentGeoArmorItem item, ResourceLocation texture, MultiBufferSource source, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    @Override
    public GeoBone getHeadBone(GeoModel<EquipmentGeoArmorItem> model) {
        return model.getBone(animatable.visual().bones().head()).orElse(null);
    }

    @Override
    public GeoBone getBodyBone(GeoModel<EquipmentGeoArmorItem> model) {
        return model.getBone(animatable.visual().bones().body()).orElse(null);
    }

    @Override
    public GeoBone getRightArmBone(GeoModel<EquipmentGeoArmorItem> model) {
        return model.getBone(animatable.visual().bones().rightArm()).orElse(null);
    }

    @Override
    public GeoBone getLeftArmBone(GeoModel<EquipmentGeoArmorItem> model) {
        return model.getBone(animatable.visual().bones().leftArm()).orElse(null);
    }

    @Override
    public GeoBone getRightLegBone(GeoModel<EquipmentGeoArmorItem> model) {
        return model.getBone(animatable.visual().bones().rightLeg()).orElse(null);
    }

    @Override
    public GeoBone getLeftLegBone(GeoModel<EquipmentGeoArmorItem> model) {
        return model.getBone(animatable.visual().bones().leftLeg()).orElse(null);
    }

    @Override
    public GeoBone getRightBootBone(GeoModel<EquipmentGeoArmorItem> model) {
        return model.getBone(animatable.visual().bones().rightBoot()).orElse(null);
    }

    @Override
    public GeoBone getLeftBootBone(GeoModel<EquipmentGeoArmorItem> model) {
        return model.getBone(animatable.visual().bones().leftBoot()).orElse(null);
    }

    static final class Provider implements GeoRenderProvider {
        private EquipmentGeoArmorRenderer renderer;

        @Override
        public <T extends LivingEntity> HumanoidModel<?> getGeoArmorRenderer(T entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<T> original) {
            if (renderer == null) renderer = new EquipmentGeoArmorRenderer();
            return renderer;
        }
    }

    private static final class Model extends GeoModel<EquipmentGeoArmorItem> {
        @Override
        public ResourceLocation getModelResource(EquipmentGeoArmorItem item) {
            return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, item.visual().geometry());
        }

        @Override
        public ResourceLocation getTextureResource(EquipmentGeoArmorItem item) {
            return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, item.visual().texture());
        }

        @Override
        public ResourceLocation getAnimationResource(EquipmentGeoArmorItem item) {
            return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, item.visual().animation());
        }
    }
}
