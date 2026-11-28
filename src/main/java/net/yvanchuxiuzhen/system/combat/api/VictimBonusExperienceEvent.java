package net.yvanchuxiuzhen.system.combat.api;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;

/**
 * 玩家击杀非玩家实体按境界计算基础修为时在 {@code NeoForge.EVENT_BUS} 上发出，由持有额外修为加成的系统提供 {@code bonus}。
 */
public final class VictimBonusExperienceEvent extends Event {
    private final LivingEntity victim;
    private double bonus;

    public VictimBonusExperienceEvent(LivingEntity victim) {
        this.victim = victim;
    }

    public LivingEntity victim() {
        return victim;
    }

    public double bonus() {
        return bonus;
    }

    public void addBonus(double amount) {
        bonus += amount;
    }
}
