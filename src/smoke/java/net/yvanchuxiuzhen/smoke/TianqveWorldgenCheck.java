package net.yvanchuxiuzhen.smoke;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.ModId;

/** 天阙正式数据加载、有限自然区块、地物维度拒绝与来源内联树的受控检查。 */
final class TianqveWorldgenCheck extends SystemCheck {
    private static final ResourceKey<Level> TIANQVE = ResourceKey.create(Registries.DIMENSION, id("dimensiontianqve"));
    private static final BlockPos ORIGIN = new BlockPos(8, 200, 8);
    private static final List<String> BIOME_NAMES = List.of("天云岛", "碎云海", "神木山", "神木原", "烈阳潭", "黑云山", "龙吸山");
    private static final List<String> EXTERNALS = List.of(
            "cailiao_16101", "cailiao_16201", "cailiao_16301", "cailiao_16401", "cailiao_16501",
            "cailiao_16601", "cailiao_16701", "cailiao_16801", "cailiao_16901", "cailiao_191ore",
            "cailiao_192ore", "cailiao_193ore", "cailiao_194ore", "cailiao_195ore", "cailiao_196ore",
            "cailiao_197ore", "cailiao_198ore", "cailiao_199ore", "cailiao_20101", "cailiao_20301",
            "cailiao_20501", "cailiao_20701", "cailiao_20801", "cailiao_20901", "cailiao_21101",
            "cailiao_21301", "cailiao_21501", "cailiao_21601", "cailiao_21701", "cailiao_21901",
            "fuluzhushablock", "nichanggrass_06", "suijijiegou_01", "suijijiegou_02", "suijijiegou_03",
            "suijijiegou_04", "suijijiegou_05", "suijijiegou_06");
    private final Screenshots screenshots;
    private final List<String> report;
    private final Map<BlockPos, SavedBlock> saved = new LinkedHashMap<>();
    private ResourceKey<Level> originalDimension;
    private Vec3 originalPosition;
    private float originalYaw;
    private float originalPitch;
    private GameType originalMode;
    private CompoundTag originalAbilities;
    private boolean originalNoGravity;
    private boolean prepared;

    TianqveWorldgenCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("天阙生成", report, levelId);
        this.report = report;
        this.screenshots = screenshots;
    }

    @Override
    protected boolean step(int step, int local) {
        try {
            switch (step) {
                case 0 -> onServer(this::checkWorld);
                case 1 -> {
                    if (local > 600) throw new IllegalStateException("天阙客户端画面同步超时");
                    if (local < 30 || minecraft.screen != null || minecraft.level == null
                            || !minecraft.level.dimension().equals(TIANQVE)) return false;
                    for (int number = 1; number <= 7; number++) {
                        String key = "biome." + ModId.VALUE + ".biometianqve_0" + number;
                        expect("群系中文已合并 " + number, BIOME_NAMES.get(number - 1), I18n.get(key));
                    }
                    screenshots.take("tianqve_natural_clouds.png");
                }
                case 2 -> {
                    onServer(this::cleanup);
                    finish();
                }
                default -> throw new IllegalStateException("未知天阙检查步骤 " + step);
            }
        } catch (RuntimeException exception) {
            fail("步骤" + step + "异常：" + exception);
            onServer(this::cleanup);
            finish();
        }
        return true;
    }

    private void checkWorld(ServerPlayer player) {
        ServerLevel level = player.server.getLevel(TIANQVE);
        if (level == null) throw new IllegalStateException("正式天阙维度未加载");
        ChunkGenerator generator = level.getChunkSource().getGenerator();
        Set<ResourceLocation> actualBiomes = new HashSet<>();
        generator.getBiomeSource().possibleBiomes().forEach(holder -> actualBiomes.add(holder.unwrapKey().orElseThrow().location()));
        Set<ResourceLocation> expectedBiomes = new HashSet<>();
        for (int number = 1; number <= 7; number++) expectedBiomes.add(id("biometianqve_0" + number));
        expect("实际biome_source完整七群系", expectedBiomes, actualBiomes);
        LevelChunk chunk = level.getChunk(0, 0);
        int clouds = 0;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < 128; y++) {
                    if (chunk.getBlockState(new BlockPos(x, y, z)).is(block("cloudblock"))) clouds++;
                }
            }
        }
        expectTrue("有限自然区块实际生成云块", clouds > 0);
        report.add("INFO 天阙生成 边界：只取原点一块完整区块；38地物直接调用核验拒绝结果；不证明全地形分布、自然出生率或自然树木供给");
        Registry<ConfiguredFeature<?, ?>> configured = level.registryAccess().registryOrThrow(Registries.CONFIGURED_FEATURE);
        for (String name : EXTERNALS) {
            ConfiguredFeature<?, ?> feature = configured.getOptional(id(name)).orElseThrow();
            expectTrue("来源维度门拒绝 " + name, !feature.place(level, generator, RandomSource.create(0L), ORIGIN));
        }
        checkTree(level, generator, configured);
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
        player.teleportTo(level, 8.5, Math.max(30, surface + 12), 8.5, 45, 35);
    }

    private void checkTree(ServerLevel level, ChunkGenerator generator, Registry<ConfiguredFeature<?, ?>> configured) {
        for (BlockPos position : BlockPos.betweenClosed(ORIGIN.offset(-4, -1, -4), ORIGIN.offset(4, 10, 4))) {
            BlockPos stable = position.immutable();
            BlockEntity entity = level.getBlockEntity(stable);
            saved.put(stable, new SavedBlock(level.getBlockState(stable), entity == null ? null : entity.saveWithFullMetadata(level.registryAccess())));
        }
        try {
            clear(level);
            PlacedFeature tree = level.registryAccess().registryOrThrow(Registries.PLACED_FEATURE)
                    .getOptional(id("tree_biometianqve_04")).orElseThrow();
            PlacementContext context = new PlacementContext(level, generator, Optional.of(tree));
            level.setBlock(ORIGIN.below(), block("cloudblock").defaultBlockState(), 18);
            expect("内联橡树在原生云层无存活落点", 0, tree.placement().get(4)
                    .getPositions(context, RandomSource.create(0L), ORIGIN).count());
            for (String name : List.of("scorchedsoil", "loess", "youbosoil", "rottingsoil", "grassblock")) {
                level.setBlock(ORIGIN.below(), block(name).defaultBlockState(), 18);
                expect("来源五土壤保留树苗存活落点 " + name, 1, tree.placement().get(4)
                        .getPositions(context, RandomSource.create(0L), ORIGIN).count());
            }
            level.setBlock(ORIGIN.below(), Blocks.GRASS_BLOCK.defaultBlockState(), 18);
            for (BlockPos position : BlockPos.betweenClosed(ORIGIN.offset(-3, 0, -3), ORIGIN.offset(3, 8, 3))) {
                level.setBlock(position, Blocks.WATER.defaultBlockState(), 18);
            }
            ConfiguredFeature<?, ?> feature = configured.getOptional(id("tree_biometianqve_04")).orElseThrow();
            expect("内联树使用来源生成类型", id("legacy_tree"), BuiltInRegistries.FEATURE.getKey(feature.feature()));
            expectTrue("来源内联树在受控水体中实际生成", feature.place(level, generator, RandomSource.create(0L), ORIGIN));
            expectTrue("来源树干实际替换水体", level.getBlockState(ORIGIN).is(Blocks.OAK_LOG));
            expectTrue("来源树干将草方块转成泥土", level.getBlockState(ORIGIN.below()).is(Blocks.DIRT));
            int leaves = 0;
            int waterlogged = 0;
            for (BlockPos position : BlockPos.betweenClosed(ORIGIN.offset(-3, 0, -3), ORIGIN.offset(3, 9, 3))) {
                BlockState state = level.getBlockState(position);
                if (state.is(Blocks.OAK_LEAVES)) {
                    leaves++;
                    if (state.getValue(BlockStateProperties.WATERLOGGED)) waterlogged++;
                }
            }
            expectTrue("受控水体实际出现橡树叶", leaves > 0);
            expect("来源树叶保留非含水提供器状态", 0, waterlogged);
        } finally {
            restore(level);
        }
    }

    private void clear(ServerLevel level) {
        for (BlockPos position : saved.keySet()) level.setBlock(position, Blocks.AIR.defaultBlockState(), 18);
    }

    private void restore(ServerLevel level) {
        for (Map.Entry<BlockPos, SavedBlock> entry : saved.entrySet()) {
            level.setBlock(entry.getKey(), entry.getValue().state(), 18);
            if (entry.getValue().tag() != null) {
                BlockEntity entity = level.getBlockEntity(entry.getKey());
                if (entity == null) throw new IllegalStateException("还原方块实体缺失 " + entry.getKey());
                entity.loadWithComponents(entry.getValue().tag(), level.registryAccess());
            }
        }
        saved.clear();
    }

    private void cleanup(ServerPlayer player) {
        if (!saved.isEmpty()) restore(player.server.getLevel(TIANQVE));
        if (!prepared) return;
        player.teleportTo(player.server.getLevel(originalDimension), originalPosition.x, originalPosition.y,
                originalPosition.z, originalYaw, originalPitch);
        player.setGameMode(originalMode);
        player.setNoGravity(originalNoGravity);
        player.getAbilities().loadSaveData(originalAbilities);
        player.onUpdateAbilities();
        prepared = false;
    }

    private static Block block(String path) { return BuiltInRegistries.BLOCK.get(id(path)); }
    private static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path); }
    private record SavedBlock(BlockState state, CompoundTag tag) {}
}
