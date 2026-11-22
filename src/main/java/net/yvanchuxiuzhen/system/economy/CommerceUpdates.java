package net.yvanchuxiuzhen.system.economy;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** 商店按来源在打开后的第二个服务端END刻初始化分页和钱包。 */
public final class CommerceUpdates {
    private static final List<Pending> PENDING = new ArrayList<>();
    private static final List<Action> ACTIONS = new ArrayList<>();
    private CommerceUpdates() { }
    public static void opened(ServerPlayer player) { PENDING.add(new Pending(player)); }
    public static void later(int ticks, Runnable action) { ACTIONS.add(new Action(ticks, action)); }
    public static void tick(ServerTickEvent.Post event) {
        List<Pending> due = new ArrayList<>();
        PENDING.removeIf(pending -> { if (--pending.ticks > 0) return false; due.add(pending); return true; });
        for (Pending pending : due) CommerceTrading.opened(pending.player);
        List<Action> ready = new ArrayList<>();
        ACTIONS.removeIf(action -> { if (--action.ticks > 0) return false; ready.add(action); return true; });
        for (Action action : ready) action.action.run();
    }
    public static void stopped(ServerStoppedEvent event) { PENDING.clear(); ACTIONS.clear(); }
    private static final class Action {
        private int ticks;
        private final Runnable action;
        private Action(int ticks, Runnable action) { this.ticks = ticks; this.action = action; }
    }
    private static final class Pending {
        private final ServerPlayer player;
        private int ticks = 2;
        private Pending(ServerPlayer player) { this.player = player; }
    }
}
