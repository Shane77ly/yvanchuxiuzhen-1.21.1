package net.yvanchuxiuzhen.system.world.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.yvanchuxiuzhen.platform.ModId;

/** 温泉接触时为受伤且没有再生效果的生物施加再生，并抽取回魂效果。 */
final class SpringLiquidBlock extends LiquidBlock {
    SpringLiquidBlock() {
        super(SpringFluids.SOURCE.get(), BlockBehaviour.Properties.of().mapColor(MapColor.WATER)
                .replaceable().noCollission().strength(100F).pushReaction(PushReaction.DESTROY).liquid());
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        if (entity instanceof LivingEntity living && living.getMaxHealth() > living.getHealth()
                && !living.hasEffect(MobEffects.REGENERATION)) {
            living.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 40, 0));
            if (Math.random() < 0.1) {
                living.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.getHolder(
                        ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "effect_017")).orElseThrow(), 600, 0));
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (level instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.BUBBLE_POP, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5,
                    3, 0.5, 0, 0.5, 0);
        }
    }
}
