package net.yvanchuxiuzhen.system.npc.api;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/** 玩家再次与已为其准备的乾坤道人交互。 */
public final class QiankunDialogueRequestEvent extends Event {
    private final ServerPlayer player;

    public QiankunDialogueRequestEvent(ServerPlayer player) {
        this.player = player;
    }

    public ServerPlayer player() {
        return player;
    }
}
