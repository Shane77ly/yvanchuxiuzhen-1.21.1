package net.yvanchuxiuzhen.system.economy.api;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/** 本人高级通宝玉请求启动来源单步银行对话；会话与刻消费归任务系统。 */
public final class BankDialogueRequestEvent extends Event {
    private final ServerPlayer player;

    public BankDialogueRequestEvent(ServerPlayer player) {
        this.player = player;
    }

    public ServerPlayer player() {
        return player;
    }
}
