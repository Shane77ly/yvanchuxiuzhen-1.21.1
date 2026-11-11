package net.yvanchuxiuzhen.system.plant;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentLeaves;
import net.yvanchuxiuzhen.system.particle.api.Particles;

/** 桃花叶片每20刻更新叶距，并在下方为空时按20%概率广播来源花瓣粒子。 */
public final class PeachFlowerBlock extends ContentLeaves {
    public PeachFlowerBlock(BlockTraits traits) {
        super(traits);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moving) {
        super.onPlace(state, level, pos, oldState, moving);
        level.scheduleTick(pos, this, 20);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        if (level.isEmptyBlock(pos.below()) && Math.random() < 0.2) {
            level.sendParticles(Particles.type("peachflowerlizi"), pos.getX() + 0.5, pos.getY(),
                    pos.getZ() + 0.5, 1, 2, 0, 2, 0);
        }
        level.scheduleTick(pos, this, 20);
    }
}
