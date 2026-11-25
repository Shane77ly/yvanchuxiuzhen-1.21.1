package net.yvanchuxiuzhen.system.skill.api;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.Event;

/** 玩家骑乘实体时按下 W 键请求切换座驾飞行状态。 */
public final class MountToggleFlightEvent extends Event {
    private final Entity vehicle;
    private final ServerPlayer rider;

    public MountToggleFlightEvent(Entity vehicle, ServerPlayer rider) {
        this.vehicle = vehicle;
        this.rider = rider;
    }

    public Entity vehicle() {
        return vehicle;
    }

    public ServerPlayer rider() {
        return rider;
    }
}
