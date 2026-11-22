package net.yvanchuxiuzhen.collection.decor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;

/** 云盆：每 5 刻喷发云粒子，受红石信号驱动在上方生成或清除地淌云。 */
public final class YvnpenBlock extends ContentBlock {
    public YvnpenBlock(BlockTraits traits) {
        super(traits);
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return true;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
        super.onPlace(state, level, pos, old, moved);
        level.scheduleTick(pos, this, 5);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighbor, BlockPos fromPos, boolean moved) {
        super.neighborChanged(state, level, pos, neighbor, fromPos, moved);
        BlockPos above = pos.above();
        BlockState aboveFluid = level.getFluidState(above).createLegacyBlock();
        if (level.getBestNeighborSignal(pos) > 0) {
            if (aboveFluid.is(Blocks.AIR)) {
                level.setBlock(above, WaterCloud.blockState(), 3);
            }
        } else if (aboveFluid.is(WaterCloud.blockState().getBlock())) {
            level.setBlock(above, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        level.sendParticles(ParticleTypes.CLOUD, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 5, 0.2, 0.2, 0.2, 0.1);
        level.scheduleTick(pos, this, 5);
    }
}
