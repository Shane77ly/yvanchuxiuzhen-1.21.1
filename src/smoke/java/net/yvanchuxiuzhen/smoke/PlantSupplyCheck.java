package net.yvanchuxiuzhen.smoke;

import com.mojang.datafixers.util.Pair;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.FeatureSorter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.storage.LevelResource;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.world.terrain.LegacyDimensionChunkGenerator;

/** 在有界新区块中分开核对地表、原生装饰与可交给生存采收的方块。 */
final class PlantSupplyCheck extends SystemCheck {
    static final long REPLAY_SEED = -5586163949850800998L;
    private static final int FULL_PER_SUPPLY = 4;
    private static final int SEARCH_CHUNK_RADIUS = 8;
    private static final int MAX_HANDOFF_POINTS = 8;
    private static final ResourceKey<Level> RENJIAN = ResourceKey.create(Registries.DIMENSION, id("dimensionrenjian"));
    private static final long RENJIAN_TERRAIN_SEED = -3958683526324992456L;
    private static final ResourceLocation PEACH_GROVE = id("biomepeach_01");
    private static final ResourceLocation GROVE_TREE = id("tree_biomepeach_01");
    // 同种子既有保存区块的树干根部；仅用作新区块请求和噪声地表诊断的锚点。
    private static final List<BlockPos> PEACH_REPLAY_ANCHORS = List.of(
            new BlockPos(7491, 80, 3493), new BlockPos(7496, 81, 3472), new BlockPos(7515, 86, 3495));
    private static final List<Supply> SUPPLIES = List.of(
            new Supply("mulberry_02", "mulberryleaves", Heightmap.Types.WORLD_SURFACE_WG, 50000,
                    List.of("biomewind_02", "biomeautumn_01", "biomefeng_02")),
            new Supply("peach_01", "peachleaves", Heightmap.Types.OCEAN_FLOOR_WG, 500000,
                    List.of("biomepeach_01", "biomefeng_02")),
            new Supply("shicai_001_plant_08", "shicai_001_plant_08", Heightmap.Types.MOTION_BLOCKING, 0,
                    List.of("biomeautumn_01", "biomeautumn_02", "biomeautumn_03")));
    private final NaturalPeachChain peach;
    private static final List<Supply> SEASONAL_CROPS = List.of(
            new Supply("shicai_001_plant_08", "shicai_001_plant_08", Heightmap.Types.MOTION_BLOCKING, 0,
                    List.of("biomeautumn_01")),
            new Supply("shicai_007_plant_04", "shicai_007_plant_04", Heightmap.Types.MOTION_BLOCKING, 0,
                    List.of("biomewind_02")));
    private final boolean seasonal = Boolean.getBoolean("yvanchuxiuzhen.smoke.plantSeasonalCrops");
    private final List<String> report;
    private final Set<ChunkPos> requested = new HashSet<>();
    private PlantDecorationTrace trace;
    private List<FeatureSorter.StepFeatureData> featureSteps;
    private int observed;

    PlantSupplyCheck(List<String> report, String levelId) {
        super("人间自然植物供给", report, levelId);
        this.report = report;
        this.peach = Boolean.getBoolean("yvanchuxiuzhen.smoke.plantPeachHarvest")
                ? new NaturalPeachChain(this, report, RENJIAN) : null;
    }

    @Override
    protected boolean step(int step, int local) {
        if (seasonal && step > 0) {
            if (step <= SEASONAL_CROPS.size()) {
                onServer(player -> locate(player.server.getLevel(RENJIAN), SEASONAL_CROPS.get(step - 1)));
            } else {
                info("秋季与风区取样结束；主动请求的中心 FULL 区块=" + requested.size());
                finish();
            }
            return true;
        }
        if (peach != null && step > 1) return stepPeach(step - 2, local);
        if (step == 0) {
            onServer(this::prepare);
        } else if (peach != null) {
            onServer(player -> locateReplayPeach(player.server.getLevel(RENJIAN)));
        } else if (step <= SUPPLIES.size()) {
            onServer(player -> locate(player.server.getLevel(RENJIAN), SUPPLIES.get(step - 1)));
        } else {
            expectTrue("原生装饰探针至少记录一次布置", observed > 0);
            info("请求的中心 FULL 区块总数=" + requested.size());
            finish();
        }
        return true;
    }

    private boolean stepPeach(int step, int local) {
        switch (step) {
            case 0 -> {
                info("请求的中心 FULL 区块总数=" + requested.size());
                trace = null;
                if (!peach.hasSample()) {
                    fail("固定桃林新区块未同时取得原生桃树成功和其可采桃叶");
                    finish();
                    return true;
                }
                boolean[] started = {false};
                onServer(player -> started[0] = peach.harvestAndEat(player));
                if (!started[0]) {
                    fail("自然林桃未进入真实食用阶段，不继续播种或判为重进覆盖");
                    finish();
                }
            }
            case 1 -> {
                boolean[] ready = {false};
                onServer(player -> ready[0] = peach.plantAfterMeal(player, local > 200));
                if (!ready[0]) return false;
                if (peach.grown()) leaveWorld();
                else finish();
            }
            case 2 -> { return reopened(local); }
            case 3 -> {
                if (!inWorld(local)) return false;
                onServer(peach::checkReloaded);
                finish();
            }
            default -> throw new IllegalStateException("无效的自然林桃检查步骤：" + step);
        }
        return true;
    }

    private void prepare(ServerPlayer player) {
        if (seasonal && (peach != null || Boolean.getBoolean("yvanchuxiuzhen.smoke.plantRiceHarvest"))) {
            fail("秋季与风区取样须单独运行");
            finish();
            return;
        }
        if (peach != null && Boolean.getBoolean("yvanchuxiuzhen.smoke.plantRiceHarvest")) {
            fail("自然林桃与水稻的重进续段须分开启用");
            finish();
            return;
        }
        ServerLevel level = player.server.getLevel(RENJIAN);
        if (level == null || player.level() == level || Files.exists(player.server.getWorldPath(LevelResource.ROOT)
                .resolve("dimensions/yvanchuxiuzhen/dimensionrenjian/region"))) {
            fail("本场景需要从未生成过人间道区块的新档");
            finish();
            return;
        }
        LegacyDimensionChunkGenerator generator = (LegacyDimensionChunkGenerator) level.getChunkSource().getGenerator();
        if (peach != null && (level.getSeed() != REPLAY_SEED || generator.terrainSeed() != RENJIAN_TERRAIN_SEED)) {
            fail("固定桃林要求世界种子 " + REPLAY_SEED + " 和人间地形种子 " + RENJIAN_TERRAIN_SEED);
            finish();
            return;
        }
        if (peach == null) {
            featureSteps = FeatureSorter.buildFeaturesPerStep(List.copyOf(generator.getBiomeSource().possibleBiomes()),
                    holder -> holder.value().getGenerationSettings().features(), true);
        }
        var registry = level.registryAccess().registryOrThrow(Registries.PLACED_FEATURE);
        Map<PlacedFeature, ResourceLocation> placed = new HashMap<>();
        Map<ConfiguredFeature<?, ?>, ResourceLocation> configured = new HashMap<>();
        for (String path : seasonal ? SEASONAL_CROPS.stream().map(Supply::feature).toList()
                : List.of("mulberry_02", "peach_01", "tree_biomepeach_01", "shicai_001_plant_08")) {
            ResourceLocation id = id(path);
            PlacedFeature value = registry.getOptional(id).orElseThrow();
            placed.put(value, id);
            configured.put(value.feature().value(), id);
        }
        trace = new PlantDecorationTrace(level, placed, configured);
        info("存档=" + player.server.getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize()
                + " 维度=" + RENJIAN.location() + " 世界种子=" + level.getSeed() + " 地形种子=" + generator.terrainSeed());
    }

    private void locateReplayPeach(ServerLevel level) {
        LegacyDimensionChunkGenerator generator = (LegacyDimensionChunkGenerator) level.getChunkSource().getGenerator();
        RandomState state = level.getChunkSource().randomState();
        BiomeManager predicted = level.getBiomeManager().withDifferentSource(
                (x, y, z) -> generator.getBiomeSource().getNoiseBiome(x, y, z, state.sampler()));
        Block log = BuiltInRegistries.BLOCK.getOptional(id("peachlog")).orElseThrow();
        for (BlockPos anchor : PEACH_REPLAY_ANCHORS) {
            ChunkPos pos = new ChunkPos(anchor);
            int noiseY = generator.getBaseHeight(anchor.getX(), anchor.getZ(), Heightmap.Types.OCEAN_FLOOR_WG, level, state);
            ResourceLocation noiseBiome = biomeId(predicted.getBiome(anchor.atY(noiseY)));
            info("PEACH_REPLAY 目标=" + pos + " 历史树根=" + anchor.toShortString() + " 噪声海床Y=" + noiseY
                    + " 噪声海床群系=" + noiseBiome + " 海平面=" + level.getSeaLevel());
            if (level.getChunkSource().getChunkNow(pos.x, pos.z) != null) {
                info("PEACH_REPLAY 跳过已经 FULL 的候选=" + pos);
                continue;
            }
            requested.add(pos);
            LevelChunk chunk;
            trace.begin();
            try {
                chunk = level.getChunk(pos.x, pos.z);
            } finally {
                trace.end();
            }
            List<BlockPos> roots = trace.successfulOrigins(pos, GROVE_TREE).stream()
                    .filter(origin -> new ChunkPos(origin).equals(pos) && chunk.getBlockState(origin).is(log)
                            && biomeId(level.getBiome(origin)).equals(PEACH_GROVE))
                    .toList();
            trace.events(pos).forEach(event -> info("PEACH_REPLAY FULL=" + pos + " " + event));
            int leaves = peach.collect(chunk, roots);
            info("PEACH_REPLAY FULL=" + pos + " 对应原生树根=" + roots + " 累计可采叶=" + leaves);
            if (leaves == NaturalPeachChain.MAX_LEAVES) break;
        }
    }

    private void locate(ServerLevel level, Supply supply) {
        LegacyDimensionChunkGenerator generator = (LegacyDimensionChunkGenerator) level.getChunkSource().getGenerator();
        RandomState state = level.getChunkSource().randomState();
        BiomeManager predictedBiomes = level.getBiomeManager().withDifferentSource(
                (x, y, z) -> generator.getBiomeSource().getNoiseBiome(x, y, z, state.sampler()));
        FeatureSeed feature = featureSeed(level, supply);
        Set<ChunkPos> seen = new HashSet<>();
        List<Candidate> candidates = new ArrayList<>();
        for (String biome : supply.biomes()) {
            Pair<BlockPos, Holder<Biome>> target = level.findClosestBiome3d(candidate -> candidate.is(id(biome)),
                    new BlockPos(4096, 72, 4096), 4096, 32, 32);
            if (target == null) {
                info(supply.feature() + " 气候搜索未找到 " + biome);
                continue;
            }
            info(supply.feature() + " 气候搜索 " + biome + " @ " + target.getFirst().toShortString());
            ChunkPos center = new ChunkPos(target.getFirst());
            for (int dx = -SEARCH_CHUNK_RADIUS; dx <= SEARCH_CHUNK_RADIUS; dx++) {
                for (int dz = -SEARCH_CHUNK_RADIUS; dz <= SEARCH_CHUNK_RADIUS; dz++) {
                    ChunkPos chunk = new ChunkPos(center.x + dx, center.z + dz);
                    if (!seen.add(chunk)) continue;
                    Candidate candidate = predict(level, generator, state, predictedBiomes, supply, feature, chunk);
                    if (candidate != null) candidates.add(candidate);
                }
            }
        }
        int filtered = (int) candidates.stream().filter(Candidate::surfaceMatch).count();
        info(supply.feature() + " 预算预筛：区块=" + seen.size() + " 随机门槛通过=" + candidates.size()
                + " 预测布置地表群系匹配=" + filtered + " 原生阶段=" + feature.step() + " 序号=" + feature.index());
        candidates.sort(Comparator.comparingInt(Candidate::score).reversed());
        int chunks = 0;
        int handoff = 0;
        for (Candidate candidate : candidates) {
            if (chunks == (seasonal ? 2 : FULL_PER_SUPPLY) || seasonal && handoff > 0) break;
            if (seasonal && !candidate.surfaceMatch()) continue;
            ChunkPos pos = candidate.chunk();
            if (requested.contains(pos) || level.getChunkSource().getChunkNow(pos.x, pos.z) != null) continue;
            requested.add(pos);
            chunks++;
            LevelChunk chunk;
            trace.begin();
            try {
                chunk = level.getChunk(pos.x, pos.z);
            } finally {
                trace.end();
            }
            List<String> events = trace.events(pos);
            observed += events.size();
            BlockPos estimated = candidate.surface();
            int y = level.getHeight(finalHeightmap(supply.heightmap()), estimated.getX(), estimated.getZ());
            BlockPos surface = new BlockPos(estimated.getX(), y, estimated.getZ());
            info(supply.feature() + " FULL=" + pos + " 预测噪声地表=" + estimated.toShortString() + "/" + candidate.biome()
                    + " 实际装饰后地表=" + surface.toShortString() + "/" + biomeId(level.getBiome(surface))
                    + " 基底=" + BuiltInRegistries.BLOCK.getKey(level.getBlockState(surface.below()).getBlock())
                    + (supply.rice() ? " 预测补丁落点相对噪声地表Y差=" + candidate.patchY() : ""));
            if (events.isEmpty()) info("FULL=" + pos + " 中心未观测到目标装饰调用");
            events.forEach(event -> info("FULL=" + pos + " " + event));
            int points = handoff(level, chunk, supply, MAX_HANDOFF_POINTS - handoff);
            handoff += points;
            info(supply.feature() + " FULL=" + pos + " 可用采收坐标=" + points);
        }
        if (seasonal) expectTrue(supply.feature() + " 在预算内取得当区原生供给坐标", handoff > 0);
        if (handoff == 0) info("未覆盖：" + supply.feature() + " 没有可交接采收坐标，不能用受控布置替代");
    }

    private FeatureSeed featureSeed(ServerLevel level, Supply supply) {
        var registry = level.registryAccess().registryOrThrow(Registries.PLACED_FEATURE);
        PlacedFeature feature = registry.getOptional(id(supply.feature())).orElseThrow();
        for (int step = 0; step < featureSteps.size(); step++) {
            var data = featureSteps.get(step);
            if (data.features().contains(feature)) return new FeatureSeed(step, data.indexMapping().applyAsInt(feature));
        }
        throw new IllegalStateException("在人间道地物顺序中缺少 " + supply.feature());
    }

    private static Candidate predict(ServerLevel level, LegacyDimensionChunkGenerator generator, RandomState state,
            BiomeManager biomes, Supply supply, FeatureSeed feature, ChunkPos chunk) {
        // 使用与原生装饰相同的独立地物随机流；只用于筛点，不读写区块或推进服务器随机流。
        WorldgenRandom random = new WorldgenRandom(new XoroshiroRandomSource(0));
        long decoration = random.setDecorationSeed(level.getSeed(), chunk.getMinBlockX(), chunk.getMinBlockZ());
        random.setFeatureSeed(decoration, feature.index(), feature.step());
        if (supply.rice()) {
            if (random.nextFloat() >= 1.0F / 32) return null;
        } else {
            if (random.nextInt(1000000) + 1 > supply.treeChance()) return null;
            random.nextInt(1);
        }
        int x = chunk.getMinBlockX() + random.nextInt(16);
        int z = chunk.getMinBlockZ() + random.nextInt(16);
        int y = generator.getBaseHeight(x, z, supply.heightmap(), level, state);
        BlockPos surface = new BlockPos(x, y, z);
        ResourceLocation biome = biomeId(biomes.getBiome(surface));
        boolean match = supply.biomes().stream().map(PlantSupplyCheck::id).anyMatch(biome::equals);
        int patchY = 0;
        if (supply.rice()) {
            int ox = random.nextInt(8) - random.nextInt(8);
            patchY = random.nextInt(4) - random.nextInt(4);
            int oz = random.nextInt(8) - random.nextInt(8);
            int patchSurface = generator.getBaseHeight(x + ox, z + oz, supply.heightmap(), level, state);
            patchY = y + patchY - patchSurface;
        }
        int score = (match ? 16 : 0) + (y > level.getSeaLevel() ? 8 : 0) - Math.min(7, Math.abs(patchY));
        return new Candidate(chunk, surface, biome, match, patchY, score);
    }

    private int handoff(ServerLevel level, LevelChunk chunk, Supply supply, int budget) {
        Block block = BuiltInRegistries.BLOCK.get(id(supply.block()));
        ChunkPos pos = chunk.getPos();
        int points = 0;
        BlockPos first = null;
        for (BlockPos point : BlockPos.betweenClosed(pos.getMinBlockX(), level.getMinBuildHeight(), pos.getMinBlockZ(),
                pos.getMaxBlockX(), level.getMaxBuildHeight() - 1, pos.getMaxBlockZ())) {
            if (points == budget) break;
            BlockState state = chunk.getBlockState(point);
            if (!state.is(block) || (first != null && !supply.rice() && point.distManhattan(first) < 8)) continue;
            if (seasonal && (!supply.biomes().contains(biomeId(level.getBiome(point)).getPath())
                    || !state.canSurvive(level, point) || trace.successfulOrigins(pos, id(supply.feature())).stream()
                    .noneMatch(origin -> Math.abs(point.getX() - origin.getX()) <= 7
                            && Math.abs(point.getY() - origin.getY()) <= 3
                            && Math.abs(point.getZ() - origin.getZ()) <= 7))) continue;
            if (supply.doubleCereal()) {
                if (state.getValue(DoublePlantBlock.HALF) != DoubleBlockHalf.LOWER) continue;
                BlockState upper = chunk.getBlockState(point.above());
                boolean viable = upper.is(block) && upper.getValue(DoublePlantBlock.HALF) == DoubleBlockHalf.UPPER
                        && state.canSurvive(level, point) && upper.canSurvive(level, point.above());
                expectTrue("自然水稻上下半格完整可存活 " + point.toShortString(), viable);
                if (!viable) continue;
            }
            if (first == null) first = point.immutable();
            info("HARVEST block=" + id(supply.block()) + " pos=" + point.toShortString()
                    + " biome=" + biomeId(level.getBiome(point)) + (supply.doubleCereal() ? " upper=" + point.above().toShortString() : ""));
            points++;
        }
        return points;
    }

    private void info(String message) {
        report.add("INFO 人间自然植物供给 " + message);
    }

    private static Heightmap.Types finalHeightmap(Heightmap.Types type) {
        return switch (type) {
            case WORLD_SURFACE_WG -> Heightmap.Types.WORLD_SURFACE;
            case OCEAN_FLOOR_WG -> Heightmap.Types.OCEAN_FLOOR;
            default -> type;
        };
    }

    private static ResourceLocation biomeId(Holder<Biome> biome) {
        return biome.unwrapKey().orElseThrow().location();
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }

    private record Supply(String feature, String block, Heightmap.Types heightmap, int treeChance, List<String> biomes) {
        boolean rice() {
            return treeChance == 0;
        }

        boolean doubleCereal() {
            return block.equals("shicai_001_plant_08");
        }
    }

    private record FeatureSeed(int step, int index) {
    }

    private record Candidate(ChunkPos chunk, BlockPos surface, ResourceLocation biome, boolean surfaceMatch, int patchY, int score) {
    }
}
