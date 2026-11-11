package net.yvanchuxiuzhen.system.world.tree;

import com.mojang.serialization.MapCodec;
import java.util.function.Supplier;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

/** 群系树装饰器的独立无参数类型；方块默认状态在生成时解析。 */
abstract class SourceTreeDecorator extends TreeDecorator {
    private final TreeDecoratorType<SourceTreeDecorator> decoratorType;
    private final Supplier<BlockState> state;

    SourceTreeDecorator(Supplier<BlockState> state) {
        this.state = state;
        this.decoratorType = new TreeDecoratorType<>(MapCodec.unit(this));
    }

    @Override
    protected final TreeDecoratorType<?> type() {
        return decoratorType;
    }

    final TreeDecoratorType<SourceTreeDecorator> registeredType() {
        return decoratorType;
    }

    final BlockState state() {
        return state.get();
    }
}
