package net.yvanchuxiuzhen.system.combat.api;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;

/**
 * 受击者当前生命低于即将承受的伤害量时发出（来源在三处伤害过程中各自检查玩家背包里的 {@code itemfulu_16}：
 * 播放图腾粒子与音效、发光、回复 (符箓等级 + 1) × 10 点生命并消耗一张符箓；符箓的规则归 fulu）。
 * 监听者让受击者存活后调用 {@link #setSurvived}，本次伤害随即作废。在 {@code NeoForge.EVENT_BUS} 上发出。
 */
public final class LethalDamageEvent extends Event {
    private final LivingEntity victim;
    private final double amount;
    private boolean survived;

    public LethalDamageEvent(LivingEntity victim, double amount) {
        this.victim = victim;
        this.amount = amount;
    }

    public LivingEntity victim() {
        return victim;
    }

    public double amount() {
        return amount;
    }

    public boolean survived() {
        return survived;
    }

    public void setSurvived() {
        survived = true;
    }
}
