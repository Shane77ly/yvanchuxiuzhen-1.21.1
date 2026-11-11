package net.yvanchuxiuzhen.system.world.tree;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/** 来源规则检查树干下方空气，随后在当前树干位置写入默认方块。 */
final class TrunkReplacementTreeDecorator extends SourceTreeDecorator {
    TrunkReplacementTreeDecorator(Supplier<BlockState> state) {
        super(state);
    }

    @Override
    public void place(Context context) {
        for (BlockPos log : context.logs()) {
            if (context.random().nextInt(3) > 0) {
                BlockPos below = log.below();
                if (context.isAir(below)) {
                    context.setBlock(log, state());
                }
            }
        }
    }
}
