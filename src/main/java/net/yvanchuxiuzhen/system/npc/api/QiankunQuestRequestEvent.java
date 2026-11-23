package net.yvanchuxiuzhen.system.npc.api;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/** 玩家右键交互乾坤门徒任务台（renwuset > 0）时发布的任务对话请求事件。 */
public final class QiankunQuestRequestEvent extends Event {
    private final ServerPlayer player;
    private final int questIndex;

    public QiankunQuestRequestEvent(ServerPlayer player, int questIndex) {
        this.player = player;
        this.questIndex = questIndex;
    }

    public ServerPlayer player() {
        return player;
    }

    public int questIndex() {
        return questIndex;
    }
}
