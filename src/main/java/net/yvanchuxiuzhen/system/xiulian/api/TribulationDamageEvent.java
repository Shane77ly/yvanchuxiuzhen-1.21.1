package net.yvanchuxiuzhen.system.xiulian.api;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;

/** 雷劫实体筛选目标后发出的雷伤请求。 */
public final class TribulationDamageEvent extends Event {
    private final ServerLevel level;
    private final LivingEntity target;
    private final LivingEntity attacker;
    private final double base;
    private final double multiplier;

    public TribulationDamageEvent(ServerLevel level, LivingEntity target, LivingEntity attacker, double base, double multiplier) {
        this.level = level;
        this.target = target;
        this.attacker = attacker;
        this.base = base;
        this.multiplier = multiplier;
    }

    public ServerLevel level() { return level; }
    public LivingEntity target() { return target; }
    public LivingEntity attacker() { return attacker; }
    public double base() { return base; }
    public double multiplier() { return multiplier; }
}
