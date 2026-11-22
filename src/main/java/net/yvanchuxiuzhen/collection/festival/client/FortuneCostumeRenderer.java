package net.yvanchuxiuzhen.collection.festival.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.collection.festival.FortuneCostumeItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

/** 财神服按来源八个部位骨骼绑定原生姿态，以半透明层绘制同一套几何。 */
final class FortuneCostumeRenderer extends GeoArmorRenderer<FortuneCostumeItem> {
    FortuneCostumeRenderer() {
        super(new Model());
    }

    @Override
    public RenderType getRenderType(FortuneCostumeItem item, ResourceLocation texture, MultiBufferSource source, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    @Override
    public GeoBone getHeadBone(GeoModel<FortuneCostumeItem> model) {
        return model.getBone("head").orElse(null);
    }

    @Override
    public GeoBone getBodyBone(GeoModel<FortuneCostumeItem> model) {
        return model.getBone("body").orElse(null);
    }

    @Override
    public GeoBone getRightArmBone(GeoModel<FortuneCostumeItem> model) {
        return model.getBone("rightarm").orElse(null);
    }

    @Override
    public GeoBone getLeftArmBone(GeoModel<FortuneCostumeItem> model) {
        return model.getBone("leftarm").orElse(null);
    }

    @Override
    public GeoBone getRightLegBone(GeoModel<FortuneCostumeItem> model) {
        return model.getBone("rightleg").orElse(null);
    }

    @Override
    public GeoBone getLeftLegBone(GeoModel<FortuneCostumeItem> model) {
        return model.getBone("leftleg").orElse(null);
    }

    @Override
    public GeoBone getRightBootBone(GeoModel<FortuneCostumeItem> model) {
        return model.getBone("rightboot").orElse(null);
    }

    @Override
    public GeoBone getLeftBootBone(GeoModel<FortuneCostumeItem> model) {
        return model.getBone("leftboot").orElse(null);
    }

    static final class Provider implements GeoRenderProvider {
        private FortuneCostumeRenderer renderer;

        @Override
        public <T extends LivingEntity> HumanoidModel<?> getGeoArmorRenderer(T entity, ItemStack stack, EquipmentSlot slot, HumanoidModel<T> original) {
            if (renderer == null) renderer = new FortuneCostumeRenderer();
            return renderer;
        }
    }

    private static final class Model extends GeoModel<FortuneCostumeItem> {
        @Override
        public ResourceLocation getModelResource(FortuneCostumeItem item) {
            return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "geo/fuqi0201.geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(FortuneCostumeItem item) {
            return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/item/fuqi0206.png");
        }

        @Override
        public ResourceLocation getAnimationResource(FortuneCostumeItem item) {
            return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "animations/fuqi0201.animation.json");
        }
    }
}
