package net.yvanchuxiuzhen.system.fulu.content;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;

/** 镇宅符放置后每五刻排斥未列入放置者友方集合的玩家与生物。 */
public final class RepellingTalismanBlock extends ContentBlock implements EntityBlock {
    private static final int TICK_INTERVAL = 5;

    public RepellingTalismanBlock(BlockTraits traits) {
        super(traits);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RepellingTalismanBlockEntity(pos, state);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        level.scheduleTick(pos, this, TICK_INTERVAL);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        ((RepellingTalismanBlockEntity) level.getBlockEntity(pos)).repel(level);
        level.scheduleTick(pos, this, TICK_INTERVAL);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (placer == null) return;
        if (level.getBlockEntity(pos) instanceof RepellingTalismanBlockEntity talisman) {
            talisman.captureAllegiance(placer);
            if (!level.isClientSide) level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
        }
    }
}
