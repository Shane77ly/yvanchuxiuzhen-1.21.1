package net.yvanchuxiuzhen.system.cooking.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.system.cooking.content.CuttingBlockEntity;

/** 根据来源角度与槽号绘制九份案板食材，倾斜角仅作为当前渲染局部量。 */
public final class CuttingBoardRenderer implements BlockEntityRenderer<CuttingBlockEntity> {
    private final ItemRenderer renderer;

    public CuttingBoardRenderer(BlockEntityRendererProvider.Context context) {
        renderer = context.getItemRenderer();
    }

    @Override
    public void render(CuttingBlockEntity board, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        int angle = board.getPersistentData().getInt("jiaodu");
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = board.getItem(slot).copy();
            if (stack.isEmpty()) continue;
            pose.pushPose();
            if (angle == 0) {
                pose.translate(0.1 + slot * 0.1, 0.2, 0.5);
                pose.mulPose(Axis.XP.rotationDegrees(90));
                pose.mulPose(Axis.YP.rotationDegrees(-slot * 4 + 4));
            } else if (angle == 180) {
                pose.translate(0.9 - slot * 0.1, 0.2, 0.5);
                pose.mulPose(Axis.XP.rotationDegrees(90));
                pose.mulPose(Axis.YP.rotationDegrees(180 + slot * 4 - 4));
            } else if (angle == -90) {
                pose.translate(0.5, 0.2, 0.1 + slot * 0.1);
                pose.mulPose(Axis.ZP.rotationDegrees(90));
                pose.mulPose(Axis.YP.rotationDegrees(-90 - slot * 4 + 4));
            } else {
                pose.translate(0.5, 0.2, 0.9 - slot * 0.1);
                pose.mulPose(Axis.ZP.rotationDegrees(90));
                pose.mulPose(Axis.YP.rotationDegrees(angle + slot * 4 - 4));
            }
            pose.scale(0.5F, 0.5F, 0.5F);
            renderer.render(stack, ItemDisplayContext.FIXED, true, pose, buffers, light, overlay,
                    renderer.getItemModelShaper().getItemModel(stack));
            pose.popPose();
        }
    }
}
