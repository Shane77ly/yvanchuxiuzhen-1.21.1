package net.yvanchuxiuzhen.system.world;

import com.mojang.logging.LogUtils;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.status.ChunkStatus;

/** 首入写入前生成目标地形，每片只释放自有区块票；存盘完成由实际 I/O 结果判定。 */
final class DifuTerrain {
    private static final TicketType<ChunkPos> TICKET = TicketType.create("yvanchuxiuzhen_difu_terrain", Comparator.comparingLong(ChunkPos::toLong));
    private static final int SIDE = 4;
    private static final int MAX_WRITES = 8;
    private final ArrayDeque<CompletableFuture<Void>> writes = new ArrayDeque<>();
    private int submitted;
    private int peak;

    static void prepare(ServerLevel level, BlockPos origin, Vec3i size) {
        var batch = new DifuTerrain();
        var chunks = level.getChunkSource();
        var access = (DifuWriteAccess) chunks.chunkMap;
        int minX = (origin.getX() - 1) >> 4, minZ = (origin.getZ() - 1) >> 4;
        int maxX = (origin.getX() + size.getX()) >> 4, maxZ = (origin.getZ() + size.getZ()) >> 4;
        access.yvanchuxiuzhen$difuWrites(batch::submitted);
        try {
            int tiles = 0;
            for (int x = minX; x <= maxX; x += SIDE) {
                for (int z = minZ; z <= maxZ; z += SIDE) {
                    int endX = Math.min(x + SIDE - 1, maxX), endZ = Math.min(z + SIDE - 1, maxZ);
                    for (int cx = x; cx <= endX; cx++) for (int cz = z; cz <= endZ; cz++) {
                        var pos = new ChunkPos(cx, cz);
                        chunks.addRegionTicket(TICKET, pos, 0, pos);
                    }
                    try {
                        chunks.pollTask();
                        for (int cx = x; cx <= endX; cx++) for (int cz = z; cz <= endZ; cz++) {
                            if (chunks.getChunk(cx, cz, ChunkStatus.FULL, false) == null) {
                                throw new IllegalStateException("地府地形未就绪：" + cx + "," + cz);
                            }
                        }
                    } finally {
                        for (int cx = x; cx <= endX; cx++) for (int cz = z; cz <= endZ; cz++) {
                            var pos = new ChunkPos(cx, cz);
                            chunks.removeRegionTicket(TICKET, pos, 0, pos);
                        }
                    }
                    chunks.save(true);
                    while (!batch.writes.isEmpty()) batch.writes.removeFirst().join();
                    LogUtils.getLogger().info("地府预生成：片={} 提交写盘={} 在途峰值={}", ++tiles, batch.submitted, batch.peak);
                }
            }
        } finally {
            access.yvanchuxiuzhen$difuWrites(null);
        }
    }

    private void submitted(CompletableFuture<Void> result) {
        writes.removeIf(write -> {
            if (!write.isDone()) return false;
            write.join();
            return true;
        });
        submitted++;
        writes.addLast(result);
        peak = Math.max(peak, writes.size());
        if (writes.size() == MAX_WRITES) writes.getFirst().join();
    }
}
