package net.yvanchuxiuzhen.system.combat.api;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;

/**
 * 修为类状态效果（提高修为、瞬间提高修为）作用在妖兽上时在 {@code NeoForge.EVENT_BUS} 上发出。
 * 来源把增加的修为直接写进妖兽的持久化经验并重算等级，等级与属性属于 creature，由 creature 订阅处理。
 */
public final class EffectExperienceEvent extends Event {
    private final LivingEntity entity;
    private final double experience;

    public EffectExperienceEvent(LivingEntity entity, double experience) {
        this.entity = entity;
        this.experience = experience;
    }

    public LivingEntity entity() {
        return entity;
    }

    public double experience() {
        return experience;
    }
}
