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
import net.yvanchuxiuzhen.collection.decor.UtilityFurnitureBlockEntity;
import net.yvanchuxiuzhen.collection.decor.UtilityFurnitureContent;
import net.yvanchuxiuzhen.collection.decor.UtilityFurnitureVariant;

/** 都承盘居中展示，衣架高位排列，紫砂壶按文字朝向展示四个普通杯模型。 */
public final class UtilityFurnitureRenderer implements BlockEntityRenderer<UtilityFurnitureBlockEntity> {
    private static final double[][] NORTH_CUPS = {{.85, .35}, {.65, .15}, {.35, .15}, {.15, .35}};
    private static final double[][] SOUTH_CUPS = {{.15, .65}, {.35, .85}, {.65, .85}, {.85, .65}};
    private static final double[][] EAST_CUPS = {{.65, .85}, {.85, .65}, {.85, .35}, {.65, .15}};
    private static final double[][] WEST_CUPS = {{.35, .15}, {.15, .35}, {.15, .65}, {.35, .85}};

    public UtilityFurnitureRenderer(BlockEntityRendererProvider.Context context) { }

    @Override public void render(UtilityFurnitureBlockEntity furniture, float partialTick, PoseStack pose,
            MultiBufferSource buffers, int light, int overlay) {
        String direction = furniture.displayDirection();
        float yaw = switch (direction) { case "北" -> 0; case "南" -> 180; case "西" -> 90; default -> 270; };
        for (int slot = 0; slot < furniture.getContainerSize(); slot++) {
            ItemStack stack = furniture.getItem(slot);
            boolean teapot = furniture.variant() == UtilityFurnitureVariant.TEAPOT;
            if (teapot && !stack.is(UtilityFurnitureContent.cup())) continue;
            pose.pushPose();
            if (teapot) {
                double[][] positions = switch (direction) { case "北" -> NORTH_CUPS; case "南" -> SOUTH_CUPS; case "东" -> EAST_CUPS; default -> WEST_CUPS; };
                pose.translate(positions[slot][0], .35, positions[slot][1]);
                stack = new ItemStack(UtilityFurnitureContent.cup());
            } else if (furniture.variant() == UtilityFurnitureVariant.RACK) {
                double x = switch (direction) { case "北" -> 1.5 - slot; case "南" -> -.5 + slot; default -> .5; };
                double z = switch (direction) { case "西" -> -.5 + slot; case "北", "南" -> .5; default -> 1.5 - slot; };
                pose.translate(x, 2, z);
                pose.mulPose(Axis.YP.rotationDegrees(yaw));
                pose.scale(2, 2, 2);
            } else {
                pose.translate(.5, .55, .5);
                pose.mulPose(Axis.YP.rotationDegrees(yaw));
                float scale = stack.getItem() instanceof BlockItem item && item.getBlock() != Blocks.AIR ? 1.7F : .8F;
                pose.scale(scale, scale, scale);
            }
            ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
            renderer.render(stack, ItemDisplayContext.FIXED, true, pose, buffers, light, overlay,
                    renderer.getItemModelShaper().getItemModel(stack));
            pose.popPose();
        }
    }
}
