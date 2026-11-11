package net.yvanchuxiuzhen.system.world.tree;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** 逐叶以四分之一概率向下生成最多五格来源默认方块。 */
final class HangingTreeDecorator extends SourceTreeDecorator {
    HangingTreeDecorator(Supplier<BlockState> state) {
        super(state);
    }

    @Override
    public void place(Context context) {
        for (BlockPos leaf : context.leaves()) {
            if (context.random().nextInt(4) == 0) {
                BlockPos position = leaf.below();
                if (context.isAir(position)) {
                    context.setBlock(position, state());
                    int remaining = 4;
                    for (BlockPos below = position.below(); context.isAir(below) && remaining > 0; --remaining) {
                        context.setBlock(below, state());
                        below = below.below();
                    }
                }
            }
        }
    }
}
