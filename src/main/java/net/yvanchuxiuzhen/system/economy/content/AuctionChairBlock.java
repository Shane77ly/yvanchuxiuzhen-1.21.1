package net.yvanchuxiuzhen.system.economy.content;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;
import net.yvanchuxiuzhen.system.economy.Auctions;
import net.yvanchuxiuzhen.system.economy.api.AuctionSeatRequestEvent;

/** 来源拍卖椅无方块库存，右键乘坐装饰实体并打开独立拍卖展示槽。 */
public final class AuctionChairBlock extends ContentBlock {
    public AuctionChairBlock(BlockTraits traits) { super(traits); }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player instanceof ServerPlayer serverPlayer) Auctions.chair(serverPlayer, pos);
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
    @Override public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        boolean destroyed = super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
        removeSeats(level, pos);
        return destroyed;
    }
    @Override public void wasExploded(Level level, BlockPos pos, Explosion explosion) {
        super.wasExploded(level, pos, explosion);
        removeSeats(level, pos);
    }
    private static void removeSeats(Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel)
            NeoForge.EVENT_BUS.post(new AuctionSeatRequestEvent(serverLevel, null, pos, AuctionSeatRequestEvent.Operation.REMOVE_ALL));
    }
}
