package net.yvanchuxiuzhen.collection.decor.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.collection.decor.RabbitCageBlockEntity;

/** 诱饵在笼底中心按来源位置、九十度旋转与八成缩放平放。 */
public final class RabbitCageRenderer implements BlockEntityRenderer<RabbitCageBlockEntity> {
    public RabbitCageRenderer(BlockEntityRendererProvider.Context context) { }

    @Override public void render(RabbitCageBlockEntity cage, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        ItemStack stack = cage.getItem(0);
        pose.pushPose();
        pose.translate(.5, .3, .5);
        pose.mulPose(Axis.XP.rotationDegrees(90));
        pose.scale(.8F, .8F, .8F);
        ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
        renderer.render(stack, ItemDisplayContext.FIXED, true, pose, buffers, light, overlay, renderer.getItemModelShaper().getItemModel(stack));
        pose.popPose();
    }
}
