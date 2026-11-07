package net.yvanchuxiuzhen.system.xiulian.api;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.Event;

/** 修炼雷劫实体筛选敌方后发出的伤害请求，命令文本保留攻击量取整与倍率。 */
public final class TribulationDamageEvent extends Event {
    private final ServerLevel level;
    private final LivingEntity target;
    private final String command;

    public TribulationDamageEvent(ServerLevel level, LivingEntity target, String command) {
        this.level = level;
        this.target = target;
        this.command = command;
    }

    public ServerLevel level() { return level; }
    public LivingEntity target() { return target; }
    public String command() { return command; }
}
