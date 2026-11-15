package net.yvanchuxiuzhen.system.xiulian.api;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/** 出身补给物品通过领取条件并保存次数后，同步交接该次来源奖励。 */
public final class BirthSupplyEvent extends Event {
    public enum Kind { INHERITANCE, FAMILY }

    private final ServerPlayer player;
    private final Kind kind;
    private final double stage;

    public BirthSupplyEvent(ServerPlayer player, Kind kind, double stage) {
        this.player = player;
        this.kind = kind;
        this.stage = stage;
    }

    public ServerPlayer player() { return player; }
    public Kind kind() { return kind; }
    public double stage() { return stage; }
}
