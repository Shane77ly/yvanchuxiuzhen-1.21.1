package net.yvanchuxiuzhen.system.world.api;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/** 每次真实跨入人间道时，在固定场景的同步部分之后发布。 */
public final class RenjianEnteredEvent extends Event {
    private final ServerPlayer player;

    public RenjianEnteredEvent(ServerPlayer player) {
        this.player = player;
    }

    public ServerPlayer player() {
        return player;
    }
}
