package net.yvanchuxiuzhen.system.xiulian.api;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.Event;

/** 修炼雷劫发出的范围落雷请求：保留未取整的落点与来源完整命令文本，由战斗系统结算。 */
public final class TribulationLightningEvent extends Event {
    private final ServerLevel level;
    private final Vec3 position;
    private final String command;

    public TribulationLightningEvent(ServerLevel level, Vec3 position, String command) {
        this.level = level;
        this.position = position;
        this.command = command;
    }

    public ServerLevel level() { return level; }
    public Vec3 position() { return position; }
    public String command() { return command; }
}
