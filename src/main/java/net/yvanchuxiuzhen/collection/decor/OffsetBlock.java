package net.yvanchuxiuzhen.collection.decor;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;

/** 偏移方块：右键同步朝向并放入/取出单个方块物品，供客户端向右偏移半米渲染。 */
public final class OffsetBlock extends ContentBlock implements EntityBlock {
    public OffsetBlock(BlockTraits traits) {
        super(traits);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new OffsetBlockEntity(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        interact(state, level, pos, player);
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        interact(state, level, pos, player);
        return InteractionResult.SUCCESS;
    }

    private void interact(BlockState state, Level level, BlockPos pos, Player player) {
        if (level.isClientSide() || !(level.getBlockEntity(pos) instanceof OffsetBlockEntity offset)) return;
        offset.updateDirection(state.getValue(BlockStateProperties.HORIZONTAL_FACING));
        if (player instanceof ServerPlayer server && server.gameMode.getGameModeForPlayer() == GameType.ADVENTURE) {
            offset.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
            return;
        }
        ItemStack held = player.getMainHandItem();
        ItemStack previous = offset.getItem(0).copy();
        if (held.getItem() instanceof BlockItem blockItem && blockItem.getBlock() != Blocks.AIR) {
            if (!previous.isEmpty()) {
                ItemHandlerHelper.giveItemToPlayer(player, previous.copyWithCount(1));
                offset.setItem(0, ItemStack.EMPTY);
            }
            offset.setItem(0, held.copyWithCount(1));
        } else if (held.isEmpty() && !previous.isEmpty()) {
            ItemHandlerHelper.giveItemToPlayer(player, previous.copyWithCount(1));
            offset.setItem(0, ItemStack.EMPTY);
        }
        offset.setChanged();
        level.sendBlockUpdated(pos, state, state, 3);
    }

    @Override
    protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof MenuProvider provider ? provider : null;
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int parameter) {
        super.triggerEvent(state, level, pos, id, parameter);
        BlockEntity entity = level.getBlockEntity(pos);
        return entity != null && entity.triggerEvent(id, parameter);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moved) {
        if (state.getBlock() != next.getBlock()) {
            if (level.getBlockEntity(pos) instanceof OffsetBlockEntity offset) {
                Containers.dropContents(level, pos, offset);
                level.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, level, pos, next, moved);
        }
    }
}
