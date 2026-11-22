package net.yvanchuxiuzhen.system.xiulian.api;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/** 开始闭关时由功法所有者查询当前书的研习状态，区分未装备、未研习和他人所有。 */
public final class RetreatTechniqueEvent extends Event {
    public enum Access {
        NONE, UNLEARNED, OTHER, OWNED
    }

    private final ServerPlayer player;
    private Access access = Access.NONE;

    public RetreatTechniqueEvent(ServerPlayer player) {
        this.player = player;
    }

    public ServerPlayer player() {
        return player;
    }

    public Access access() {
        return access;
    }

    public void setAccess(Access access) {
        this.access = access;
    }
}
