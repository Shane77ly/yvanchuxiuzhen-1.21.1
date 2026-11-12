package net.yvanchuxiuzhen.system.world.fluid;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

/** 温泉源与流动态共享的流动参数及滴落粒子。 */
abstract class SpringFluid extends BaseFlowingFluid {
    SpringFluid() {
        super(SpringFluids.properties());
    }

    @Override
    public ParticleOptions getDripParticle() {
        return ParticleTypes.FALLING_WATER;
    }

    static final class Source extends SpringFluid {
        @Override
        public int getAmount(FluidState state) {
            return 8;
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
        }
    }

    static final class Flowing extends SpringFluid {
        @Override
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }

        @Override
        public boolean isSource(FluidState state) {
            return false;
        }
    }
}
