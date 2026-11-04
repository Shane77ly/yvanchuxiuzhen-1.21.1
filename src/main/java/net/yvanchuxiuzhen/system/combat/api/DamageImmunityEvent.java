package net.yvanchuxiuzhen.system.combat.api;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;

/**
 * 伤害结算在敌友判定之后、闪避与计算之前发出，所属系统可以据自己的规则让受击者免伤
 * （来源 {@code Yaoshou006fangyvzitai}：妖兽的防御姿态不受其他生物伤害，规则归 creature）。
 * 在 {@code NeoForge.EVENT_BUS} 上发出，监听者调用 {@link #setImmune} 即免伤。
 */
public final class DamageImmunityEvent extends Event {
    private final LivingEntity victim;
    private final Entity attacker;
    private boolean immune;

    public DamageImmunityEvent(LivingEntity victim, Entity attacker) {
        this.victim = victim;
        this.attacker = attacker;
    }

    public LivingEntity victim() {
        return victim;
    }

    public Entity attacker() {
        return attacker;
    }

    public boolean immune() {
        return immune;
    }

    public void setImmune() {
        immune = true;
    }
}
