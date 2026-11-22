package net.yvanchuxiuzhen.system.fulu.api;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.Event;

/** 神傀符命中妖兽的同步服务端通知；符箓持有命中入口，生物系统处理驯服规则。 */
public final class TalismanTamingEvent extends Event {
    private final ServerLevel level;
    private final Entity target;
    private final Entity shooter;
    private final double grade;

    public TalismanTamingEvent(ServerLevel level, Entity target, Entity shooter, double grade) {
        this.level = level;
        this.target = target;
        this.shooter = shooter;
        this.grade = grade;
    }

    public ServerLevel level() { return level; }
    public Entity target() { return target; }
    public Entity shooter() { return shooter; }
    public double grade() { return grade; }
}
