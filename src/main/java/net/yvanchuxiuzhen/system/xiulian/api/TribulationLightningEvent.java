package net.yvanchuxiuzhen.system.xiulian.api;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.Event;

/** 劫云在指定原始落点发出的无来源范围落雷请求。 */
public final class TribulationLightningEvent extends Event {
    private final ServerLevel level;
    private final Vec3 position;
    private final double base;
    private final double multiplier;
    private final int diameter;
    private final String color;

    public TribulationLightningEvent(ServerLevel level, Vec3 position, double base, double multiplier, int diameter, String color) {
        this.level = level;
        this.position = position;
        this.base = base;
        this.multiplier = multiplier;
        this.diameter = diameter;
        this.color = color;
    }

    public ServerLevel level() { return level; }
    public Vec3 position() { return position; }
    public double base() { return base; }
    public double multiplier() { return multiplier; }
    public int diameter() { return diameter; }
    public String color() { return color; }
}
