package net.yvanchuxiuzhen.system.xiulian.api;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/** 观想成功时在减速之后、寿元与资源结算之前发出，由烹饪炼化酒精。 */
public final class MeditationRefineEvent extends Event {
    private final ServerPlayer player;

    public MeditationRefineEvent(ServerPlayer player) {
        this.player = player;
    }

    public ServerPlayer player() {
        return player;
    }
}
