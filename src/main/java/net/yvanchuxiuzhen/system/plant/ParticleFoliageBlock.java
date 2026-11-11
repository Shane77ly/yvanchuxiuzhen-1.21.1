package net.yvanchuxiuzhen.system.plant;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;
import net.yvanchuxiuzhen.system.particle.api.Particles;

/** 按来源周期在下方为空时广播树叶粒子；该内容保留普通方块基类。 */
public final class ParticleFoliageBlock extends ContentBlock {
    private static final int UPDATE_INTERVAL = 20;
    private final String particleId;

    public ParticleFoliageBlock(BlockTraits traits, String particleId) {
        super(traits);
        this.particleId = particleId;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        level.scheduleTick(pos, this, UPDATE_INTERVAL);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        if (level.isEmptyBlock(pos.below()) && Math.random() < 0.2) {
            level.sendParticles(Particles.type(particleId), pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5,
                    1, 2, 0, 2, 0);
        }
        level.scheduleTick(pos, this, UPDATE_INTERVAL);
    }
}
