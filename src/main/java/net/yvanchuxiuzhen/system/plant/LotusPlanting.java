package net.yvanchuxiuzhen.system.plant;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/** Cooking莲子调用的公开播种入口；Plant拥有泥土替换和对应世界状态写入。 */
public final class LotusPlanting {
    private LotusPlanting() {
    }

    public static InteractionResult plant(UseOnContext context) {
        if (!context.getLevel().isClientSide) {
            BlockPos pos = context.getClickedPos();
            if (context.getLevel().getBlockState(pos).is(Blocks.FARMLAND)) {
                if (context.getClickedFace() == Direction.UP && context.getLevel().isEmptyBlock(pos.above())) {
                    context.getItemInHand().shrink(1);
                }
            } else if (context.getLevel().getBlockState(pos).is(Blocks.DIRT)
                    && context.getLevel().getBlockState(pos.above()).is(Blocks.WATER)) {
                context.getLevel().setBlock(pos, LotusGrowth.block(1).defaultBlockState(), Block.UPDATE_ALL);
                context.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
