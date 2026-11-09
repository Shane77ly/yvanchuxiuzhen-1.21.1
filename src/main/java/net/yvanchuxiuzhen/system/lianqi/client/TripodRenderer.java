package net.yvanchuxiuzhen.system.lianqi.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.system.lianqi.content.TripodBlockEntity;

/** 鼎上方九件材料的位置与旋转沿用来源 Xvanran，鼎身仍由各档方块模型绘制。 */
public final class TripodRenderer implements BlockEntityRenderer<TripodBlockEntity> {
    public TripodRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(TripodBlockEntity tripod, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
        int angle = tripod.getPersistentData().getInt("jiaodulinshi");
        for (int slot = 0; slot < 9; slot++) {
            pose.pushPose();
            pose.translate(0.5 + 1.5 * Math.cos(angle + slot * 40), 2, 0.5 + 1.5 * Math.sin(angle + slot * 40));
            pose.mulPose(Axis.YP.rotationDegrees(slot * 40 + 90));
            ItemStack stack = tripod.getItem(slot);
            renderer.render(stack, ItemDisplayContext.FIXED, true, pose, buffers, light + 3, overlay,
                    renderer.getItemModelShaper().getItemModel(stack));
            pose.popPose();
        }
    }
}
