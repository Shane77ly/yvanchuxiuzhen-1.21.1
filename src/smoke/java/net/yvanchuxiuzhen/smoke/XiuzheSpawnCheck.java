package net.yvanchuxiuzhen.smoke;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.ModId;

/** 检查修饰后的群系列表及原版自然抽签，不通过指令、刷怪蛋或手工创建实体。 */
final class XiuzheSpawnCheck extends SystemCheck {
    private static final int MAX_ATTEMPTS = 64;
    private static final int FIRST_STEP_RADIUS = 5;
    private final List<String> report;
    private final Map<BlockPos, BlockState> saved = new LinkedHashMap<>();
    private final boolean verifyReentry = Boolean.getBoolean("yvanchuxiuzhen.smoke.xiuzheNaturalReentry");
    private BlockPos open;
    private BlockPos light8;
    private BlockPos light9;
    private Vec3 start;
    private float yaw;
    private float pitch;
    private boolean noGravity;
    private boolean flying;
    private Difficulty originalDifficulty;
    private UUID retained;
    private long spawnTime;
    private float initializedMaxHealth;

    XiuzheSpawnCheck(List<String> report, String levelId) {
        super("修者0101自然出生", report, levelId);
        this.report = report;
    }

    @Override
    protected boolean step(int step, int local) {
        switch (step) {
            case 0 -> onServer(player -> {
                checkLists(player);
                prepare(player);
            });
            case 1 -> {
                if (local < 40) return false;
                onServer(this::checkSpawning);
                if (retained == null) finish();
            }
            case 2 -> {
                if (local < 25) return false;
                boolean[] ready = {false};
                onServer(player -> ready[0] = checkInitialized(player, local > 300));
                if (!ready[0]) return false;
                if (retained == null) finish();
                else leaveWorld();
            }
            case 3 -> { return reopened(local); }
            case 4 -> {
                if (!inWorld(local)) return false;
                onServer(this::checkReloaded);
                finish();
            }
            default -> throw new IllegalStateException("无效的修者自然出生检查步骤：" + step);
        }
        return true;
    }

    private void checkLists(ServerPlayer player) {
        EntityType<?> xiuzhe = xiuzhe();
        Registry<Biome> biomes = player.server.registryAccess().registryOrThrow(Registries.BIOME);
        expectTrue("原版下界荒地仍为共享群系", biomes.containsKey(ResourceLocation.withDefaultNamespace("nether_wastes")));
        expectTrue("虚空群系已加载", biomes.containsKey(id("xvkongqvnxi")));
        ServerLevel xiuluo = player.server.getLevel(ResourceKey.create(Registries.DIMENSION, id("dimensionxiuluo")));
        if (xiuluo != null) {
            var used = xiuluo.getChunkSource().getGenerator().getBiomeSource().possibleBiomes();
            expectTrue("修罗只引用原版下界荒地", used.size() == 1
                    && used.iterator().next().is(ResourceLocation.withDefaultNamespace("nether_wastes")));
        }
        boolean diling = player.server.getLevel(ResourceKey.create(Registries.DIMENSION, id("dimensiondiling"))) != null;
        if (diling) {
            for (int i = 1; i <= 8; i++) {
                ResourceLocation biome = id("biomediling_0" + i);
                expectTrue("地灵群系已加载 " + biome, biomes.containsKey(biome));
            }
        }
        checkGenerationGates(player);
        int inherited = 0;
        int appended = 0;
        for (ResourceLocation biomeId : biomes.keySet().stream().sorted().toList()) {
            Biome biome = biomes.get(biomeId);
            var original = biome.modifiableBiomeInfo().getOriginalBiomeInfo().mobSpawnSettings().getMobs(MobCategory.CREATURE).unwrap();
            var effective = biome.getMobSettings().getMobs(MobCategory.CREATURE).unwrap();
            var matches = effective.stream().filter(entry -> entry.type == xiuzhe).toList();
            expectTrue("唯一有效名单及来源参数 " + biomeId, matches.size() == 1
                    && matches.getFirst().getWeight().asInt() == 10
                    && matches.getFirst().minCount == 1 && matches.getFirst().maxCount == 1);
            var existing = original.stream().filter(entry -> entry.type == xiuzhe).toList();
            if (biomeId.getNamespace().equals("minecraft") || biomeId.equals(id("xvkongqvnxi"))) {
                expectTrue("原版或虚空由修饰器追加 " + biomeId, existing.isEmpty());
            }
            if (biomeId.getNamespace().equals(ModId.VALUE) && biomeId.getPath().startsWith("biomediling_")) {
                expectTrue("地灵内联条目不能由修饰器补写 " + biomeId, existing.size() == 1);
            }
            if (!existing.isEmpty()) {
                inherited++;
                expectTrue("已有条目保留原位置与对象 " + biomeId, original.equals(effective));
            } else {
                appended++;
                expectTrue("缺项只追加且保留原顺序 " + biomeId, effective.size() == original.size() + 1
                        && effective.subList(0, original.size()).equals(original));
            }
            for (MobCategory category : MobCategory.values()) {
                if (category == MobCategory.CREATURE) continue;
                if (biome.getMobSettings().getMobs(category).unwrap().stream().anyMatch(entry -> entry.type == xiuzhe)) {
                    fail("其他类别不得含修者 " + biomeId + " " + category);
                }
            }
        }
        expect("原有内联修者的群系数", diling ? 67 : 59, inherited);
        expect("锁定版原版64项与虚空1项由修饰器追加", 65, appended);
        report.add("INFO 修者0101自然出生 有效群系=" + (inherited + appended) + " 原条目=" + inherited + " 追加=" + appended);
    }

    private void checkGenerationGates(ServerPlayer player) {
        for (String path : List.of("dimensionrenjian", "dimensionyaohun", "dimensionxiuluo", "dimensiondiling",
                "dimensiontianqve", "dimensionwugui", "fabao_009weidu", "xvkong")) {
            ServerLevel level = player.server.getLevel(ResourceKey.create(Registries.DIMENSION, id(path)));
            if (level == null) continue;
            boolean enabled = switch (path) {
                case "dimensionrenjian", "dimensionyaohun", "dimensionxiuluo" -> true;
                default -> false;
            };
            expectTrue("来源新区块初始生物生成开关 " + path + " = " + enabled,
                    level.getChunkSource().getGenerator() instanceof NoiseBasedChunkGenerator noise
                            && noise.generatorSettings().value().disableMobGeneration() != enabled);
        }
    }

    private void prepare(ServerPlayer player) {
        ServerLevel level = player.server.overworld();
        start = player.position();
        yaw = player.getYRot();
        pitch = player.getXRot();
        noGravity = player.isNoGravity();
        flying = player.getAbilities().flying;
        ChunkPos chunk = new ChunkPos(player.blockPosition());
        open = new BlockPos(chunk.getMinBlockX() + 8, level.getMaxBuildHeight() - 48, chunk.getMinBlockZ() + 8);
        BlockPos sharedSpawn = level.getSharedSpawnPos();
        if (open.distSqr(sharedSpawn) <= 32 * 32) {
            open = open.offset(open.getX() >= sharedSpawn.getX() ? 32 : -32, 0, 0);
        }
        light8 = open.west(12);
        light9 = open.east(12);
        prepareSpawnRoom(level);
        chamber(level, light8, 8);
        chamber(level, light9, 9);
        player.setNoGravity(true);
        player.getAbilities().flying = true;
        player.onUpdateAbilities();
        player.teleportTo(level, open.getX() + 0.5, open.getY() + 36, open.getZ() + 0.5, 0, 45);
    }

    private void prepareSpawnRoom(ServerLevel level) {
        int wall = FIRST_STEP_RADIUS + 1;
        BlockState light = Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, 12);
        for (BlockPos pos : BlockPos.betweenClosed(open.offset(-wall, -1, -wall), open.offset(wall, 3, wall))) {
            int y = pos.getY() - open.getY();
            boolean shell = y == 3 || Math.abs(pos.getX() - open.getX()) == wall || Math.abs(pos.getZ() - open.getZ()) == wall;
            // 注册谓词要求原始亮度大于8；Monster实例检查还要求该位置的光成本不为正。
            BlockState state = shell ? Blocks.STONE.defaultBlockState() : switch (y) {
                case -1 -> Blocks.GRASS_BLOCK.defaultBlockState();
                case 2 -> light;
                default -> Blocks.AIR.defaultBlockState();
            };
            set(level, pos, state);
        }
    }

    private void chamber(ServerLevel level, BlockPos center, int brightness) {
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-2, -2, -2), center.offset(2, 2, 2))) {
            boolean shell = Math.abs(pos.getX() - center.getX()) == 2 || Math.abs(pos.getY() - center.getY()) == 2
                    || Math.abs(pos.getZ() - center.getZ()) == 2;
            set(level, pos, (shell ? Blocks.STONE : Blocks.AIR).defaultBlockState());
        }
        set(level, center.below(), Blocks.GRASS_BLOCK.defaultBlockState());
        set(level, center.above(), Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, brightness + 1));
    }

    private void set(ServerLevel level, BlockPos pos, BlockState state) {
        saved.putIfAbsent(pos.immutable(), level.getBlockState(pos));
        level.setBlock(pos, state, Block.UPDATE_ALL);
    }

    private void checkSpawning(ServerPlayer player) {
        ServerLevel level = player.server.overworld();
        EntityType<?> xiuzhe = xiuzhe();
        originalDifficulty = level.getDifficulty();
        try {
            expect("原生高度图", Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, SpawnPlacements.getHeightmapType(xiuzhe));
            expect("原生地面放置", SpawnPlacementTypes.ON_GROUND, SpawnPlacements.getPlacementType(xiuzhe));
            expectTrue("两个亮度样本均没有天空光", !level.canSeeSky(light8) && !level.canSeeSky(light9));
            expect("实测亮度8", 8, level.getRawBrightness(light8, 0));
            expect("实测亮度9", 9, level.getRawBrightness(light9, 0));
            expectTrue("草方块亮度8拒绝", !passes(xiuzhe, level, light8));
            expectTrue("草方块亮度9且地面有效", passes(xiuzhe, level, light9)
                    && SpawnPlacements.isSpawnPositionOk(xiuzhe, level, light9));
            expectTrue("新区块出生理由同样区分亮度8与9",
                    !SpawnPlacements.checkSpawnRules(xiuzhe, level, MobSpawnType.CHUNK_GENERATION, light8, RandomSource.create(0))
                            && SpawnPlacements.checkSpawnRules(xiuzhe, level, MobSpawnType.CHUNK_GENERATION, light9, RandomSource.create(0)));
            level.setBlock(light9.below(), Blocks.STONE.defaultBlockState(), Block.UPDATE_ALL);
            expectTrue("同亮度石头拒绝", !passes(xiuzhe, level, light9));
            level.setBlock(light9.below(), Blocks.MYCELIUM.defaultBlockState(), Block.UPDATE_ALL);
            expectTrue("同亮度菌丝符合来源草质而非只认草方块", passes(xiuzhe, level, light9));
            expectTrue("正例在共享原版群系", level.getBiome(open).unwrapKey().orElseThrow().location().getNamespace().equals("minecraft"));
            expectTrue("平台中心允许原生放置与位置谓词", SpawnPlacements.isSpawnPositionOk(xiuzhe, level, open)
                    && passes(xiuzhe, level, open));
            var chunk = level.getChunk(open);
            if (!checkFirstCandidates(player, level, xiuzhe, chunk.getPos())) return;
            int[] spawned = {0};
            int[] filtered = {0};
            int attempts = 0;
            // 保持非和平难度直到采样完成，避免 Monster 样本在后续刻被移除。
            player.server.setDifficulty(Difficulty.NORMAL, true);
            while (attempts < MAX_ATTEMPTS && spawned[0] == 0) {
                attempts++;
                NaturalSpawner.spawnCategoryForPosition(MobCategory.CREATURE, level, chunk, open,
                        (type, pos, ignored) -> {
                            if (type != xiuzhe) return false;
                            filtered[0]++;
                            return true;
                        },
                        (mob, ignored) -> {
                            expect("原生出生回调收到修者", xiuzhe, mob.getType());
                            boolean enrolled = level.getEntity(mob.getUUID()) == mob;
                            expectTrue("实体经原生自然出生入口入场", enrolled);
                            expect("原生 finalizeSpawn 的出生理由", MobSpawnType.NATURAL, mob.getSpawnType());
                            report.add("INFO 修者0101自然出生 抽签命中 " + mob.blockPosition().toShortString()
                                    + " 群系=" + level.getBiome(mob.blockPosition()).unwrapKey().orElseThrow().location());
                            spawned[0]++;
                            if (verifyReentry && retained == null && enrolled && mob.getSpawnType() == MobSpawnType.NATURAL) {
                                retained = mob.getUUID();
                                spawnTime = level.getGameTime();
                            } else {
                                mob.discard();
                            }
                        });
            }
            expectTrue("有界原生抽签实际生出修者，尝试=" + attempts + " 修者通过前置校验=" + filtered[0]
                    + " 完成入场=" + spawned[0], spawned[0] > 0);
            if (retained != null) {
                // 封闭取样场保留到重进完成；只移近玩家，不改变样本的位置、AI 或持久化标志。
                player.teleportTo(level, open.getX() + 0.5, open.getY() + 10, open.getZ() + 0.5, 0, 90);
            }
        } finally {
            if (retained == null) restore(player);
        }
    }

    private boolean checkFirstCandidates(ServerPlayer player, ServerLevel level, EntityType<?> type, ChunkPos chunk) {
        int checked = 0;
        int radius = FIRST_STEP_RADIUS;
        for (BlockPos pos : BlockPos.betweenClosed(open.offset(-radius, 0, -radius), open.offset(radius, 0, radius))) {
            Vec3 center = Vec3.atBottomCenterOf(pos);
            double d2 = player.distanceToSqr(center);
            if (!new ChunkPos(pos).equals(chunk) || d2 <= 24 * 24 || level.getSharedSpawnPos().closerToCenterThan(center, 24)
                    || d2 > type.getCategory().getDespawnDistance() * type.getCategory().getDespawnDistance()
                    || level.canSeeSky(pos) || level.getRawBrightness(pos, 0) != 10 || level.getPathfindingCostFromLightLevels(pos) > 0
                    || !SpawnPlacements.isSpawnPositionOk(type, level, pos) || !passes(type, level, pos)
                    || !level.noCollision(type.getSpawnAABB(center.x, center.y, center.z))) {
                fail("首步候选条件不成立：" + pos.toShortString() + " 光=" + level.getRawBrightness(pos, 0)
                        + " 光成本=" + level.getPathfindingCostFromLightLevels(pos) + " 玩家距离平方=" + d2);
                return false;
            }
            checked++;
        }
        List<MobSpawnSettings.SpawnerData> rows = level.getChunkSource().getGenerator()
                .getMobsAt(level.getBiome(open), level.structureManager(), MobCategory.CREATURE, open).unwrap();
        int totalWeight = rows.stream().mapToInt(row -> row.getWeight().asInt()).sum();
        boolean groupOne = rows.stream().anyMatch(row -> row.type == type && row.minCount == 1 && row.maxCount == 1);
        expectTrue("目标实际 CREATURE 表中组大小为1–1", groupOne);
        report.add("INFO 修者0101自然出生 首步候选=" + checked + " 生物总权重=" + totalWeight
                + " doMobSpawning=" + level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING)
                + " 中心实体可刻=" + level.isNaturalSpawningAllowed(open) + " 重进=" + verifyReentry
                + "；直接调用位置抽签不覆盖 tick 的规则与数量上限");
        return groupOne;
    }

    private boolean checkInitialized(ServerPlayer player, boolean expired) {
        ServerLevel level = player.server.overworld();
        if (level.getGameTime() - spawnTime < 25 && !expired) return false;
        if (!(level.getEntity(retained) instanceof Mob mob) || !mob.isAlive()) {
            fail("自然样本在保存前已丢失，不能换用其他实体：" + retained);
            retained = null;
            restore(player);
        } else {
            initializedMaxHealth = mob.getMaxHealth();
            expectTrue("来源延迟20刻的初始生命上限（101–122）已生效：" + initializedMaxHealth,
                    initializedMaxHealth >= 101 && initializedMaxHealth <= 122);
            expectTrue("正常自然样本没有人为设置持久化", !mob.isPersistenceRequired());
            report.add("INFO 修者0101自然出生 保存自然样本 UUID=" + retained);
        }
        return true;
    }

    private void checkReloaded(ServerPlayer player) {
        ServerLevel level = player.server.overworld();
        try {
            if (!(level.getEntity(retained) instanceof Mob mob) || !mob.isAlive()) {
                fail("原生关档重开后未读回同一存活修者：" + retained);
                return;
            }
            expect("重开后同 UUID 的实体类型", xiuzhe(), mob.getType());
            expect("重开后保留自然出生理由", MobSpawnType.NATURAL, mob.getSpawnType());
            expect("重开后保留出生初始化的生命上限", initializedMaxHealth, mob.getMaxHealth());
            expectTrue("重开后仍未人为持久化", !mob.isPersistenceRequired());
            mob.discard();
        } finally {
            restore(player);
        }
    }

    private void restore(ServerPlayer player) {
        ServerLevel level = player.server.overworld();
        player.server.setDifficulty(originalDifficulty, true);
        saved.forEach((pos, state) -> level.setBlock(pos, state, Block.UPDATE_ALL));
        player.setNoGravity(noGravity);
        player.getAbilities().flying = flying;
        player.onUpdateAbilities();
        player.teleportTo(level, start.x, start.y, start.z, yaw, pitch);
    }

    private static boolean passes(EntityType<?> type, ServerLevel level, BlockPos pos) {
        return SpawnPlacements.checkSpawnRules(type, level, MobSpawnType.NATURAL, pos, RandomSource.create(0));
    }

    private static EntityType<?> xiuzhe() {
        return BuiltInRegistries.ENTITY_TYPE.getOptional(id("xiuzhe_0101")).orElseThrow();
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path);
    }
}
