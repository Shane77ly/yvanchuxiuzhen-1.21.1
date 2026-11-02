package net.yvanchuxiuzhen.platform.content.block;

import com.mojang.serialization.MapCodec;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.yvanchuxiuzhen.platform.content.BlockTraits;

/**
 * 继承 {@link Block} 的内容表方块：朝向、轴向、形状、光照与推动反应取自内容表行。
 * 状态属性由行决定，而 {@code createBlockStateDefinition} 在父类构造器中调用，
 * 因此用 {@link #create} 在构造期间暂存内容表数据。
 */
public class ContentBlock extends Block implements TraitedBlock {
    private static final ThreadLocal<BlockTraits> PENDING = new ThreadLocal<>();
    private final BlockTraits traits;

    public static <B extends ContentBlock> B create(BlockTraits traits, Function<BlockTraits, B> constructor) {
        PENDING.set(traits);
        try {
            return constructor.apply(traits);
        } finally {
            PENDING.remove();
        }
    }

    public ContentBlock(BlockTraits traits) {
        super(traits.properties());
        this.traits = traits;
        registerDefaultState(traits.defaultState(stateDefinition.any()));
    }

    @Override
    public BlockTraits traits() {
        return traits;
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return MapCodec.unit(this);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        PENDING.get().addStates(builder);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return traits.stateForPlacement(defaultBlockState(), context);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return traits.rotate(state, rotation);
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return traits.mirror(state, mirror);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return traits.shape(state, level, pos, super.getShape(state, level, pos, context));
    }

    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return traits.emptyVisualShape() ? Shapes.empty() : super.getVisualShape(state, level, pos, context);
    }

    @Override
    protected int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return traits.lightBlock(super.getLightBlock(state, level, pos));
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        Boolean rule = traits.propagatesSkylight(state);
        return rule != null ? rule : super.propagatesSkylightDown(state, level, pos);
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return traits.pushReaction(super.getPistonPushReaction(state));
    }
}
