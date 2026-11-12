package net.yvanchuxiuzhen.system.combat.api;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;

/** 伤害达到目标最大生命的十六分之一时发出；专供受击目标所属系统处理重击响应。 */
public final class HeavyHitEvent extends Event {
    private final LivingEntity victim;
    private final Entity attacker;

    public HeavyHitEvent(LivingEntity victim, Entity attacker) {
        this.victim = victim;
        this.attacker = attacker;
    }

    public LivingEntity victim() {
        return victim;
    }

    public Entity attacker() {
        return attacker;
    }
}
