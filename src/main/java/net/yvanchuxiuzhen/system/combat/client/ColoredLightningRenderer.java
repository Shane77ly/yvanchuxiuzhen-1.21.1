package net.yvanchuxiuzhen.system.combat.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LightningBoltRenderer;
import net.minecraft.world.entity.LightningBolt;
import net.neoforged.neoforge.client.model.pipeline.VertexConsumerWrapper;
import net.yvanchuxiuzhen.system.combat.content.ColoredLightningBolt;

/** 原生闪电几何使用来源同步的 ARGB 颜色，包括来源不透明的 alpha。 */
final class ColoredLightningRenderer extends LightningBoltRenderer {
    ColoredLightningRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(LightningBolt bolt, float yaw, float partialTicks, PoseStack poses, MultiBufferSource buffers, int light) {
        int color = ((ColoredLightningBolt) bolt).color();
        super.render(bolt, yaw, partialTicks, poses, type -> new VertexConsumerWrapper(buffers.getBuffer(type)) {
            @Override
            public VertexConsumer setColor(int red, int green, int blue, int alpha) {
                parent.setColor(color);
                return this;
            }
        }, light);
    }
}
