package net.yvanchuxiuzhen.platform.content;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/** 内容表登记的状态效果，行为由 {@link EffectBehavior} 承担。 */
final class ContentEffect extends MobEffect {
    private final EffectBehavior behavior;

    ContentEffect(MobEffectCategory category, int color, EffectBehavior behavior) {
        super(category, color);
        this.behavior = behavior;
    }

    EffectBehavior behavior() {
        return behavior;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide()) {
            behavior.clientTick(entity, amplifier);
            return true;
        }
        return behavior.tick(entity, amplifier);
    }
}
