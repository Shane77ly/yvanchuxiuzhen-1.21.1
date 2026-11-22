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
import net.yvanchuxiuzhen.collection.decor.OffsetBlockEntity;

/** 按偏移方块保存的朝向向右偏移半米并放大二倍渲染槽位内的方块。 */
public final class OffsetBlockRenderer implements BlockEntityRenderer<OffsetBlockEntity> {
    public OffsetBlockRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(OffsetBlockEntity offset, float partialTick, PoseStack pose,
            MultiBufferSource buffers, int light, int overlay) {
        ItemStack stack = offset.getItem(0);
        if (stack.isEmpty()) return;
        String direction = offset.displayDirection();
        pose.pushPose();
        switch (direction) {
            case "北" -> {
                pose.translate(0, 0.5, 0.5);
                pose.mulPose(Axis.YP.rotationDegrees(0));
            }
            case "南" -> {
                pose.translate(1, 0.5, 0.5);
                pose.mulPose(Axis.YP.rotationDegrees(180));
            }
            case "西" -> {
                pose.translate(0.5, 0.5, 1);
                pose.mulPose(Axis.YP.rotationDegrees(90));
            }
            default -> {
                pose.translate(0.5, 0.5, 0);
                pose.mulPose(Axis.YP.rotationDegrees(270));
            }
        }
        pose.scale(2, 2, 2);
        ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
        renderer.render(stack, ItemDisplayContext.FIXED, true, pose, buffers, light, overlay,
                renderer.getItemModelShaper().getItemModel(stack));
        pose.popPose();
    }
}
