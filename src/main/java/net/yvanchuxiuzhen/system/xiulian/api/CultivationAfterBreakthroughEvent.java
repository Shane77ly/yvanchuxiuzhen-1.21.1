package net.yvanchuxiuzhen.system.xiulian.api;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/** 每秒完成突破处理后、修炼冷却前发出，供饮食状态按本轮最新体质修正代谢。 */
public final class CultivationAfterBreakthroughEvent extends Event {
    private final ServerPlayer player;

    public CultivationAfterBreakthroughEvent(ServerPlayer player) {
        this.player = player;
    }

    public ServerPlayer player() {
        return player;
    }
}
