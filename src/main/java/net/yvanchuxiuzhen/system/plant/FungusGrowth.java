package net.yvanchuxiuzhen.system.plant;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** 保留菌类阶段推进、骨粉差异与来源同名状态属性复制。 */
final class FungusGrowth {
    private FungusGrowth() {
    }

    static void tick(FungusCrop crop, int stage, ServerLevel level, BlockPos pos) {
        if (stage < crop.lastStage() && Math.random() < 0.2) {
            advance(crop, stage + 1, level, pos);
        }
    }

    static void bonemeal(FungusCrop crop, int stage, ServerLevel level, BlockPos pos) {
        int targetStage = crop.attached() ? crop.lastStage() : stage + 1;
        advance(crop, targetStage, level, pos);
    }

    private static void advance(FungusCrop crop, int targetStage, ServerLevel level, BlockPos pos) {
        BlockState next = crop.block(targetStage).withPropertiesOf(level.getBlockState(pos));
        level.setBlock(pos, next, Block.UPDATE_ALL);
    }
}
