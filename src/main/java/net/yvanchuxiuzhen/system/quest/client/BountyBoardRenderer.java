package net.yvanchuxiuzhen.system.quest.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.yvanchuxiuzhen.system.quest.content.BountyBoardBlockEntity;

/** 六张悬赏纸按来源三列两行绘制，朝向随告示牌旋转。 */
public final class BountyBoardRenderer implements BlockEntityRenderer<BountyBoardBlockEntity> {
    public BountyBoardRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(BountyBoardBlockEntity board, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        for (int index = 0; index < 6; index++) {
            pose.pushPose();
            pose.translate(.5, .5 + (index < 3 ? 1 : 0), .5);
            float rotation = switch (board.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING)) {
                case SOUTH -> 180;
                case WEST -> 90;
                case EAST -> 270;
                default -> 0;
            };
            pose.mulPose(Axis.YP.rotationDegrees(rotation));
            pose.translate(1 - index % 3, 0, 0);
            pose.scale(1.8F, 1.8F, 1.8F);
            Minecraft.getInstance().getItemRenderer().renderStatic(board.getItem(index), ItemDisplayContext.FIXED, light, overlay,
                    pose, buffers, board.getLevel(), 0);
            pose.popPose();
        }
    }
}
