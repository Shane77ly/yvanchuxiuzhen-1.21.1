package net.yvanchuxiuzhen.system.world.diagnostic;

import com.mojang.logging.LogUtils;
import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkGenerationTask;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.server.level.ServerLevel;
import org.slf4j.Logger;

/**
 * 在开发冒烟场景中记录实际的区块卸载重排回调；未被冒烟诊断挂载时保持零开销返回。
 * 该类只观察回调状态，不改变区块、票据、Future 或保存流程。
 */
public final class WorldStopRequeueDiagnostics {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Field LEVEL = field(ChunkMap.class, "level");
    private static final Field UPDATING = field(ChunkMap.class, "updatingChunkMap");
    private static final Field PENDING_UNLOADS = field(ChunkMap.class, "pendingUnloads");
    private static final Field PENDING_GENERATION_TASKS = field(ChunkMap.class, "pendingGenerationTasks");
    private static final Field CURRENT_TASK = field(GenerationChunkHolder.class, "task");
    private static final Field TASK_SCHEDULED_STATUS = field(ChunkGenerationTask.class, "scheduledStatus");
    private static final Field TASK_CANCELLED = field(ChunkGenerationTask.class, "markedForCancellation");
    private static volatile MinecraftServer armedServer;
    private static final Map<ChunkHolder, RequeueState> states = new IdentityHashMap<>();

    private WorldStopRequeueDiagnostics() {
    }

    /** 只由Smoke世界启动阶段调用；生产运行没有调用者。 */
    public static void arm(MinecraftServer server) {
        armedServer = server;
        states.clear();
        LOGGER.info("TRACE 公共停服重排诊断 stage=armed callbackOnly=true threshold=1,10");
    }

    /** 停服结束或诊断异常时解除挂载，避免跨世界保留holder引用。 */
    public static void disarm(MinecraftServer server) {
        if (armedServer != server) return;
        armedServer = null;
        states.clear();
    }

    /**
     * 在Smoke断开前读取服务端各维度的生成引用收敛状态；未挂载诊断时保持原有退出等待条件。
     * 调用发生在服务端线程，仅读取原生映射、任务列表和holder状态。
     */
    public static Readiness readiness(MinecraftServer server) {
        if (armedServer != server) return Readiness.inactive();
        try {
            int pendingGenerationTasks = 0;
            int generationRefs = 0;
            int notReadyPendingUnloads = 0;
            for (ServerLevel level : server.getAllLevels()) {
                ChunkMap map = level.getChunkSource().chunkMap;
                pendingGenerationTasks += ((List<?>) PENDING_GENERATION_TASKS.get(map)).size();
                Map<?, ?> updating = (Map<?, ?>) UPDATING.get(map);
                Map<?, ?> pendingUnloads = (Map<?, ?>) PENDING_UNLOADS.get(map);
                IdentityHashMap<ChunkHolder, Boolean> holders = new IdentityHashMap<>();
                updating.values().forEach(value -> holders.put((ChunkHolder) value, Boolean.TRUE));
                pendingUnloads.values().forEach(value -> holders.put((ChunkHolder) value, Boolean.TRUE));
                for (ChunkHolder holder : holders.keySet()) {
                    generationRefs += holder.getGenerationRefCount();
                }
                for (Object value : pendingUnloads.values()) {
                    if (!((ChunkHolder) value).isReadyForSaving()) notReadyPendingUnloads++;
                }
            }
            return new Readiness(true, pendingGenerationTasks, generationRefs, notReadyPendingUnloads);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            LOGGER.warn("Smoke停服收敛状态读取失败，回退到原有退出等待条件", exception);
            return Readiness.inactive();
        }
    }

    /**
     * 由ChunkMap的scheduleUnload完成回调入口调用。方法必须吞掉诊断自身异常，不能影响原生回调。
     */
    public static void observe(ChunkMap map, ChunkHolder holder, long chunkPos) {
        MinecraftServer server = armedServer;
        if (server == null) return;
        try {
            if (((ServerLevel) LEVEL.get(map)).getServer() != server
                    || holder.isReadyForSaving()) {
                return;
            }
            CompletableFuture<?> save = holder.getSaveSyncFuture();
            Object pendingHolder = ((Map<?, ?>) PENDING_UNLOADS.get(map)).get(chunkPos);
            RequeueState state = states.computeIfAbsent(holder, ignored -> new RequeueState(System.nanoTime()));
            state.count++;
            state.lastNanos = System.nanoTime();
            if (state.count == 1 || state.count == 10) {
                LOGGER.info("TRACE 公共停服重排诊断 dimension={} pos={} count={} durationMs={} pendingSame={} ref={} ready={} save={} currentTask={}",
                        ((net.minecraft.server.level.ServerLevel) LEVEL.get(map)).dimension().location(), holder.getPos(), state.count,
                        (state.lastNanos - state.firstNanos) / 1_000_000L, pendingHolder == holder,
                        holder.getGenerationRefCount(), holder.isReadyForSaving(), future(save), task(holder));
            }
        } catch (ReflectiveOperationException | RuntimeException exception) {
            LOGGER.warn("公共停服重排诊断采样失败，已忽略以保持原生卸载流程", exception);
        }
    }

    private static String future(CompletableFuture<?> future) {
        return "#" + Integer.toHexString(System.identityHashCode(future)) + ":done=" + future.isDone()
                + ":exceptional=" + future.isCompletedExceptionally() + ":cancelled=" + future.isCancelled();
    }

    private static String task(ChunkHolder holder) throws IllegalAccessException {
        AtomicReference<?> reference = (AtomicReference<?>) CURRENT_TASK.get(holder);
        Object task = reference.get();
        if (task == null) return "none";
        return "#" + Integer.toHexString(System.identityHashCode(task))
                + ":scheduled=" + TASK_SCHEDULED_STATUS.get(task)
                + ":cancelled=" + TASK_CANCELLED.getBoolean(task);
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

    private static final class RequeueState {
        private final long firstNanos;
        private long lastNanos;
        private int count;

        private RequeueState(long firstNanos) {
            this.firstNanos = firstNanos;
            this.lastNanos = firstNanos;
        }
    }

    public record Readiness(boolean active, int pendingGenerationTasks, int generationRefs,
            int notReadyPendingUnloads) {
        private static Readiness inactive() {
            return new Readiness(false, 0, 0, 0);
        }

        public boolean settled() {
            return !active || (pendingGenerationTasks == 0 && generationRefs == 0 && notReadyPendingUnloads == 0);
        }
    }
}
