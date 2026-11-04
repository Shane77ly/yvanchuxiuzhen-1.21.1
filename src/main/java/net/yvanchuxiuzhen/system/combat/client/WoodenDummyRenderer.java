package net.yvanchuxiuzhen.system.combat.client;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.combat.content.WoodenDummyEntity;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * 木人桩的渲染（来源 {@code MurenzhuangRenderer}、{@code MurenzhuangModel}）：GeckoLib 几何 {@code geo/murenzhuang.geo.json}、
 * 动画 {@code animations/murenzhuang.animation.json}、贴图 {@code textures/entities/murenzhuang.png}，阴影半径 0.5，半透明渲染。
 */
public final class WoodenDummyRenderer extends GeoEntityRenderer<WoodenDummyEntity> {
    public WoodenDummyRenderer(EntityRendererProvider.Context context) {
        super(context, new Model());
        shadowRadius = 0.5f;
    }

    @Override
    public RenderType getRenderType(WoodenDummyEntity entity, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    private static final class Model extends GeoModel<WoodenDummyEntity> {
        @Override
        public ResourceLocation getModelResource(WoodenDummyEntity entity) {
            return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "geo/murenzhuang.geo.json");
        }

        @Override
        public ResourceLocation getTextureResource(WoodenDummyEntity entity) {
            return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/entities/murenzhuang.png");
        }

        @Override
        public ResourceLocation getAnimationResource(WoodenDummyEntity entity) {
            return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "animations/murenzhuang.animation.json");
        }
    }
}
