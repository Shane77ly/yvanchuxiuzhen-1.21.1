package net.yvanchuxiuzhen.system.xiulian.api;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/** 每秒完成资源恢复后发出，供功法系统按来源顺序检查当前功法种类与名称变化。 */
public final class CultivationAfterRecoveryEvent extends Event {
    private final ServerPlayer player;

    public CultivationAfterRecoveryEvent(ServerPlayer player) {
        this.player = player;
    }

    public ServerPlayer player() {
        return player;
    }
}
