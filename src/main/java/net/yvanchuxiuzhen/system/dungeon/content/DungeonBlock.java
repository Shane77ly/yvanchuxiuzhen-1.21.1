package net.yvanchuxiuzhen.system.dungeon.content;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
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
import net.yvanchuxiuzhen.system.dungeon.DungeonController;
import net.yvanchuxiuzhen.system.dungeon.api.Dungeons;

/** 副本控制器与刷怪占位符使用来源原生交互和二十刻计划刻。 */
public class DungeonBlock extends ContentBlock implements EntityBlock {
    public DungeonBlock(BlockTraits traits) { super(traits); }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new DungeonBlockEntity(pos, state); }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moved) {
        super.onPlace(state, level, pos, oldState, moved);
        if (!(this instanceof Marker)) level.scheduleTick(pos, this, 20);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof DungeonBlockEntity block) DungeonController.tick(block);
        level.scheduleTick(pos, this, 20);
    }

    protected void use(Level level, BlockPos pos, Player player) {
        if (player instanceof ServerPlayer serverPlayer) Dungeons.open(serverPlayer, pos, player.isCreative());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        use(level, pos, player);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        use(level, pos, player);
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int parameter) {
        super.triggerEvent(state, level, pos, id, parameter);
        BlockEntity entity = level.getBlockEntity(pos);
        return entity != null && entity.triggerEvent(id, parameter);
    }

    public static final class Marker extends DungeonBlock {
        public Marker(BlockTraits traits) { super(traits); }
        @Override protected boolean hasAnalogOutputSignal(BlockState state) { return true; }
        @Override protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) { return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos)); }
        @Override protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState next, boolean moving) {
            if (state.getBlock() != next.getBlock() && level.getBlockEntity(pos) instanceof DungeonBlockEntity block) {
                Containers.dropContents(level, pos, block);
                level.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, level, pos, next, moving);
        }
    }
}
