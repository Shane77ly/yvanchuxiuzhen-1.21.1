package net.yvanchuxiuzhen.system.world.diagnostic;

import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import org.slf4j.Logger;

/** 独立冒烟只观察指定原生装饰中心的目标模板加载与放置，不改变生成输入或结果。 */
public final class RenjianRandomDiagnostics {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static volatile Request request;
    private static volatile Result result;

    private RenjianRandomDiagnostics() {
    }

    public static void begin(ServerLevel level, ChunkPos chunk, ResourceLocation template) {
        result = null;
        request = new Request(level, chunk, template);
    }

    public static Result end() {
        request = null;
        Result observed = result;
        result = null;
        return observed;
    }

    public static boolean loading(WorldGenLevel level, BlockPos decorationOrigin, ResourceLocation template, BlockPos origin) {
        Request active = request;
        if (active == null || !active.template().equals(template) || !(level instanceof WorldGenRegion region)
                || region.getLevel() != active.level() || !region.getCenter().equals(active.chunk())) {
            return false;
        }
        LOGGER.info("TRACE 人间随机结构 stage=load_begin seed={} chunk={} decoration={} template={} origin={} heapUsedBytes={}",
                active.level().getSeed(), active.chunk(), decorationOrigin, template, origin, heapUsed());
        return true;
    }

    public static void placed(ResourceLocation template, BlockPos origin, Mirror mirror, Rotation rotation, Vec3i size, boolean placed) {
        LOGGER.info("TRACE 人间随机结构 stage=place_end template={} origin={} mirror={} rotation={} size={} placed={} heapUsedBytes={}",
                template, origin, mirror, rotation, size, placed, heapUsed());
        result = new Result(origin, mirror, rotation, size, placed);
    }

    private static long heapUsed() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    private record Request(ServerLevel level, ChunkPos chunk, ResourceLocation template) {
    }

    public record Result(BlockPos origin, Mirror mirror, Rotation rotation, Vec3i size, boolean placed) {
    }
}
