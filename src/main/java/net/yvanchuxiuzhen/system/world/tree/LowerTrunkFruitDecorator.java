package net.yvanchuxiuzhen.system.world.tree;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

/** 整树五分之一概率进入低三层树干的四向默认方块装饰。 */
final class LowerTrunkFruitDecorator extends SourceTreeDecorator {
    LowerTrunkFruitDecorator(Supplier<BlockState> state) {
        super(state);
    }

    @Override
    public void place(Context context) {
        if (!(context.random().nextFloat() >= 0.2F)) {
            int firstY = context.logs().get(0).getY();
            for (BlockPos log : context.logs()) {
                if (log.getY() - firstY <= 2) {
                    for (Direction direction : Direction.Plane.HORIZONTAL) {
                        if (context.random().nextFloat() <= 0.25F) {
                            Direction opposite = direction.getOpposite();
                            BlockPos position = log.offset(opposite.getStepX(), 0, opposite.getStepZ());
                            if (context.isAir(position)) {
                                context.setBlock(position, state());
                            }
                        }
                    }
                }
            }
        }
    }
}
