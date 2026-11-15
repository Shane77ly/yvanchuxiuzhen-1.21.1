package net.yvanchuxiuzhen.system.npc.client;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.system.npc.content.QiankunBlockItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/** 乾坤方块物品的Geo渲染提供者。 */
public final class QiankunItemRenderer extends GeoItemRenderer<QiankunBlockItem> {
    public QiankunItemRenderer() { super(new Model()); }
    @Override public RenderType getRenderType(QiankunBlockItem item, ResourceLocation texture, MultiBufferSource buffers, float partialTick) { return RenderType.entityTranslucent(texture); }
    public static final class Provider implements GeoRenderProvider {
        private QiankunItemRenderer renderer;
        @Override public BlockEntityWithoutLevelRenderer getGeoItemRenderer() { if (renderer == null) renderer = new QiankunItemRenderer(); return renderer; }
    }
    private static final class Model extends GeoModel<QiankunBlockItem> {
        @Override public ResourceLocation getModelResource(QiankunBlockItem item) { return QiankunRenderer.resource("geo/" + item.id() + ".geo.json"); }
        @Override public ResourceLocation getTextureResource(QiankunBlockItem item) { return QiankunRenderer.resource("textures/block/" + item.id() + ".png"); }
        @Override public ResourceLocation getAnimationResource(QiankunBlockItem item) { return QiankunRenderer.resource("animations/" + item.id() + ".animation.json"); }
    }
}
