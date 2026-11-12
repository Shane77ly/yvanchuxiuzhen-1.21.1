package net.yvanchuxiuzhen.system.plant;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;

/** 008根块保留开始挖掘时的玩家采藕数量，实际破坏后独立掉落并清零。 */
public final class LotusRootBlock extends ContentBlock {
    public LotusRootBlock(BlockTraits traits) {
        super(traits);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        LotusGrowth.grow(level, pos);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        LotusGrowth.grow(level, pos);
    }

    @Override
    protected void attack(BlockState state, Level level, BlockPos pos, Player player) {
        super.attack(state, level, pos, player);
        if (level.isClientSide) return;
        for (int height = 1; height <= 10; height++) {
            BlockState above = level.getBlockState(pos.above(height));
            if (above.is(LotusGrowth.block(3)) || above.is(LotusGrowth.block(4))) {
                player.getPersistentData().putDouble("caiou", 1 + Math.floor(height * 0.5));
                break;
            }
        }
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player,
            boolean willHarvest, FluidState fluid) {
        boolean removed = super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
        if (level instanceof ServerLevel server) {
            int count = (int) player.getPersistentData().getDouble("caiou");
            for (int index = 0; index < count; index++) {
                ItemEntity drop = new ItemEntity(server, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        LotusGrowth.item("shicai_008", 1));
                drop.setPickUpDelay(10);
                server.addFreshEntity(drop);
            }
            player.getPersistentData().putDouble("caiou", 0);
        }
        return removed;
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = super.getDrops(state, params);
        return drops.isEmpty() ? List.of(new ItemStack(Blocks.DIRT)) : drops;
    }
}
