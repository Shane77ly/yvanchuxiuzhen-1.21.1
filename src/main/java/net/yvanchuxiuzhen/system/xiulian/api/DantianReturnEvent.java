package net.yvanchuxiuzhen.system.xiulian.api;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * 丹田界面的返回按钮（来源 {@code Dantianguanbianniu}：按 {@code dangqianGUI} 回到玩家面板的第 1–6 页，否则关闭界面）。
 * 在服务端发布到 NeoForge 事件总线；打开面板分页的一方取消本事件，没有被取消时关闭界面。
 */
public final class DantianReturnEvent extends Event implements ICancellableEvent {
    private final ServerPlayer player;

    public DantianReturnEvent(ServerPlayer player) {
        this.player = player;
    }

    public ServerPlayer player() {
        return player;
    }
}
