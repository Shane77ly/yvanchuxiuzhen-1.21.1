package net.yvanchuxiuzhen.system.plant;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/** 普通作物的播种公开入口，供独立种子与Cooking所属可食播种物调用。 */
public final class ProducePlanting {
    private ProducePlanting() {
    }

    public static InteractionResult useOn(UseOnContext context, ProduceCrop crop) {
        if (!context.getLevel().isClientSide && context.getClickedFace() == Direction.UP
                && context.getLevel().getBlockState(context.getClickedPos()).is(Blocks.FARMLAND)) {
            BlockPos plantPos = context.getClickedPos().above();
            if (context.getLevel().isEmptyBlock(plantPos)) {
                context.getLevel().setBlock(plantPos, crop.block(1).defaultBlockState(), Block.UPDATE_ALL);
                context.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
