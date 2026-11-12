package net.yvanchuxiuzhen.system.fulu.client;

import net.minecraft.client.model.ChickenModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.fulu.content.TalismanSummon;

/** 符箓飞行体使用鸡模型和全透明的 {@code kong.png} 贴图，来源 {@code Fulutexiao09Renderer}，实际不可见。 */
public final class TalismanSummonRenderer extends MobRenderer<TalismanSummon, ChickenModel<TalismanSummon>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "textures/entities/kong.png");

    public TalismanSummonRenderer(EntityRendererProvider.Context context) {
        super(context, new ChickenModel<>(context.bakeLayer(ModelLayers.CHICKEN)), 0.2f);
    }

    @Override
    public ResourceLocation getTextureLocation(TalismanSummon entity) {
        return TEXTURE;
    }
}
