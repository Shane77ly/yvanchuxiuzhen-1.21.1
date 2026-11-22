package net.yvanchuxiuzhen.system.combat.api;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;

/** 功法命中规则发布的战斗触发，由技能系统消费并生成对应战斗实体。 */
public final class TechniqueMeteorEvent extends Event {
    private final ServerPlayer player;
    private final LivingEntity victim;
    private final double multiplier;

    public TechniqueMeteorEvent(ServerPlayer player, LivingEntity victim, double multiplier) {
        this.player = player;
        this.victim = victim;
        this.multiplier = multiplier;
    }

    public ServerPlayer player() {
        return player;
    }

    public LivingEntity victim() {
        return victim;
    }

    public double multiplier() {
        return multiplier;
    }
}
