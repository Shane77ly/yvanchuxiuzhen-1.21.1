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
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.FeatureSorter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunk;
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
    private final List<Probe> probes = new ArrayList<>();
    private List<FeatureSorter.StepFeatureData> featureSteps;
    private List<Candidate> candidates;
    private int target;
    private int samples;

    RenjianRandomCheck(List<String> report, String levelId) {
        super("人间随机结构自然新区块", report, levelId);
        this.report = report;
    }

    @Override
    protected boolean step(int step, int local) {
        switch (step) {
            case 0 -> onServer(this::prepare);
            case 1 -> {
                if (target < IDS.length) {
                    onServer(this::sample);
                    return false;
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
        info("seed=" + level.getSeed() + "，每种最多 " + FULL_BUDGET + " 个互相隔开的 FULL 区块");
    }

    private void sample(ServerPlayer player) {
        ServerLevel level = renjian(player);
        String suffix = "0" + IDS[target];
        ResourceLocation templateId = id("suijijiegou" + suffix);
        if (candidates == null) candidates = candidates(level, id("suijijiegou_" + suffix));
        if (samples >= candidates.size()) {
            fail(templateId + " 没有自然放置并读回的阳性样本；已请求=" + samples);
            advance();
            return;
        }

        Candidate candidate = candidates.get(samples++);
        ChunkPos pos = candidate.chunk();
        if (level.getChunkSource().getChunkNow(pos.x, pos.z) != null) {
            fail(templateId + " 候选已是 FULL，不能作为新区块=" + pos);
            return;
        }
        LOGGER.info("TRACE 人间随机结构 stage=full_begin seed={} template={} chunk={} predictedSurface={}",
                level.getSeed(), templateId, pos, candidate.surface());
        RenjianRandomDiagnostics.begin(level, pos, templateId);
        LevelChunk chunk;
        RenjianRandomDiagnostics.Result observed;
        try {
            chunk = level.getChunk(pos.x, pos.z);
        } finally {
            observed = RenjianRandomDiagnostics.end();
        }
        info(templateId + " FULL=" + pos + " surface=" + candidate.surface().toShortString() + " observed=" + observed);
        if (observed == null || !observed.placed() || observed.size().getX() == 0) return;

        // 探针可能在相邻区块；只读取当次模板换算出的探针，不再放模板。
        Block witness = target == 1 ? BuiltInRegistries.BLOCK.get(ResourceLocation.withDefaultNamespace("dispenser"))
                : BuiltInRegistries.BLOCK.get(id("treasure_chest_03"));
        StructurePlaceSettings transform = new StructurePlaceSettings().setMirror(observed.mirror()).setRotation(observed.rotation());
        StructureTemplate template = level.getStructureManager().get(templateId).orElseThrow();
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

    private record Candidate(ChunkPos chunk, BlockPos surface) {
    }

    private record Probe(ResourceLocation id, BlockPos pos, Block block) {
    }
}
