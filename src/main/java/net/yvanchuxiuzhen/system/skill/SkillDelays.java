package net.yvanchuxiuzhen.system.skill;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * 施放过程里的延时任务（来源 {@code new Object() { … }.start(world, 刻数)}：在服务端每刻结束事件里累计，到点执行一次）。
 * 任务只放在内存里，服务端停止时丢弃，与来源的事件监听一致。
 */
public final class SkillDelays {
    private static final List<Pending> PENDING = new ArrayList<>();

    private SkillDelays() {
    }

    private static final class Pending {
        private final Runnable task;
        private int remaining;

        private Pending(int remaining, Runnable task) {
            this.remaining = remaining;
            this.task = task;
        }
    }

    static void register() {
        NeoForge.EVENT_BUS.addListener(SkillDelays::onServerTick);
    }

    /** {@code ticks} 个服务端刻之后执行 {@code task}。 */
    public static void after(int ticks, Runnable task) {
        PENDING.add(new Pending(ticks, task));
    }

    private static void onServerTick(ServerTickEvent.Post event) {
        Iterator<Pending> iterator = new ArrayList<>(PENDING).iterator();
        while (iterator.hasNext()) {
            Pending pending = iterator.next();
            if (--pending.remaining <= 0) {
                PENDING.remove(pending);
                pending.task.run();
            }
        }
    }
}
