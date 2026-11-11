package net.yvanchuxiuzhen.system.world;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

/** 来源叶藤装饰器：逐叶按西、东、北、南消费整数随机并向下延伸五格。 */
final class SourceLeaveVineDecorator extends TreeDecorator {
    static final SourceLeaveVineDecorator INSTANCE = new SourceLeaveVineDecorator();
    static final MapCodec<SourceLeaveVineDecorator> CODEC = MapCodec.unit(() -> INSTANCE);

    @Override
    protected TreeDecoratorType<?> type() {
        return SourceTreeComponents.LEAVE_VINE.get();
    }

    @Override
    public void place(Context context) {
        for (BlockPos leaf : context.leaves()) {
            if (context.random().nextInt(4) == 0 && context.isAir(leaf.west())) {
                hanging(context, leaf.west(), VineBlock.EAST);
            }
            if (context.random().nextInt(4) == 0 && context.isAir(leaf.east())) {
                hanging(context, leaf.east(), VineBlock.WEST);
            }
            if (context.random().nextInt(4) == 0 && context.isAir(leaf.north())) {
                hanging(context, leaf.north(), VineBlock.SOUTH);
            }
            if (context.random().nextInt(4) == 0 && context.isAir(leaf.south())) {
                hanging(context, leaf.south(), VineBlock.NORTH);
            }
        }
    }

    private static void hanging(Context context, BlockPos position, BooleanProperty facing) {
        context.placeVine(position, facing);
        int remaining = 4;
        for (BlockPos cursor = position.below(); context.isAir(cursor) && remaining > 0; remaining--) {
            context.placeVine(cursor, facing);
            cursor = cursor.below();
        }
    }
}
