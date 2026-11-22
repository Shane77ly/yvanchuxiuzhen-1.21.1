package net.yvanchuxiuzhen.system.xiulian.api;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/** 寿尽的延迟轮回请求，保留安排时的世界和玩家对象。 */
public final class LifetimeExhaustedEvent extends Event {
    private final ServerPlayer player;
    private final ServerLevel sourceLevel;

    public LifetimeExhaustedEvent(ServerPlayer player, ServerLevel sourceLevel) {
        this.player = player;
        this.sourceLevel = sourceLevel;
    }

    public ServerPlayer player() {
        return player;
    }

    public ServerLevel sourceLevel() {
        return sourceLevel;
    }
}
