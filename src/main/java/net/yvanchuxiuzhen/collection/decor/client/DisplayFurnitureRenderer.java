package net.yvanchuxiuzhen.collection.decor.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.yvanchuxiuzhen.collection.decor.DisplayFurnitureBlockEntity;

/** 展示家具按放置时保存的文字朝向，分别绘制普通物品与方块模型。 */
public final class DisplayFurnitureRenderer implements BlockEntityRenderer<DisplayFurnitureBlockEntity> {
    public DisplayFurnitureRenderer(BlockEntityRendererProvider.Context context) { }

    @Override
    public void render(DisplayFurnitureBlockEntity display, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int light, int overlay) {
        String direction = display.displayDirection();
        float yaw = switch (direction) { case "北" -> 0; case "南" -> 180; case "西" -> 90; default -> 270; };
        boolean cabinet = display.variant().cabinet();
        for (int slot = 0; slot < display.getContainerSize(); slot++) {
            ItemStack stack = display.getItem(slot);
            boolean block = stack.getItem() instanceof BlockItem item && item.getBlock() != Blocks.AIR;
            pose.pushPose();
            if (cabinet) {
                double x = switch (direction) { case "北" -> 1.5 - slot; case "南" -> -.5 + slot; default -> .5; };
                double z = switch (direction) { case "西" -> -.5 + slot; case "北", "南" -> .5; default -> 1.5 - slot; };
                pose.translate(x, block ? 2 : 1.55, z);
            } else {
                pose.translate(.5, .55, .5);
            }
            pose.mulPose(Axis.YP.rotationDegrees(yaw));
            if (cabinet && !block) pose.mulPose(Axis.XP.rotationDegrees(direction.equals("南") || direction.equals("西") ? -90 : 90));
            float scale = block ? cabinet ? 2 : 1.7F : .8F;
            pose.scale(scale, scale, scale);
            ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
            renderer.render(stack, ItemDisplayContext.FIXED, true, pose, buffers, light, overlay,
                    renderer.getItemModelShaper().getItemModel(stack));
            pose.popPose();
        }
    }
}
