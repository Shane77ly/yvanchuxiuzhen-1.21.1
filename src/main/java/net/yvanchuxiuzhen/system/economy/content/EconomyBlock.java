package net.yvanchuxiuzhen.system.economy.content;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;
import net.yvanchuxiuzhen.system.economy.Auctions;
import net.yvanchuxiuzhen.system.economy.ShopTrading;

/** 交易方块和拍卖台沿用内容表外观，分别提供自然右键入口与来源20刻拍卖更新。 */
public final class EconomyBlock extends ContentBlock implements EntityBlock {
    private final boolean auction;
    public EconomyBlock(BlockTraits traits, boolean auction) { super(traits); this.auction = auction; }
    public boolean auction() { return auction; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new EconomyBlockEntity(pos, state); }
    @Override public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity entity, ItemStack stack) {
        super.setPlacedBy(level, pos, state, entity, stack);
        if (!auction && entity instanceof ServerPlayer player && level.getBlockEntity(pos) instanceof EconomyBlockEntity block) ShopTrading.placed(player, block);
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (player instanceof ServerPlayer serverPlayer && level.getBlockEntity(pos) instanceof EconomyBlockEntity block) {
            if (auction) Auctions.interact(serverPlayer, block); else ShopTrading.interact(serverPlayer, block);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
    @Override protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moving) {
        super.onPlace(state, level, pos, oldState, moving);
        if (auction && !level.isClientSide()) level.scheduleTick(pos, this, 20);
    }
    @Override protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (auction && level.getBlockEntity(pos) instanceof EconomyBlockEntity block) {
            Auctions.tick(level, block);
            level.scheduleTick(pos, this, 20);
        }
    }
    @Override protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        if (state.getBlock() != replacement.getBlock() && level.getBlockEntity(pos) instanceof EconomyBlockEntity block) {
            Containers.dropContents(level, pos, block);
            level.updateNeighbourForOutputSignal(pos, this);
        }
        super.onRemove(state, level, pos, replacement, moving);
    }
}
