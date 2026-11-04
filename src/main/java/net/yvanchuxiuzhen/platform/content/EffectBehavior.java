package net.yvanchuxiuzhen.platform.content;

import net.minecraft.world.entity.LivingEntity;

/**
 * 状态效果的行为，对应 1.18.2 版本 {@code MobEffect} 子类的三个钩子：
 * {@link #started} 是 {@code addAttributeModifiers}（效果加上，以及已有效果被刷新而重新应用时），
 * {@link #tick} 是 {@code applyEffectTick}（来源的 {@code isDurationEffectTick} 恒为真，每刻执行）。
 * 来源的过程在两端的实体上都会执行；{@link #started} 与 {@link #tick} 只在服务端调用，
 * 需要在本人客户端同步执行的（如转动视角）另用 {@link #clientTick}。
 */
public interface EffectBehavior {
    default void started(LivingEntity entity, int amplifier) {
    }

    /** @return 为假时效果被移除 */
    default boolean tick(LivingEntity entity, int amplifier) {
        return true;
    }

    /** 客户端实体上的每刻执行，只能改本地表现。 */
    default void clientTick(LivingEntity entity, int amplifier) {
    }
}
