package net.yvanchuxiuzhen.collection.decor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.yvanchuxiuzhen.platform.content.BlockTraits;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;
import net.yvanchuxiuzhen.system.particle.api.Particles;

/** 紫砂香炉（zisha_03）：每刻在炉口上方按朝向持续释放烟粒子。 */
public final class Zisha03Block extends ContentBlock {
    public Zisha03Block(BlockTraits traits) {
        super(traits);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState old, boolean moved) {
        super.onPlace(state, level, pos, old, moved);
        level.scheduleTick(pos, this, 1);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        Direction facing = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        var particle = Particles.type("yan_02");
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();
        if (facing == Direction.NORTH || facing == Direction.SOUTH) {
            level.sendParticles(particle, x + 0.6, y + 0.9, z + 0.5, 1, 0, 0, 0, 0);
            level.sendParticles(particle, x + 0.5, y + 1.0, z + 0.5, 1, 0, 0, 0, 0);
            level.sendParticles(particle, x + 0.4, y + 0.9, z + 0.5, 1, 0, 0, 0, 0);
        } else {
            level.sendParticles(particle, x + 0.5, y + 0.9, z + 0.6, 1, 0, 0, 0, 0);
            level.sendParticles(particle, x + 0.5, y + 1.0, z + 0.5, 1, 0, 0, 0, 0);
            level.sendParticles(particle, x + 0.5, y + 0.9, z + 0.4, 1, 0, 0, 0, 0);
        }
        level.scheduleTick(pos, this, 1);
    }
}
