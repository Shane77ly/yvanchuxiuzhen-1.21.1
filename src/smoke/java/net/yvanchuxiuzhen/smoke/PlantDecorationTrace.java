package net.yvanchuxiuzhen.smoke;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/** 只收集本次定位所请求世界的原生装饰结果，不改变随机流、区块或返回值。 */
public final class PlantDecorationTrace {
    private static volatile PlantDecorationTrace active;
    private final ServerLevel level;
    private final Map<PlacedFeature, ResourceLocation> placed;
    private final Map<ConfiguredFeature<?, ?>, ResourceLocation> configured;
    private final Map<ChunkPos, ConcurrentLinkedQueue<String>> events = new ConcurrentHashMap<>();
    private final Map<ChunkPos, ConcurrentLinkedQueue<Placement>> successful = new ConcurrentHashMap<>();

    PlantDecorationTrace(ServerLevel level, Map<PlacedFeature, ResourceLocation> placed,
            Map<ConfiguredFeature<?, ?>, ResourceLocation> configured) {
        this.level = level;
        this.placed = Map.copyOf(placed);
        this.configured = Map.copyOf(configured);
    }

    void begin() {
        active = this;
    }

    void end() {
        active = null;
    }

    List<String> events(ChunkPos chunk) {
        var matches = events.get(chunk);
        return matches == null ? List.of() : List.copyOf(matches);
    }

    List<BlockPos> successfulOrigins(ChunkPos chunk, ResourceLocation feature) {
        var matches = successful.get(chunk);
        if (matches == null) return List.of();
        return matches.stream().filter(placement -> placement.feature().equals(feature)).map(Placement::origin).toList();
    }

    public static void placed(WorldGenLevel level, PlacedFeature feature, boolean result) {
        PlantDecorationTrace trace = active;
        if (trace == null || !(level instanceof WorldGenRegion region) || region.getLevel() != trace.level) return;
        ResourceLocation id = trace.placed.get(feature);
        if (id != null) trace.record(region, "placed " + id + " result=" + result);
    }

    public static void configured(WorldGenLevel level, ConfiguredFeature<?, ?> feature, BlockPos origin, boolean result) {
        PlantDecorationTrace trace = active;
        if (trace == null || !(level instanceof WorldGenRegion region) || region.getLevel() != trace.level) return;
        ResourceLocation id = trace.configured.get(feature);
        if (id == null) return;
        trace.record(region, "configured " + id + " origin=" + origin.toShortString() + " result=" + result);
        if (result) {
            trace.successful.computeIfAbsent(region.getCenter(), ignored -> new ConcurrentLinkedQueue<>())
                    .add(new Placement(id, origin.immutable()));
        }
    }

    private void record(WorldGenRegion region, String event) {
        events.computeIfAbsent(region.getCenter(), ignored -> new ConcurrentLinkedQueue<>()).add(event);
    }

    private record Placement(ResourceLocation feature, BlockPos origin) {
    }
}
