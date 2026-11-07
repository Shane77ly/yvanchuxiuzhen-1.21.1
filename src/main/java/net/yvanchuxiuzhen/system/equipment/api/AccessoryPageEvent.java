package net.yvanchuxiuzhen.system.equipment.api;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/** 饰品页请求打开玩家面板的属性页，由汇总导航监听；装备系统不依赖面板实现。 */
public final class AccessoryPageEvent extends Event {
    private final ServerPlayer player;

    public AccessoryPageEvent(ServerPlayer player) {
        this.player = player;
    }

    public ServerPlayer player() {
        return player;
    }
}
