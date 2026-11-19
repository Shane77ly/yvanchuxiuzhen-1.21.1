package net.yvanchuxiuzhen.smoke;

import com.mojang.logging.LogUtils;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.FeatureSorter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.LevelResource;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.world.WorldGeneration;
import net.yvanchuxiuzhen.system.world.diagnostic.RenjianRandomDiagnostics;
import net.yvanchuxiuzhen.system.world.portal.DimensionPortalSpec;
import org.slf4j.Logger;

/** 只请求有界且预测过抽签门槛的新 FULL 区块；以真正放置和关档后的方块作为阳性证据。 */
final class RenjianRandomCheck extends SystemCheck {
    static final long SEED = -5586163949850800998L;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int FULL_BUDGET = 4;
    private static final int[] IDS = {1, 3, 4};
    private final List<String> report;
    private final Remainder remainder;
    private final List<Probe> probes = new ArrayList<>();
    private List<FeatureSorter.StepFeatureData> featureSteps;
    private List<Candidate> candidates;
    private int target;
    private int samples;

    RenjianRandomCheck(List<String> report, String levelId) {
        this(report, levelId, null);
    }

    RenjianRandomCheck(List<String> report, String levelId, Remainder remainder) {
        super("人间随机结构自然新区块", report, levelId);
        this.report = report;
        this.remainder = remainder;
    }

    @Override
    protected boolean step(int step, int local) {
        switch (step) {
            case 0 -> onServer(this::prepare);
            case 1 -> {
                if (target < (remainder == null ? IDS.length : 1)) {
                    onServer(this::sample);
                    return false;
                }
                if (remainder != null && probes.isEmpty()) {
                    finish();
                    return true;
                }
                leaveWorld();
            }
            case 2 -> { return reopened(local); }
            case 3 -> {
                if (!inWorld(local)) return false;
                onServer(player -> {
                    expect("重进后玩家仍在主世界", Level.OVERWORLD, player.level().dimension());
                    ServerLevel level = renjian(player);
                    for (Probe probe : probes) {
                        if (remainder != null) {
                            ChunkPos pos = new ChunkPos(probe.pos());
                            String persisted = level.getChunkSource().chunkMap.read(pos).join()
                                    .map(tag -> tag.getString("Status")).orElse("无存档记录");
                            expect("重进后探针区块的磁盘状态 " + pos, ChunkStatus.FULL.getName(), persisted);
                            if (!ChunkStatus.FULL.getName().equals(persisted)) continue;
                        }
                        expectTrue("保存重进仍存在自然模板 " + probe.id() + " @ " + probe.pos().toShortString(),
                                level.getBlockState(probe.pos()).is(probe.block()));
                    }
                });
                finish();
            }
            default -> throw new IllegalStateException("未知随机结构步骤 " + step);
        }
        return true;
    }

    private void prepare(ServerPlayer player) {
        ServerLevel level = renjian(player);
        if (level == null || player.serverLevel().dimension() != Level.OVERWORLD
                || Files.exists(player.server.getWorldPath(LevelResource.ROOT).resolve("dimensions")
                        .resolve(ModId.VALUE).resolve("dimensionrenjian/region"))) {
            fail("需要人间道未生成区块的新档，玩家始终留在主世界");
            finish();
            return;
        }
        if (!WorldGeneration.randomStructuresAllowed(player.server) || level.getSeed() != SEED) {
            fail("要求随机结构默认解锁、固定种子 " + SEED + "，实际种子=" + level.getSeed());
            finish();
            return;
        }
        ChunkGenerator generator = level.getChunkSource().getGenerator();
        featureSteps = FeatureSorter.buildFeaturesPerStep(List.copyOf(generator.getBiomeSource().possibleBiomes()),
                holder -> holder.value().getGenerationSettings().features(), true);
        info("seed=" + level.getSeed() + (remainder == null ? "，每种最多 " + FULL_BUDGET + " 个互相隔开的 FULL 区块"
                : "，只验 " + remainder.suffix + "，最多请求一个自然装饰中心和一个探针邻区"));
    }

    private void sample(ServerPlayer player) {
        ServerLevel level = renjian(player);
        String suffix = remainder == null ? "0" + IDS[target] : remainder.suffix;
        ResourceLocation templateId = id("suijijiegou" + suffix);
        if (candidates == null) {
            ResourceLocation placedId = id("suijijiegou_" + suffix);
            candidates = remainder == null ? candidates(level, placedId) : remainderCandidates(level, placedId);
        }
        if (samples >= candidates.size()) {
            fail(templateId + " 没有自然放置并读回的阳性样本；已请求=" + samples);
            advance();
            return;
        }

        Candidate candidate = candidates.get(samples++);
        ChunkPos pos = candidate.chunk();
        if (remainder != null) {
            String stored = level.getChunkSource().chunkMap.read(pos).join().map(tag -> tag.getString("Status")).orElse(null);
            if (stored != null) {
                fail(templateId + " 候选不是空白区块 " + pos + "，磁盘状态=" + stored);
                advance();
                return;
            }
        }
        if (level.getChunkSource().getChunkNow(pos.x, pos.z) != null) {
            fail(templateId + " 候选已是 FULL，不能作为新区块=" + pos);
            return;
        }
        String estimateLabel = remainder == null ? "predictedSurface" : "predictedBase";
        LOGGER.info("TRACE 人间随机结构 stage=full_begin seed={} template={} chunk={} {}={}",
                level.getSeed(), templateId, pos, estimateLabel, candidate.surface());
        RenjianRandomDiagnostics.begin(level, pos, templateId);
        LevelChunk chunk;
        RenjianRandomDiagnostics.Result observed;
        try {
            chunk = level.getChunk(pos.x, pos.z);
        } finally {
            observed = RenjianRandomDiagnostics.end();
        }
        info(templateId + " FULL=" + pos + " " + (remainder == null ? "surface" : "baseEstimate") + "="
                + candidate.surface().toShortString() + " observed=" + observed);
        if (observed == null || !observed.placed() || observed.size().getX() == 0) return;

        // 探针可能在相邻区块；只读取当次模板换算出的探针，不再放模板。
        StructurePlaceSettings transform = new StructurePlaceSettings().setMirror(observed.mirror()).setRotation(observed.rotation());
        StructureTemplate template = level.getStructureManager().get(templateId).orElseThrow();
        if (remainder != null) {
            checkRemainderWitness(level, chunk, templateId, template, observed.origin(), transform);
            return;
        }
        Block witness = target == 1 ? Blocks.DISPENSER : BuiltInRegistries.BLOCK.get(id("treasure_chest_03"));
        for (StructureTemplate.StructureBlockInfo entry : template.filterBlocks(observed.origin(), transform, witness)) {
            ChunkPos witnessChunk = new ChunkPos(entry.pos());
            if (witnessChunk.getChessboardDistance(pos) > 1) continue;
            LevelChunk sample = witnessChunk.equals(pos) ? chunk : level.getChunk(witnessChunk.x, witnessChunk.z);
            if (!sample.getBlockState(entry.pos()).is(witness)) continue;
            probes.add(new Probe(templateId, entry.pos(), witness));
            info(templateId + " 原生放置标志在 FULL " + witnessChunk + " 实际写入 @ " + entry.pos().toShortString());
            advance();
            return;
        }
        info(templateId + " 已调用自然放置，但原生装饰写入范围内没有可保存复查的预期标志");
    }

    private void checkRemainderWitness(ServerLevel level, LevelChunk center, ResourceLocation templateId,
            StructureTemplate template, BlockPos origin, StructurePlaceSettings transform) {
        ChunkPos centerPos = center.getPos();
        Block witness = BuiltInRegistries.BLOCK.get(id(remainder.witness));
        List<StructureTemplate.StructureBlockInfo> markers = template.filterBlocks(origin, transform, witness);
        List<BlockPos> inRange = markers.stream().map(StructureTemplate.StructureBlockInfo::pos)
                .filter(pos -> !level.isOutsideBuildHeight(pos)
                        && new ChunkPos(pos).getChessboardDistance(centerPos) <= (remainder == Remainder.RUIN ? 0 : 1))
                .toList();
        // 02/05 的已知标志各只有一个；06 只读取中心，避免为寻找箱子另请求区块。
        for (BlockPos pos : inRange) {
            ChunkPos witnessChunk = new ChunkPos(pos);
            LevelChunk chunk = witnessChunk.equals(centerPos) ? center : level.getChunk(witnessChunk.x, witnessChunk.z);
            Block actual = chunk.getBlockState(pos).getBlock();
            info(templateId + " 实存方块 FULL=" + witnessChunk + " @ " + pos.toShortString()
                    + " expected=" + BuiltInRegistries.BLOCK.getKey(witness) + " actual=" + BuiltInRegistries.BLOCK.getKey(actual));
            if (actual == witness) {
                probes.add(new Probe(templateId, pos, witness));
                advance();
                return;
            }
        }
        if (inRange.isEmpty()) info(templateId + " 原生放置后的变换无界内探针，未请求旁区：" + markers);
    }

    private List<Candidate> remainderCandidates(ServerLevel level, ResourceLocation placedId) {
        PlacedFeature placed = level.registryAccess().registryOrThrow(Registries.PLACED_FEATURE).getOptional(placedId).orElseThrow();
        int step = remainder == Remainder.SURFACE ? 4 : 3;
        FeatureSorter.StepFeatureData data = featureSteps.get(step);
        int index = data.features().indexOf(placed);
        if (!placed.placement().isEmpty() || index != remainder.index) {
            info(placedId + " 放置修饰器或排序不符合预案：step=" + step + " index=" + index);
            return List.of();
        }
        WorldgenRandom random = new WorldgenRandom(new XoroshiroRandomSource(0));
        if (remainder != Remainder.RUIN) {
            BlockPos base = remainder.archiveBase;
            ChunkPos chunk = new ChunkPos(base);
            BlockPos xz = drawHorizontal(level, random, chunk, step, index);
            if (xz == null || xz.getX() != base.getX() || xz.getZ() != base.getZ()) {
                info(placedId + " 归档坐标与当前抽签不一致：" + chunk + " actual=" + xz);
                return List.of();
            }
            info(placedId + " 归档站点 " + chunk + " base=" + base.toShortString() + " step=" + step + " index=" + index);
            return List.of(new Candidate(chunk, base));
        }

        ChunkGenerator generator = level.getChunkSource().getGenerator();
        RandomState state = level.getChunkSource().randomState();
        List<Candidate> matches = new ArrayList<>();
        int drawn = 0;
        // 只干选既有03/05站点附近的固定窗口；噪声列不执行表层、洞穴雕刻或装饰。
        for (int cx = 288; cx < 416; cx++) {
            for (int cz = 128; cz < 256; cz++) {
                ChunkPos chunk = new ChunkPos(cx, cz);
                BlockPos xz = drawHorizontal(level, random, chunk, step, index);
                if (xz == null) continue;
                drawn++;
                int x = xz.getX();
                int z = xz.getZ();
                int height = generator.getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, level, state);
                int min = level.getMinBuildHeight();
                int y = Mth.nextInt(random, min + 8, Math.max(height, min + 9));
                Mirror mirror = Mirror.values()[random.nextInt(2)];
                Rotation rotation = Rotation.values()[random.nextInt(3)];
                Holder<Biome> biome = generator.getBiomeSource().getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(y),
                        QuartPos.fromBlock(z), state.sampler());
                if (!biome.value().getGenerationSettings().hasFeature(placed) || y < min + 16 || y > height - 8) continue;
                NoiseColumn column = generator.getBaseColumn(x, z, level, state);
                if (!column.getBlock(y - 2).is(Blocks.STONE) || !column.getBlock(y - 1).is(Blocks.STONE)
                        || !column.getBlock(y).is(Blocks.STONE) || !column.getBlock(y + 1).is(Blocks.STONE)
                        || !column.getBlock(y + 2).is(Blocks.STONE)) continue;
                BlockPos base = new BlockPos(x, y, z);
                BlockPos origin = base.below(10);
                // 两个11号宝箱在原模板的相对位置；预检不提前解码模板。
                if (List.of(new BlockPos(5, 0, 9), new BlockPos(2, 1, 9)).stream().noneMatch(pos ->
                        new ChunkPos(StructureTemplate.transform(pos, mirror, rotation, BlockPos.ZERO).offset(origin)).equals(chunk))) continue;
                matches.add(new Candidate(chunk, base));
                info(placedId + " 噪声预选 " + chunk + " height=" + height + " base=" + base.toShortString()
                        + " mirror=" + mirror + " rotation=" + rotation + " biome=" + biome.getRegisteredName());
            }
        }
        info(placedId + " 窗口 [288,416)×[128,256) step=" + step + " index=" + index + " 中签=" + drawn
                + " 噪声预选=" + matches.size() + "，最多请求首选一个中心");
        return matches.stream().limit(1).toList();
    }

    private static BlockPos drawHorizontal(ServerLevel level, WorldgenRandom random, ChunkPos chunk, int step, int index) {
        long decoration = random.setDecorationSeed(level.getSeed(), chunk.getMinBlockX(), chunk.getMinBlockZ());
        random.setFeatureSeed(decoration, index, step);
        if (random.nextInt(1000000) >= 1000) return null;
        random.nextInt(1);
        return new BlockPos(chunk.getMinBlockX() + random.nextInt(16), 0, chunk.getMinBlockZ() + random.nextInt(16));
    }

    private List<Candidate> candidates(ServerLevel level, ResourceLocation id) {
        PlacedFeature placed = level.registryAccess().registryOrThrow(Registries.PLACED_FEATURE).getOptional(id).orElseThrow();
        if (!placed.placement().isEmpty()) throw new IllegalStateException(id + " 的布置修饰器已变化");
        int step = 0;
        while (step < featureSteps.size() && !featureSteps.get(step).features().contains(placed)) step++;
        if (step == featureSteps.size()) throw new IllegalStateException(id + " 不在人间道装饰排序中");
        int index = featureSteps.get(step).indexMapping().applyAsInt(placed);
        ChunkGenerator generator = level.getChunkSource().getGenerator();
        RandomState state = level.getChunkSource().randomState();
        WorldgenRandom random = new WorldgenRandom(new XoroshiroRandomSource(0));
        List<Candidate> matches = new ArrayList<>();
        int start = 128 + target * 160;
        for (int cx = start; cx < start + 128; cx++) {
            for (int cz = 128; cz < 256; cz++) {
                ChunkPos chunk = new ChunkPos(cx, cz);
                long decoration = random.setDecorationSeed(level.getSeed(), chunk.getMinBlockX(), chunk.getMinBlockZ());
                random.setFeatureSeed(decoration, index, step);
                if (random.nextInt(1000000) >= 1000) continue;
                random.nextInt(1);
                int x = chunk.getMinBlockX() + random.nextInt(16);
                int z = chunk.getMinBlockZ() + random.nextInt(16);
                int y = generator.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, level, state) - 1;
                Holder<Biome> biome = generator.getBiomeSource().getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(y),
                        QuartPos.fromBlock(z), state.sampler());
                if (biome.value().getGenerationSettings().hasFeature(placed)) {
                    matches.add(new Candidate(chunk, new BlockPos(x, y, z)));
                }
            }
        }
        matches.sort(Comparator.comparingInt((Candidate match) -> Math.min(match.surface().getY() - generator.getSeaLevel(), 16)).reversed());
        List<Candidate> selected = new ArrayList<>();
        for (Candidate match : matches) {
            if (selected.stream().anyMatch(other -> other.chunk().getChessboardDistance(match.chunk()) < 16)) continue;
            selected.add(match);
            if (selected.size() == FULL_BUDGET) break;
        }
        info(id + " step=" + step + " index=" + index + " 干选=" + matches.size() + " 请求计划=" + selected);
        return selected;
    }

    private void advance() {
        target++;
        samples = 0;
        candidates = null;
    }

    private void info(String message) {
        report.add("INFO 人间随机结构自然新区块 " + message);
    }

    private static ServerLevel renjian(ServerPlayer player) {
        return player.server.getLevel(DimensionPortalSpec.RENJIAN.dimension());
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }

    enum Remainder {
        SURFACE("02", 31, "treasure_chest_03", new BlockPos(7385, 55, 3388)),
        CAVERN("05", 0, "treasure_chest_05", new BlockPos(5247, 52, 2301)),
        RUIN("06", 1, "treasure_chest_11", null);

        private final String suffix;
        private final int index;
        private final String witness;
        private final BlockPos archiveBase;

        Remainder(String suffix, int index, String witness, BlockPos archiveBase) {
            this.suffix = suffix;
            this.index = index;
            this.witness = witness;
            this.archiveBase = archiveBase;
        }

        static boolean isSelector(String value) {
            return value.equals("renjianrandom02") || value.equals("renjianrandom05") || value.equals("renjianrandom06");
        }
    }

    private record Candidate(ChunkPos chunk, BlockPos surface) {
    }

    private record Probe(ResourceLocation id, BlockPos pos, Block block) {
    }
}
