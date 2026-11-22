package net.yvanchuxiuzhen.system.town.content;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;
import net.yvanchuxiuzhen.system.town.TownRuntime;

/** 规划核心自然材料入口与中心案台认主入口。 */
public final class TownBlock extends ContentBlock implements EntityBlock {
    private final boolean planning;

    public TownBlock(BlockTraits traits, boolean planning) {
        super(traits);
        this.planning = planning;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TownBlockEntity(pos, state, planning);
    }

    private void interact(Player player, Level level, BlockPos pos) {
        if (player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof TownBlockEntity town) {
            if (planning) TownRuntime.usePlan(serverPlayer, town);
            else TownRuntime.useCenter(serverPlayer, town);
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        interact(player, level, pos);
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        interact(player, level, pos);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof TownBlockEntity town ? town : null;
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int parameter) {
        super.triggerEvent(state, level, pos, id, parameter);
        BlockEntity entity = level.getBlockEntity(pos);
        return entity != null && entity.triggerEvent(id, parameter);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof TownBlockEntity town) Containers.dropContents(level, pos, town);
            level.updateNeighbourForOutputSignal(pos, this);
            super.onRemove(state, level, pos, newState, moved);
        }
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return !planning;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof TownBlockEntity town ? AbstractContainerMenu.getRedstoneSignalFromContainer(town) : 0;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof TownBlockEntity town && planning) TownRuntime.countMaterials(town);
    }
}
