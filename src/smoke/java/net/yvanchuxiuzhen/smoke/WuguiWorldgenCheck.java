package net.yvanchuxiuzhen.smoke;

import com.google.common.hash.Hashing;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.world.terrain.LegacyDimensionChunkGenerator;
import net.yvanchuxiuzhen.system.world.terrain.LegacyNoiseSlide;

/** 无归十群系数据、有限自然区块与地物生成的植物/模板保存消费者。 */
final class WuguiWorldgenCheck extends SystemCheck {
    private static final ResourceKey<Level> WUGUI = ResourceKey.create(Registries.DIMENSION, id("dimensionwugui"));
    private static final BlockPos PATCH = new BlockPos(8, 200, 8);
    private static final int FLAGS = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
    private static final List<String> BIOMES = List.of("biomebaichuan_01", "biomebaichuan_02", "biomebaichuan_03",
            "biomelianyvan_01", "biomeshijve_01", "biomeshijve_02", "biomeshijve_03",
            "biomeyoumeng_01", "biomeyoumeng_02", "biomeyoumeng_03");
    private static final List<String> NAMES = List.of("黑水流域", "幽河", "黑河平原", "魂锁平原", "腐水荡", "万坟岗",
            "十死绝地", "朱金梦魇", "幽林雾地", "红土荒原");
    private static final List<String> REJECTED = List.of("cailiao_16101", "cailiao_16201", "cailiao_16301",
            "cailiao_16401", "cailiao_16501", "cailiao_16601", "cailiao_16701", "cailiao_16801", "cailiao_16901",
            "cailiao_20101", "cailiao_20301", "cailiao_20501", "cailiao_20701", "cailiao_20801", "cailiao_20901",
            "cailiao_21101", "cailiao_21301", "cailiao_21501", "cailiao_21601", "cailiao_21701", "cailiao_21901",
            "nichanggrass_06", "suijijiegou_01", "suijijiegou_02", "suijijiegou_03", "suijijiegou_04",
            "suijijiegou_05", "suijijiegou_06");
    private final List<String> report;
    private final Screenshots screenshots;
    private final Map<BlockPos, SavedBlock> saved = new LinkedHashMap<>();
    private final Map<BlockPos, BlockState> retained = new LinkedHashMap<>();
    private ResourceKey<Level> originalDimension;
    private Vec3 originalPosition;
    private float originalYaw;
    private float originalPitch;
    private GameType originalMode;
    private CompoundTag originalAbilities;
    private boolean originalNoGravity;
    private boolean prepared;

    WuguiWorldgenCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("无归生成", report, levelId);
        this.report = report;
        this.screenshots = screenshots;
    }

    @Override
    protected boolean step(int step, int local) {
        try {
            switch (step) {
                case 0 -> onServer(this::checkWorld);
                case 1 -> {
                    if (!ready(local)) return false;
                    for (int index = 0; index < BIOMES.size(); index++) {
                        expect("群系中文已合并 " + BIOMES.get(index), NAMES.get(index),
                                I18n.get("biome." + ModId.VALUE + "." + BIOMES.get(index)));
                    }
                    screenshots.take("wugui_natural_sample.png");
                }
                case 2 -> onServer(this::checkConsumers);
                case 3 -> {
                    if (!ready(local)) return false;
                    screenshots.take("wugui_generated_herbs.png");
                }
                case 4 -> leaveWorld();
                case 5 -> { return reopened(local); }
                case 6 -> {
                    if (!inWorld(local)) return false;
                    onServer(player -> {
                        ServerLevel level = player.server.getLevel(WUGUI);
                        expect("重进仍在无归道", WUGUI, player.level().dimension());
                        for (Map.Entry<BlockPos, BlockState> entry : retained.entrySet()) {
                            expect("地物产出真实保存 " + entry.getKey(), entry.getValue(), level.getBlockState(entry.getKey()));
                        }
                        cleanup(player);
                    });
                    finish();
                }
                default -> throw new IllegalStateException("未知无归检查步骤 " + step);
            }
        } catch (RuntimeException exception) {
            fail("步骤" + step + "异常：" + exception);
            if (minecraft.getSingleplayerServer() != null) onServer(this::cleanup);
            finish();
        }
        return true;
    }

    private void checkSlide(LegacyDimensionChunkGenerator generator) {
        var nodes = new ArrayList<LegacyNoiseSlide>();
        generator.generatorSettings().value().noiseRouter().finalDensity().mapAll(f -> {
            if (f instanceof LegacyNoiseSlide slide) nodes.add(slide);
            return f;
        });
        expect("最终密度仅包含一个紧凑滑移", 1, nodes.size());
        if (nodes.size() != 1) return;
        var node = nodes.getFirst();
        double[] inputs = {-91.75, -0.0, 0.0, Math.PI, Math.nextDown(0.9375), 31.125};
        var samples = new ArrayList<LegacyNoiseSlide>();
        for (double value : inputs) samples.add(new LegacyNoiseSlide(DensityFunctions.constant(value), node.cellHeight(),
                node.minCell(), node.cells(), node.top(), node.bottom()));
        var bits = ByteBuffer.allocate(385 * inputs.length * Long.BYTES);
        for (int y = -64; y <= 320; y++) {
            var pos = new DensityFunction.SinglePointContext(0, y, 0);
            for (var sample : samples) bits.putLong(Double.doubleToRawLongBits(sample.compute(pos)));
        }
        // 对照摘要来自相同坐标与输入在原 range_choice JSON 中的逐节点运算，大端 IEEE 754 位序。
        expect("原高度分支 2310 个双精度结果逐位一致", "b4388983ee4bd09512c2a94409283b1fe3afa35d7976a0b806ba6878e1540ff6",
                Hashing.sha256().hashBytes(bits.array()).toString());
    }

    private boolean ready(int local) {
        if (local > 600) throw new IllegalStateException("无归客户端画面同步超时");
        return local >= 30 && minecraft.screen == null && minecraft.level != null && minecraft.level.dimension().equals(WUGUI);
    }

    private void checkWorld(ServerPlayer player) {
        ServerLevel level = player.server.getLevel(WUGUI);
        if (level == null) throw new IllegalStateException("正式无归维度未加载");
        checkSlide((LegacyDimensionChunkGenerator) level.getChunkSource().getGenerator());
        Set<ResourceLocation> actual = new HashSet<>();
        level.getChunkSource().getGenerator().getBiomeSource().possibleBiomes()
                .forEach(holder -> actual.add(holder.unwrapKey().orElseThrow().location()));
        Set<ResourceLocation> expected = new HashSet<>();
        BIOMES.forEach(name -> expected.add(id(name)));
        expect("实际biome_source六专属与四共享完整", expected, actual);
        LevelChunk chunk = level.getChunk(0, 0);
        int soulstone = 0;
        int naturalSoulgrass = 0;
        int naturalCorpsegrass = 0;
        for (BlockPos pos : BlockPos.betweenClosed(new BlockPos(0, 0, 0), new BlockPos(15, 127, 15))) {
            BlockState state = chunk.getBlockState(pos);
            if (state.is(block("soulstone"))) soulstone++;
            if (state.is(block("soulgrass"))) naturalSoulgrass++;
            if (state.is(block("corpsegrass"))) naturalCorpsegrass++;
        }
        expectTrue("有限自然区块实际出现魂石", soulstone > 0);
        report.add("INFO 无归生成 原点一块FULL样本：阴魂草=" + naturalSoulgrass + " 尸草=" + naturalCorpsegrass);
        report.add("INFO 无归生成 边界：一个FULL区块与受控生成消费者；不据此证明十群系自然分布、出生率或矿物/植物全域供给");
        originalDimension = player.level().dimension();
        originalPosition = player.position();
        originalYaw = player.getYRot();
        originalPitch = player.getXRot();
        originalMode = player.gameMode.getGameModeForPlayer();
        originalNoGravity = player.isNoGravity();
        originalAbilities = new CompoundTag();
        player.getAbilities().addSaveData(originalAbilities);
        prepared = true;
        player.setGameMode(GameType.CREATIVE);
        player.setNoGravity(true);
        player.getAbilities().flying = true;
        player.onUpdateAbilities();
        int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE, 8, 8);
        player.teleportTo(level, 8.5, Math.max(30, surface + 10), 8.5, 45, 35);
    }

    private void checkConsumers(ServerPlayer player) {
        ServerLevel level = player.server.getLevel(WUGUI);
        ChunkGenerator generator = level.getChunkSource().getGenerator();
        Registry<ConfiguredFeature<?, ?>> features = level.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE);
        for (String name : REJECTED) {
            expectTrue("来源维度门拒绝 " + name, !feature(features, name).place(level, generator, RandomSource.create(0L), PATCH));
        }
        ConfiguredFeature<?, ?> delta = feature(features, "jinsebankuai");
        expect("金色板块同名类型实际注册", id("jinsebankuai"), BuiltInRegistries.FEATURE.getKey(delta.feature()));
        ServerLevel overworld = player.server.overworld();
        expectTrue("金色板块拒绝主世界", !delta.place(overworld, overworld.getChunkSource().getGenerator(), RandomSource.create(0L), PATCH));
        checkTemplate(level, generator, features);
        checkColumn(level, generator, features, "ironropeshengcheng", "ironrope", new BlockPos(0, 170, 0), 60);
        checkColumn(level, generator, features, "rottingbambooshengcheng", "rottingbamboo", new BlockPos(0, 170, 1), 4);
        checkHerbs(player, level, generator, features);
    }

    private void checkTemplate(ServerLevel level, ChunkGenerator generator, Registry<ConfiguredFeature<?, ?>> features) {
        BlockPos base = new BlockPos(6, 221, 7);
        BlockPos ceiling = new BlockPos(6, 250, 7);
        save(level, ceiling);
        for (int y = 0; y <= 6; y++) save(level, base.above(y));
        LevelChunk chunk = level.getChunk(0, 0);
        Heightmap.primeHeightmaps(chunk, Set.of(Heightmap.Types.OCEAN_FLOOR_WG));
        long[] heightmap = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG).getRawData().clone();
        try {
            level.setBlock(ceiling, Blocks.STONE.defaultBlockState(), FLAGS);
            for (int y = 1; y <= 6; y++) level.setBlock(base.above(y), Blocks.AIR.defaultBlockState(), FLAGS);
            Heightmap.primeHeightmaps(chunk, Set.of(Heightmap.Types.OCEAN_FLOOR_WG));
            expect("受控来源OCEAN_FLOOR_WG上界", 251, level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, 6, 7));
            if (level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, 6, 7) != 251) {
                throw new IllegalStateException("魔眼模板高度图前置条件失败");
            }
            ConfiguredFeature<?, ?> template = feature(features, "moyanjiegou_01");
            level.setBlock(base, Blocks.STONE.defaultBlockState(), FLAGS);
            expectTrue("魔眼结构拒绝非腐土地基", !template.place(level, generator, RandomSource.create(70L), BlockPos.ZERO));
            level.setBlock(base, block("rottingsoil").defaultBlockState(), FLAGS);
            expectTrue("魔眼来源seed70在腐土实际落地", template.place(level, generator, RandomSource.create(70L), BlockPos.ZERO));
            retained.put(base, level.getBlockState(base));
            for (int y = 1; y <= 5; y++) {
                expectTrue("魔眼模板第" + y + "格黑曜石", level.getBlockState(base.above(y)).is(Blocks.OBSIDIAN));
                retained.put(base.above(y), level.getBlockState(base.above(y)));
            }
            BlockState eye = level.getBlockState(base.above(6));
            expectTrue("魔眼模板终格身份与来源180度变换", eye.is(block("moeye"))
                    && eye.getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.EAST);
            retained.put(base.above(6), eye);
        } finally {
            chunk.setHeightmap(Heightmap.Types.OCEAN_FLOOR_WG, heightmap);
        }
    }

    private void checkColumn(ServerLevel level, ChunkGenerator generator, Registry<ConfiguredFeature<?, ?>> features,
            String featureName, String blockName, BlockPos origin, int expectedHeight) {
        int maximum = featureName.equals("ironropeshengcheng") ? 60 : 5;
        for (int y = 0; y <= maximum; y++) {
            save(level, origin.above(y));
            level.setBlock(origin.above(y), Blocks.AIR.defaultBlockState(), FLAGS);
        }
        expectTrue(featureName + "实际生成", feature(features, featureName).place(level, generator, RandomSource.create(0L), origin));
        int height = 0;
        while (height < maximum && level.getBlockState(origin.above(height)).is(block(blockName))) height++;
        expect(featureName + "来源seed0列高", expectedHeight, height);
        expectTrue(featureName + "顶格保持空气", level.getBlockState(origin.above(height)).isAir());
    }

    private void checkHerbs(ServerPlayer player, ServerLevel level, ChunkGenerator generator,
            Registry<ConfiguredFeature<?, ?>> features) {
        for (BlockPos pos : BlockPos.betweenClosed(PATCH.offset(-7, -4, -7), PATCH.offset(7, 4, 7))) {
            save(level, pos);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), FLAGS);
        }
        for (BlockPos pos : BlockPos.betweenClosed(PATCH.offset(-7, -1, -7), PATCH.offset(7, -1, 7))) {
            level.setBlock(pos, block(pos.getX() < 8 ? "youbosoil" : "rottingsoil").defaultBlockState(), FLAGS);
        }
        for (String name : List.of("soulgrass", "corpsegrass")) {
            ConfiguredFeature<?, ?> grass = feature(features, name);
            expectTrue(name + "正式来源斑块实际生成", grass.place(level, generator, RandomSource.create(0L), PATCH));
            int count = 0;
            for (BlockPos pos : BlockPos.betweenClosed(PATCH.offset(-7, -3, -7), PATCH.offset(7, 3, 7))) {
                BlockState state = level.getBlockState(pos);
                if (!state.is(block(name))) continue;
                count++;
                expectTrue(name + "生成格按实际地面存活 " + pos, state.canSurvive(level, pos));
                expectTrue(name + "玩家采集不产生实体物品 " + pos,
                        Block.getDrops(state, level, pos, null, player, ItemStack.EMPTY).stream().allMatch(ItemStack::isEmpty));
                retained.put(pos.immutable(), state);
                retained.put(pos.below().immutable(), level.getBlockState(pos.below()));
            }
            expectTrue(name + "生成结果可供存活与存档消费者使用", count > 0);
            report.add("INFO 无归生成 受控来源斑块 " + name + " 存活格数=" + count);
        }
        player.teleportTo(level, 8.5, 202.5, 14.5, 180, 35);
    }

    private void save(ServerLevel level, BlockPos pos) {
        BlockPos stable = pos.immutable();
        if (saved.containsKey(stable)) return;
        BlockEntity entity = level.getBlockEntity(stable);
        saved.put(stable, new SavedBlock(level.getBlockState(stable),
                entity == null ? null : entity.saveWithFullMetadata(level.registryAccess())));
    }

    private void cleanup(ServerPlayer player) {
        ServerLevel level = player.server.getLevel(WUGUI);
        for (Map.Entry<BlockPos, SavedBlock> entry : saved.entrySet()) {
            BlockPos pos = entry.getKey();
            SavedBlock before = entry.getValue();
            level.removeBlockEntity(pos);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), FLAGS);
            level.setBlock(pos, before.state(), FLAGS);
            if (before.data() != null) level.setBlockEntity(BlockEntity.loadStatic(pos, before.state(), before.data(), level.registryAccess()));
        }
        saved.clear();
        retained.clear();
        if (!prepared) return;
        player.teleportTo(player.server.getLevel(originalDimension), originalPosition.x, originalPosition.y,
                originalPosition.z, originalYaw, originalPitch);
        player.setGameMode(originalMode);
        player.setNoGravity(originalNoGravity);
        player.getAbilities().loadSaveData(originalAbilities);
        player.onUpdateAbilities();
        prepared = false;
    }

    private static ConfiguredFeature<?, ?> feature(Registry<ConfiguredFeature<?, ?>> features, String path) {
        return features.getOptional(id(path)).orElseThrow();
    }

    private static Block block(String path) { return BuiltInRegistries.BLOCK.get(id(path)); }
    private static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path); }
    private record SavedBlock(BlockState state, CompoundTag data) {}
}
