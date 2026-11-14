package net.yvanchuxiuzhen.system.economy.api;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/** 经济自然入口提交来源脚本；任务系统独占游标、选项和逐步执行状态。 */
public final class EconomyDialogueRequestEvent extends Event {
    private final ServerPlayer player;
    private final String script;

    public EconomyDialogueRequestEvent(ServerPlayer player, String script) {
        this.player = player;
        this.script = script;
    }

    public ServerPlayer player() { return player; }
    public String script() { return script; }
}
