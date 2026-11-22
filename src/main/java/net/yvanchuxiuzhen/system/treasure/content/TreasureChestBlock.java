package net.yvanchuxiuzhen.system.treasure.content;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;
import net.yvanchuxiuzhen.system.treasure.TreasureChestRuntime;

/** 宝箱03和11共用来源动画状态、二十刻计划刻与持久开箱流程。 */
public class TreasureChestBlock extends ContentBlock implements EntityBlock {
    public static final IntegerProperty ANIMATION = IntegerProperty.create("animation", 0, 2);
    public static final IntegerProperty LOCKED_ANIMATION = IntegerProperty.create("animation", 0, 3);

    public TreasureChestBlock(BlockTraits traits) {
        super(traits);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(animationProperty());
    }

    public IntegerProperty animationProperty() {
        return ANIMATION;
    }

    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.ENTITYBLOCK_ANIMATED; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new TreasureChestBlockEntity(pos, state); }

    @Override
    protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof MenuProvider provider ? provider : null;
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int parameter) {
        super.triggerEvent(state, level, pos, id, parameter);
        BlockEntity chest = level.getBlockEntity(pos);
        return chest != null && chest.triggerEvent(id, parameter);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moved) {
        super.onPlace(state, level, pos, oldState, moved);
        level.scheduleTick(pos, this, 20);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        TreasureChestRuntime.tick(level, pos);
        level.scheduleTick(pos, this, 20);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        TreasureChestRuntime.use(level, pos, player);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        TreasureChestRuntime.use(level, pos, player);
        return ItemInteractionResult.SUCCESS;
    }

    /** 上锁宝箱独有的含水状态保持来源水源与流体计划刻语义。 */
    public static final class Waterlogged extends TreasureChestBlock implements SimpleWaterloggedBlock {
        public Waterlogged(BlockTraits traits) {
            super(traits);
        }

        @Override
        public IntegerProperty animationProperty() {
            return LOCKED_ANIMATION;
        }

        @Override
        protected FluidState getFluidState(BlockState state) {
            return state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
        }

        @Override
        protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level,
                BlockPos pos, BlockPos neighborPos) {
            if (state.getValue(BlockStateProperties.WATERLOGGED)) {
                level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
            }
            return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
        }
    }
}
