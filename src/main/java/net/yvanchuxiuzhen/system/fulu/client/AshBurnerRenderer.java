package net.yvanchuxiuzhen.system.fulu.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.system.fulu.content.AshBurnerBlockEntity;

/** 九槽木材按来源位置、角度和左右手变换渲染在架内。 */
public final class AshBurnerRenderer implements BlockEntityRenderer<AshBurnerBlockEntity> {
    private static final double[][] PLACEMENTS = {
            {0.5, 0.24, 0.5, 30}, {0.8, 0.24, 0.8, 70}, {0.8, 0.24, 0.2, -10},
            {0.2, 0.24, 0.2, 30}, {0.2, 0.24, 0.8, 10}, {0.25, 0.6, 0.25, -10},
            {0.25, 0.6, 0.75, 80}, {0.75, 0.6, 0.75, 60}, {0.75, 0.6, 0.25, 20}
    };

    public AshBurnerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(AshBurnerBlockEntity burner, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = burner.getItem(slot);
            if (stack.isEmpty()) continue;
            double[] placement = PLACEMENTS[slot];
            pose.pushPose();
            pose.translate(placement[0], placement[1], placement[2]);
            pose.mulPose(Axis.YP.rotationDegrees((float) placement[3]));
            pose.scale(0.7F, 0.7F, 0.7F);
            renderer.render(stack, ItemDisplayContext.FIXED, true, pose, buffers, light, overlay, renderer.getItemModelShaper().getItemModel(stack));
            pose.popPose();
        }
    }
}
