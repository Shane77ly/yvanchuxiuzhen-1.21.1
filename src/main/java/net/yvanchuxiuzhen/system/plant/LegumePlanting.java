package net.yvanchuxiuzhen.system.plant;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/** 046-056食材的来源播种入口；052/053保留来源分支与消耗行为。 */
public final class LegumePlanting {
    private LegumePlanting() {
    }

    public static InteractionResult plant(UseOnContext context, LegumeCrop crop) {
        if (context.getLevel().isClientSide || context.getClickedFace() != Direction.UP) {
            return InteractionResult.SUCCESS;
        }

        BlockPos clickedPos = context.getClickedPos();
        BlockState clickedState = context.getLevel().getBlockState(clickedPos);
        BlockPos plantPos = clickedPos.above();
        if (!context.getLevel().isEmptyBlock(plantPos)) {
            return InteractionResult.SUCCESS;
        }

        if (crop.supportCrop()) {
            if (clickedState.is(Blocks.FARMLAND)) {
                context.getItemInHand().shrink(1);
                return InteractionResult.SUCCESS;
            }
            if (PlantSoils.dirtOrGrass(clickedState)) {
                BlockState state = crop.block(1).defaultBlockState();
                context.getLevel().setBlock(plantPos, state, Block.UPDATE_ALL);
                context.getLevel().setBlock(plantPos, state.setValue(BlockStateProperties.HORIZONTAL_FACING,
                        randomLegacyFacing()), Block.UPDATE_ALL);
                context.getItemInHand().shrink(1);
            }
            return InteractionResult.SUCCESS;
        }

        if (clickedState.is(Blocks.FARMLAND)) {
            context.getLevel().setBlock(plantPos, crop.block(1).defaultBlockState(), Block.UPDATE_ALL);
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.SUCCESS;
    }

    private static Direction randomLegacyFacing() {
        if (Math.random() < 0.25) return Direction.NORTH;
        if (Math.random() < 0.333) return Direction.SOUTH;
        if (Math.random() < 0.5) return Direction.WEST;
        return Direction.EAST;
    }
}
