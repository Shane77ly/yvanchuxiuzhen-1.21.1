package net.yvanchuxiuzhen.system.xiulian.content;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;

/**
 * 吸血石（来源 {@code XixveshiBlock}、{@code XixveshiGengXinYouXiKe}）：放置后每 20 tick 检查一次，充血量达到 100 时
 * 在上方 1–4 格中第一个空气处放一格血流并扣 100；4 格都不空时充血量置为 100。充血由流血的实体站在其上时写入（见 {@code BloodStone}）。
 */
public final class BloodStoneBlock extends ContentBlock implements EntityBlock {
    private static final int INTERVAL = 20;

    public BloodStoneBlock(BlockTraits traits) {
        super(traits);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BloodStoneBlockEntity(pos, state);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        level.scheduleTick(pos, this, INTERVAL);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        if (level.getBlockEntity(pos) instanceof BloodStoneBlockEntity stone && stone.blood() >= 100) {
            stone.setBlood(fillAbove(level, pos) ? stone.blood() - 100 : 100);
        }
        level.scheduleTick(pos, this, INTERVAL);
    }

    private static boolean fillAbove(ServerLevel level, BlockPos pos) {
        for (int height = 1; height <= 4; height++) {
            BlockPos above = pos.above(height);
            if (level.isEmptyBlock(above)) {
                level.setBlock(above, XiulianFluids.BLOOD_BLOCK.get().defaultBlockState(), Block.UPDATE_ALL);
                return true;
            }
        }
        return false;
    }
}
