package net.yvanchuxiuzhen.system.cooking.content;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;

/** 三类餐具保持独立注册身份，共用单槽取放、库存掉落与比较器生命周期。 */
public class TablewareBlock extends ContentBlock implements EntityBlock {
    private final Kind kind;

    public TablewareBlock(BlockTraits traits, Kind kind) {
        super(traits);
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TablewareBlockEntity(pos, state);
    }

    @Override
    protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof TablewareBlockEntity tableware ? tableware : null;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        used(level, pos, player);
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        used(level, pos, player);
        return InteractionResult.SUCCESS;
    }

    private static void used(Level level, BlockPos pos, Player player) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof TablewareBlockEntity tableware) {
            tableware.use(player);
        }
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        return drops.isEmpty() ? List.of(new ItemStack(this)) : drops;
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int parameter) {
        super.triggerEvent(state, level, pos, id, parameter);
        BlockEntity tableware = level.getBlockEntity(pos);
        return tableware != null && tableware.triggerEvent(id, parameter);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof TablewareBlockEntity tableware) {
            Containers.dropContents(level, pos, tableware);
            level.updateNeighbourForOutputSignal(pos, this);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof TablewareBlockEntity tableware
                ? AbstractContainerMenu.getRedstoneSignalFromContainer(tableware) : 0;
    }

    /** 容量和各层高度来自三种来源餐具的库存与渲染过程。 */
    public enum Kind {
        BOWL("foodwan", 3, 0.75, 0.25),
        PLATE("foodcipan", 7, 0.625, 0.125),
        LARGE_BOWL("fooddawan", 2, 0.825, 0.325);

        private final String id;
        private final int capacity;
        private final double firstHeight;
        private final double layerHeight;

        Kind(String id, int capacity, double firstHeight, double layerHeight) {
            this.id = id;
            this.capacity = capacity;
            this.firstHeight = firstHeight;
            this.layerHeight = layerHeight;
        }

        public String id() { return id; }
        public int capacity() { return capacity; }
        public double layerHeight(int layer) { return firstHeight + layerHeight * layer; }
    }

    /** 碗和大碗保留来源含水及水刻调度；瓷盘不实现含水接口。 */
    public static final class Waterlogged extends TablewareBlock implements SimpleWaterloggedBlock {
        public Waterlogged(BlockTraits traits, Kind kind) {
            super(traits, kind);
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
