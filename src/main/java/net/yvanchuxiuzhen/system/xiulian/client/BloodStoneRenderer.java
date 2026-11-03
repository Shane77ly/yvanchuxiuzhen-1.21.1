package net.yvanchuxiuzhen.system.xiulian.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.yvanchuxiuzhen.system.xiulian.content.BloodStoneBlockEntity;

/**
 * 吸血石的血池（来源 {@code XixveshiFangKuaiShiTiXuanRanShi}）：以粉红色陶瓦的物品模型表示，高度 = min(充血量 × 2, 200) / 110，
 * 水平放大 1.9 倍，抬高高度的四分之一再加 0.04。
 */
public final class BloodStoneRenderer implements BlockEntityRenderer<BloodStoneBlockEntity> {
    private static final ItemStack POOL = new ItemStack(Blocks.PINK_TERRACOTTA);

    public BloodStoneRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(BloodStoneBlockEntity stone, float partialTick, PoseStack poseStack, MultiBufferSource buffers, int light, int overlay) {
        float height = Math.min(stone.blood() * 2, 200) / 110F;
        poseStack.pushPose();
        poseStack.translate(0.5, height / 4 + 0.04, 0.5);
        poseStack.scale(1.9F, height, 1.9F);
        Minecraft.getInstance().getItemRenderer().renderStatic(POOL, ItemDisplayContext.FIXED, light, overlay, poseStack, buffers, stone.getLevel(), 0);
        poseStack.popPose();
    }
}
