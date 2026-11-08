package net.yvanchuxiuzhen.system.liandan.content;

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
import net.yvanchuxiuzhen.system.liandan.MedicineContainerPlacement;

/** 放置后的药瓶由单槽方块实体保存原物品实例，方块身份只决定外观、形状与含水能力。 */
public class MedicineContainerBlock extends ContentBlock implements EntityBlock {
    private final String variant;

    protected MedicineContainerBlock(BlockTraits traits) {
        super(traits);
        String id = traits.row().id();
        variant = id.substring("itemdanyaohulu_".length(), id.length() - "block".length());
    }

    public static MedicineContainerBlock create(BlockTraits traits) {
        return ContentBlock.create(traits, traits.waterlogged() ? Waterlogged::new : MedicineContainerBlock::new);
    }

    public String variant() {
        return variant;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MedicineContainerBlockEntity(pos, state);
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        return drops.isEmpty() ? List.of(ItemStack.EMPTY) : drops;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        MedicineContainerPlacement.pickUp(level, pos, player);
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        MedicineContainerPlacement.pickUp(level, pos, player);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof MedicineContainerBlockEntity bottle ? bottle : null;
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int parameter) {
        super.triggerEvent(state, level, pos, id, parameter);
        BlockEntity bottle = level.getBlockEntity(pos);
        return bottle != null && bottle.triggerEvent(id, parameter);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof MedicineContainerBlockEntity bottle) {
                Containers.dropContents(level, pos, bottle);
                level.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, level, pos, newState, movedByPiston);
        }
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return !variant.equals("01");
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return hasAnalogOutputSignal(state) && level.getBlockEntity(pos) instanceof MedicineContainerBlockEntity bottle
                ? AbstractContainerMenu.getRedstoneSignalFromContainer(bottle) : 0;
    }

    /** 仅来源声明 WATERLOGGED 的四种瓶体提供注水、取水与流体更新。 */
    private static final class Waterlogged extends MedicineContainerBlock implements SimpleWaterloggedBlock {
        private Waterlogged(BlockTraits traits) {
            super(traits);
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
