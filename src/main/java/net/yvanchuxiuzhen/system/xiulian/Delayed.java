package net.yvanchuxiuzhen.system.xiulian;

import java.util.ArrayList;
import java.util.List;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 服务端 tick 结束时倒数执行的延后动作，对应来源用 {@code ServerTickEvent} 计数等待若干 tick 的写法
 * （升级界面 20 tick、阳寿已尽后 40 tick）。服务端停止时未执行的动作随之丢弃，与来源一致。
 */
final class Delayed {
    private static final List<Pending> PENDING = new ArrayList<>();

    private static final class Pending {
        private int remaining;
        private final Runnable action;

        private Pending(int remaining, Runnable action) {
            this.remaining = remaining;
            this.action = action;
        }
    }

    private Delayed() {
    }

    static void run(int ticks, Runnable action) {
        PENDING.add(new Pending(ticks, action));
    }

    static void onServerTick(ServerTickEvent.Post event) {
        List<Pending> due = new ArrayList<>();
        PENDING.removeIf(pending -> {
            pending.remaining--;
            if (pending.remaining <= 0) {
                due.add(pending);
                return true;
            }
            return false;
        });
        due.forEach(pending -> pending.action.run());
    }
}
