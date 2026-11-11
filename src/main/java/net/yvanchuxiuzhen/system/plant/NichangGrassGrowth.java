package net.yvanchuxiuzhen.system.plant;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.yvanchuxiuzhen.system.particle.api.Particles;

/** 保留霓裳草来源的阶段推进率；第六阶段仅由骨粉调用，成功后转为七阶植株。 */
final class NichangGrassGrowth {
    private static final double[] CHANCES = {0, 0.5, 0.4, 0.3, 0.2, 0.1, 0.05};

    private NichangGrassGrowth() {
    }

    static void tick(int stage, ServerLevel level, BlockPos pos) {
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                5, 1, 1, 1, 0);
        if (level.getBlockState(pos).is(NichangGrassCrop.INSTANCE.block(stage)) && Math.random() < CHANCES[stage]) {
            if (stage < 6) {
                level.setBlock(pos, NichangGrassCrop.INSTANCE.block(stage + 1).defaultBlockState(), Block.UPDATE_ALL);
            }
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5,
                    pos.getZ() + 0.5, 10, 1, 1, 1, 0);
            if (stage == 6) {
                level.sendParticles(Particles.type("smokelightred"), pos.getX() + 0.5, pos.getY() + 0.5,
                        pos.getZ() + 0.5, 10, 0, 0, 0, 1);
                level.sendParticles(Particles.type("smokelightred"), pos.getX() + 0.5, pos.getY() + 0.5,
                        pos.getZ() + 0.5, 10, 0, 0, 0, 1);
                level.sendParticles(Particles.type("smokelightred"), pos.getX(), pos.getY(), pos.getZ(),
                        30, 16, 6, 16, 0.2);
                level.sendParticles(Particles.type("smokelightpurple"), pos.getX(), pos.getY(), pos.getZ(),
                        30, 16, 6, 16, 0.2);
                level.setBlock(pos, NichangGrassCrop.INSTANCE.block(stage + 1).defaultBlockState(), Block.UPDATE_ALL);
            }
        }
    }
}
