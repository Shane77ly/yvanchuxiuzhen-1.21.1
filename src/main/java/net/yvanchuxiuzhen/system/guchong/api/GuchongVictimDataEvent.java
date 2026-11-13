package net.yvanchuxiuzhen.system.guchong.api;

import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;

/** 蛊虫击杀成长在服务端同步查询死者的来源等级与额外经验。 */
public final class GuchongVictimDataEvent extends Event {
    private final LivingEntity victim;
    private double level;
    private double additionalExperience;

    public GuchongVictimDataEvent(LivingEntity victim) {
        this.victim = victim;
    }

    public LivingEntity victim() {
        return victim;
    }

    public double level() {
        return level;
    }

    public double additionalExperience() {
        return additionalExperience;
    }

    public void respond(double level, double additionalExperience) {
        this.level = level;
        this.additionalExperience = additionalExperience;
    }
}
