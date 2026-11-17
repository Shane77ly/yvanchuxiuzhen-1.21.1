package net.yvanchuxiuzhen.smoke;

import com.mojang.logging.LogUtils;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.server.level.ServerLevel;
import org.slf4j.Logger;

/** 仅在冒烟源集中绑定卸载回调的注册Future，区分它与回调执行时的当前Future。 */
public final class WorldStopFutureDiagnostics {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Field UPDATING = field(ChunkMap.class, "updatingChunkMap");
    private static final Field PENDING = field(ChunkMap.class, "pendingUnloads");
    private static final Field CURRENT_TASK = field(GenerationChunkHolder.class, "task");
    private static volatile State active;

    private WorldStopFutureDiagnostics() {
    }

    static void arm(MinecraftServer server) {
        active = new State(server);
        LOGGER.info("TRACE 卸载注册Future关联 stage=armed binding=per_callback_closure originalExecutor=true originalAction=true");
    }

    static void disarm(MinecraftServer server) {
        State state = active;
        if (state == null || state.server != server) return;
        state.closed.set(true);
        active = null;
        LOGGER.info("TRACE 卸载注册Future关联 stage=disarmed registrations={} callbacks={} notReadySamples={} futureChangedSamples={} pendingMismatchSamples={} nonzeroRefSamples={} sampleErrors={}",
                state.registrations.get(), state.callbacks.get(), state.notReady.get(), state.changed.get(),
                state.pendingMismatch.get(), state.nonzeroRefs.get(), state.errors.get());
    }

    /** 未挂载时直接返回原Runnable；挂载时每个闭包只关联本次thenRunAsync接收的Future。 */
    public static Runnable wrap(ChunkMap map, ServerLevel level, ChunkHolder holder, long chunkPos,
            CompletableFuture<?> parent, Runnable action) {
        State state = active;
        if (state == null || state.closed.get() || state.server != level.getServer()) return action;
        long registration = state.registrations.incrementAndGet();
        long registeredAt = System.nanoTime();
        boolean registeredDone = parent.isDone();
        boolean registeredExceptional = parent.isCompletedExceptionally();
        return () -> {
            observe(state, map, level, holder, chunkPos, parent, action, registration, registeredAt,
                    registeredDone, registeredExceptional);
            // 原生动作只调用一次；其异常由原来的thenRunAsync完成链处理。
            action.run();
        };
    }

    private static void observe(State state, ChunkMap map, ServerLevel level, ChunkHolder holder, long chunkPos,
            CompletableFuture<?> parent, Runnable action, long registration, long registeredAt,
            boolean registeredDone, boolean registeredExceptional) {
        if (state.closed.get()) return;
        try {
            long callbacks = state.callbacks.incrementAndGet();
            CompletableFuture<?> current = holder.getSaveSyncFuture();
            boolean same = parent == current;
            int refs = holder.getGenerationRefCount();
            boolean pendingSame = ((Map<?, ?>) PENDING.get(map)).get(chunkPos) == holder;
            boolean updatingSame = ((Map<?, ?>) UPDATING.get(map)).get(chunkPos) == holder;
            boolean ready = holder.isReadyForSaving();
            if (!same) state.changed.incrementAndGet();
            if (!pendingSame) state.pendingMismatch.incrementAndGet();
            if (refs != 0) state.nonzeroRefs.incrementAndGet();
            long notReady = ready ? 0 : state.notReady.incrementAndGet();
            // 前32次及随后二次幂采样限制日志量；全部计数保留到解除挂载。
            if (!selected(callbacks) && (ready || !selected(notReady))) return;
            Object task = ((AtomicReference<?>) CURRENT_TASK.get(holder)).get();
            LOGGER.info("TRACE 卸载注册Future关联 dimension={} pos={} registration={} callbacks={} notReadySample={} delayMicros={} action={} registeredParent={} registeredDone={} registeredExceptional={} current={} parentCurrentSame={} pendingSame={} updatingSame={} ref={} readySample={} currentTask={} consistency=one_callback_nonAtomic_samples",
                    level.dimension().location(), holder.getPos(), registration, callbacks, notReady,
                    (System.nanoTime() - registeredAt) / 1000L, identity(action), future(parent), registeredDone,
                    registeredExceptional, future(current), same, pendingSame, updatingSame, refs, ready,
                    task == null ? "none" : identity(task));
        } catch (ReflectiveOperationException | RuntimeException exception) {
            long errors = state.errors.incrementAndGet();
            if (selected(errors)) {
                LOGGER.warn("卸载注册Future诊断采样失败，保留原生动作及完成链", exception);
            }
        }
    }

    private static boolean selected(long count) {
        return count <= 32 || (count & (count - 1)) == 0;
    }

    private static String future(CompletableFuture<?> future) {
        return identity(future) + ":done=" + future.isDone() + ":exceptional=" + future.isCompletedExceptionally()
                + ":cancelled=" + future.isCancelled();
    }

    private static String identity(Object value) {
        return "#" + Integer.toHexString(System.identityHashCode(value));
    }

    private static Field field(Class<?> owner, String name) {
        try {
            Field field = owner.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("精确21.1.265诊断字段不存在 " + owner.getName() + "." + name, exception);
        }
    }

    private static final class State {
        private final MinecraftServer server;
        private final AtomicBoolean closed = new AtomicBoolean();
        private final AtomicLong registrations = new AtomicLong();
        private final AtomicLong callbacks = new AtomicLong();
        private final AtomicLong notReady = new AtomicLong();
        private final AtomicLong changed = new AtomicLong();
        private final AtomicLong pendingMismatch = new AtomicLong();
        private final AtomicLong nonzeroRefs = new AtomicLong();
        private final AtomicLong errors = new AtomicLong();

        private State(MinecraftServer server) {
            this.server = server;
        }
    }
}
