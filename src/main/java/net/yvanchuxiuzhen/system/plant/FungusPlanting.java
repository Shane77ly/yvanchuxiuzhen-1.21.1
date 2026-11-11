package net.yvanchuxiuzhen.system.plant;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/** Cooking 菌类食材的 Plant 播种操作，只在服务器写入作物状态。 */
public final class FungusPlanting {
    private FungusPlanting() {
    }

    public static InteractionResult useOn(UseOnContext context, FungusCrop crop) {
        if (!context.getLevel().isClientSide) {
            Direction face = context.getClickedFace();
            BlockPos supportPos = context.getClickedPos();
            BlockState support = context.getLevel().getBlockState(supportPos);
            boolean supported = crop.attached()
                    ? support.is(BlockTags.LOGS) || support.is(Blocks.MYCELIUM) || support.is(Blocks.PODZOL)
                    : face == Direction.UP && (support.is(Blocks.MYCELIUM) || support.is(Blocks.PODZOL));
            BlockPos plantPos = supportPos.relative(face);
            if (supported && context.getLevel().isEmptyBlock(plantPos)) {
                context.getLevel().setBlock(plantPos, crop.block(1).defaultBlockState(), Block.UPDATE_ALL);
                if (crop.attached()) {
                    BlockState placed = context.getLevel().getBlockState(plantPos);
                    if (placed.hasProperty(BlockStateProperties.FACING)) {
                        context.getLevel().setBlock(plantPos, placed.setValue(BlockStateProperties.FACING, face), Block.UPDATE_ALL);
                    }
                }
                context.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
