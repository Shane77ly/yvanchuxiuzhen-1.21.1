package net.yvanchuxiuzhen.smoke;

import com.mojang.logging.LogUtils;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkGenerationTask;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StaticCache2D;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import org.slf4j.Logger;

/** 所有客户端冒烟场景共用的有界退出观察；不调度区块、不改变票据、生成引用或原生保存。 */
final class WorldStopDiagnostics {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Field UPDATING = field(ChunkMap.class, "updatingChunkMap");
    private static final Field VISIBLE = field(ChunkMap.class, "visibleChunkMap");
    private static final Field UNLOADING = field(ChunkMap.class, "pendingUnloads");
    private static final Field PENDING_TASKS = field(ChunkMap.class, "pendingGenerationTasks");
    private static final Field UNLOAD_QUEUE = field(ChunkMap.class, "unloadQueue");
    private static final Field CURRENT_TASK = field(GenerationChunkHolder.class, "task");
    private static final Field STARTED_WORK = field(GenerationChunkHolder.class, "startedWork");
    private static final Field ALLOWED_STATUS = field(GenerationChunkHolder.class, "highestAllowedStatus");
    private static final Field TASK_CACHE = field(ChunkGenerationTask.class, "cache");
    private static final Field SCHEDULED_STATUS = field(ChunkGenerationTask.class, "scheduledStatus");
    private static final Field CANCELLED = field(ChunkGenerationTask.class, "markedForCancellation");
    private static final int DETAIL_LIMIT = 24;
    private static final int POSITION_LIMIT = 256;
    private final MinecraftServer server;
    private final AtomicBoolean finished = new AtomicBoolean();
    private final Consumer<ServerStoppingEvent> stoppingListener = this::stopping;
    private final Consumer<ServerStoppedEvent> stoppedListener = this::stopped;
    private final Map<Object, Integer> identities = new IdentityHashMap<>();
    private Thread observer;

    private WorldStopDiagnostics(MinecraftServer server) {
        this.server = server;
    }

    static void arm(MinecraftServer server) {
        WorldStopDiagnostics diagnostics = new WorldStopDiagnostics(server);
        NeoForge.EVENT_BUS.addListener(ServerStoppingEvent.class, diagnostics.stoppingListener);
        NeoForge.EVENT_BUS.addListener(ServerStoppedEvent.class, diagnostics.stoppedListener);
        LOGGER.info("TRACE 公共停服观察 stage=armed limits=1,5,15,30,45,55s noChunkRequests=true noQueueDraining=true");
    }

    /** 可变区块映射和待派发任务列表只在原生停服事件的服务端线程读取。 */
    private void stopping(ServerStoppingEvent event) {
        if (event.getServer() != server) return;
        try {
            List<LevelCapture> levels = new ArrayList<>();
            for (ServerLevel level : server.getAllLevels()) {
                ChunkMap map = level.getChunkSource().chunkMap;
                Map<?, ?> updating = (Map<?, ?>) UPDATING.get(map);
                Map<?, ?> unloading = (Map<?, ?>) UNLOADING.get(map);
                Set<ChunkHolder> holders = Collections.newSetFromMap(new IdentityHashMap<>());
                updating.values().forEach(value -> holders.add(ChunkHolder.class.cast(value)));
                unloading.values().forEach(value -> holders.add(ChunkHolder.class.cast(value)));
                List<HolderCapture> captured = new ArrayList<>();
                Map<ChunkGenerationTask, Boolean> tasks = new IdentityHashMap<>();
                for (ChunkHolder holder : holders) {
                    long key = holder.getPos().toLong();
                    CompletableFuture<?> save = holder.getSaveSyncFuture();
                    captured.add(new HolderCapture(holder, updating.get(key) == holder, unloading.get(key) == holder,
                            holder.getTicketLevel(), holder.getQueueLevel(), save, holder.getGenerationRefCount(),
                            save.isDone(), save.isCompletedExceptionally()));
                    discoverTask(holder, tasks);
                }
                for (Object task : (List<?>) PENDING_TASKS.get(map)) tasks.put((ChunkGenerationTask) task, Boolean.TRUE);
                Queue<?> queue = (Queue<?>) UNLOAD_QUEUE.get(map);
                levels.add(new LevelCapture(level.dimension().location().toString(), map, captured, tasks, queue));
                LOGGER.info("TRACE 公共停服观察 stage=server_stopping dimension={} updating={} pendingUnloads={} pendingGenerationTasks={} unloadQueueAtCapture={} capturedTasks={}",
                        level.dimension().location(), updating.size(), unloading.size(), ((List<?>) PENDING_TASKS.get(map)).size(), queue.size(), tasks.size());
            }
            observer = Thread.ofPlatform().daemon(true).name("R06 world stop observer").start(() -> observe(levels));
        } catch (ReflectiveOperationException exception) {
            LOGGER.error("公共停服观察字段读取失败", exception);
            close();
        }
    }

    private void stopped(ServerStoppedEvent event) {
        if (event.getServer() != server) return;
        LOGGER.info("TRACE 公共停服观察 stage=server_stopped reachedAfterNativeStopCall=true notASaveSuccessAssertion=true");
        close();
    }

    private void close() {
        finished.set(true);
        NeoForge.EVENT_BUS.unregister(stoppingListener);
        NeoForge.EVENT_BUS.unregister(stoppedListener);
        if (observer != null) observer.interrupt();
    }

    private void observe(List<LevelCapture> levels) {
        long started = System.nanoTime();
        try {
            for (int seconds : new int[]{1, 5, 15, 30, 45, 55}) {
                long remaining = started + seconds * 1_000_000_000L - System.nanoTime();
                if (remaining > 0) Thread.sleep(remaining / 1_000_000L, (int) (remaining % 1_000_000L));
                if (finished.get()) return;
                for (LevelCapture level : levels) sample(level, seconds);
                LOGGER.info("TRACE 公共停服观察 elapsed={} serverThreadState={} serverStack={}", seconds,
                        server.getRunningThread().getState(), List.of(server.getRunningThread().getStackTrace()));
            }
            LOGGER.info("TRACE 公共停服观察 stage=observation_limit serverStoppedEventObserved=false noTerminationPerformed=true");
        } catch (InterruptedException exception) {
            if (!finished.get()) LOGGER.error("公共停服观察被外部中断", exception);
            Thread.currentThread().interrupt();
        } catch (ReflectiveOperationException exception) {
            LOGGER.error("公共停服观察字段读取失败", exception);
        } finally {
            close();
        }
    }

    /** 后台只遍历停服快照、原生不可变可见映射、任务不可变cache及原子Future列表。 */
    private void sample(LevelCapture level, int seconds) throws ReflectiveOperationException {
        Map<ChunkHolder, HolderCapture> holders = new IdentityHashMap<>();
        for (HolderCapture capture : level.holders) holders.put(capture.holder, capture);
        Map<?, ?> visible = (Map<?, ?>) VISIBLE.get(level.map);
        for (Object value : visible.values()) holders.putIfAbsent((ChunkHolder) value, null);
        for (ChunkHolder holder : holders.keySet()) discoverTask(holder, level.tasks);
        for (ChunkGenerationTask task : level.tasks.keySet()) {
            StaticCache2D<?> cache = (StaticCache2D<?>) TASK_CACHE.get(task);
            cache.forEach(value -> holders.putIfAbsent((ChunkHolder) value, null));
        }
        int refsTotal = 0;
        int immediateCandidates = 0;
        int waiting = 0;
        int exceptional = 0;
        int detailed = 0;
        List<String> positions = new ArrayList<>();
        List<Map.Entry<ChunkHolder, HolderCapture>> immediateDetails = new ArrayList<>();
        List<Map.Entry<ChunkHolder, HolderCapture>> otherDetails = new ArrayList<>();
        for (Map.Entry<ChunkHolder, HolderCapture> entry : holders.entrySet()) {
            ChunkHolder holder = entry.getKey();
            int refs = holder.getGenerationRefCount();
            CompletableFuture<?> save = holder.getSaveSyncFuture();
            refsTotal += refs;
            if (!save.isDone()) waiting++;
            if (save.isCompletedExceptionally()) exceptional++;
            if (refs != 0 && save.isDone()) {
                immediateCandidates++;
                if (positions.size() < POSITION_LIMIT) positions.add(holder.getPos() + "#" + id(holder) + ":ref=" + refs);
                immediateDetails.add(entry);
            } else if (refs != 0 || !save.isDone() || save.isCompletedExceptionally()) {
                otherDetails.add(entry);
            }
        }
        LOGGER.info("TRACE 公共停服观察 elapsed={} dimension={} knownHolders={} visible={} generationRefs={} waitingSaves={} exceptionalSaves={} immediateRequeueCandidates={} candidatePositions={} positionsTruncated={} unloadHead={} consistency=observational_nonAtomic_plainFieldsMayBeStale",
                seconds, level.dimension, holders.size(), visible.size(), refsTotal, waiting, exceptional,
                immediateCandidates, positions, immediateCandidates > positions.size(), identity(level.unloadQueue.peek()));
        immediateDetails.addAll(otherDetails);
        for (Map.Entry<ChunkHolder, HolderCapture> entry : immediateDetails) {
            ChunkHolder holder = entry.getKey();
            int refs = holder.getGenerationRefCount();
            CompletableFuture<?> save = holder.getSaveSyncFuture();
            if (refs == 0 && save.isDone() && !save.isCompletedExceptionally()) continue;
            if (detailed++ >= DETAIL_LIMIT) continue;
            List<String> futures = new ArrayList<>();
            String latestFuture = "none";
            String latestPending = "none";
            for (var pair : holder.getAllFutures()) {
                CompletableFuture<?> future = pair.getSecond();
                if (future == null) continue;
                latestFuture = pair.getFirst().toString();
                if (!future.isDone()) latestPending = pair.getFirst().toString();
                futures.add(pair.getFirst() + "=" + future(future));
            }
            List<String> caches = new ArrayList<>();
            for (ChunkGenerationTask task : level.tasks.keySet()) {
                StaticCache2D<?> cache = (StaticCache2D<?>) TASK_CACHE.get(task);
                if (cache.contains(holder.getPos().x, holder.getPos().z)) caches.add(task(task));
            }
            HolderCapture captured = entry.getValue();
            ChunkGenerationTask current = currentTask(holder);
            LOGGER.info("TRACE 公共停服观察 holder={} pos={} ref={} save={} stopCapture={} inCurrentVisibleSnapshot={} currentTicketSample={} currentQueueSample={} startedWork={} allowedStatus={} latestFuture={} latestPending={} statusFutures={} currentTask={} cacheMembershipCandidates={} membershipDoesNotProveOutstandingClaim=true",
                    id(holder), holder.getPos(), refs, future(save), captured == null ? "not_in_stop_snapshot" : captured.summary(),
                    visible.get(holder.getPos().toLong()) == holder, holder.getTicketLevel(), holder.getQueueLevel(),
                    ((AtomicReference<?>) STARTED_WORK.get(holder)).get(), ALLOWED_STATUS.get(holder),
                    latestFuture, latestPending, futures, current == null ? "none" : task(current), caches);
        }
    }

    private static void discoverTask(ChunkHolder holder, Map<ChunkGenerationTask, Boolean> tasks) throws IllegalAccessException {
        ChunkGenerationTask task = currentTask(holder);
        if (task != null) tasks.put(task, Boolean.TRUE);
    }

    private static ChunkGenerationTask currentTask(ChunkHolder holder) throws IllegalAccessException {
        return (ChunkGenerationTask) ((AtomicReference<?>) CURRENT_TASK.get(holder)).get();
    }

    private String task(ChunkGenerationTask task) throws IllegalAccessException {
        return "#" + id(task) + ":center=" + task.getCenter().getPos() + ":target=" + task.targetStatus
                + ":scheduledStatusSample=" + SCHEDULED_STATUS.get(task) + ":cancelled=" + CANCELLED.get(task)
                + ":cache=" + TASK_CACHE.get(task);
    }

    private String future(CompletableFuture<?> future) {
        String failure = "none";
        if (future.isCompletedExceptionally()) {
            try {
                future.getNow(null);
            } catch (CompletionException exception) {
                Throwable cause = exception.getCause() == null ? exception : exception.getCause();
                failure = String.valueOf(cause) + ":stack=" + List.of(cause.getStackTrace());
            } catch (CancellationException exception) {
                failure = exception.toString();
            }
        }
        return "#" + id(future) + ":identity=" + Integer.toHexString(System.identityHashCode(future))
                + ":done=" + future.isDone() + ":exceptional=" + future.isCompletedExceptionally()
                + ":cancelled=" + future.isCancelled() + ":failure=" + failure;
    }

    private String identity(Object value) {
        return value == null ? "none" : value.getClass().getName() + "#" + id(value);
    }

    private int id(Object object) {
        return identities.computeIfAbsent(object, ignored -> identities.size() + 1);
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

    private record LevelCapture(String dimension, ChunkMap map, List<HolderCapture> holders,
            Map<ChunkGenerationTask, Boolean> tasks, Queue<?> unloadQueue) {
    }

    private record HolderCapture(ChunkHolder holder, boolean updating, boolean pendingUnload, int ticket, int queue,
            CompletableFuture<?> save, int refs, boolean saveDone, boolean saveExceptional) {
        String summary() {
            return "updating=" + updating + ",pendingUnload=" + pendingUnload + ",ticket=" + ticket + ",queue=" + queue
                    + ",ref=" + refs + ",saveIdentity=" + Integer.toHexString(System.identityHashCode(save))
                    + ",saveDone=" + saveDone + ",saveExceptional=" + saveExceptional;
        }
    }
}

