package net.yvanchuxiuzhen.system.lianqi;

import java.util.ArrayList;
import java.util.List;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** 炼器工作台在服务端刻结束时执行的延后操作；停止服务端时丢弃尚未执行的回调。 */
public final class LianqiDelays {
    private static final List<Pending> PENDING = new ArrayList<>();

    private LianqiDelays() {
    }

    public static void run(int ticks, Runnable action) {
        PENDING.add(new Pending(ticks, action));
    }

    public static void tick(ServerTickEvent.Post event) {
        List<Runnable> due = new ArrayList<>();
        PENDING.removeIf(pending -> {
            if (--pending.ticks > 0) return false;
            due.add(pending.action);
            return true;
        });
        due.forEach(Runnable::run);
    }

    public static void stopped(ServerStoppedEvent event) {
        PENDING.clear();
    }

    private static final class Pending {
        private int ticks;
        private final Runnable action;

        private Pending(int ticks, Runnable action) {
            this.ticks = ticks;
            this.action = action;
        }
    }
}
