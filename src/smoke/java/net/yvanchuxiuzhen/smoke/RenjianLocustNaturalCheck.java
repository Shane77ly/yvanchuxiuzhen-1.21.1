package net.yvanchuxiuzhen.smoke;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.equipment.content.Fabao01701Entity;
import net.yvanchuxiuzhen.system.guchong.content.GuchongJarData;
import net.yvanchuxiuzhen.system.guchong.entity.Guchong03Entity;
import net.yvanchuxiuzhen.system.world.terrain.LegacyDimensionChunkGenerator;
import net.yvanchuxiuzhen.system.xiulian.XiulianRules;

/** 只采样既有风系站点，核对同一自然入场飞蝗被玩家投网收纳。 */
final class RenjianLocustNaturalCheck extends SystemCheck {
    static final String SELECTOR = "renjianlocustnatural";
    static final String SAVE = "renjian-locust-natural-frozen-20261119";
    private static final long WORLD_SEED = -5586163949850800998L;
    private static final long TERRAIN_SEED = -3958683526324992456L;
    private static final UUID FROZEN_PLAYER = UUID.fromString("380df991-f603-344c-a090-369bad2a924a");
    private static final ResourceKey<Level> RENJIAN = ResourceKey.create(Registries.DIMENSION, id("dimensionrenjian"));
    private static final ResourceLocation NET_ITEM = id("fabao_017");
    private static final ResourceLocation LOCUST_ITEM = id("guchong_03item");
    private static final List<Station> STATIONS = List.of(
            new Station(id("biomewind_01"), new BlockPos(4780, 71, 2629), new BlockPos(4750, 71, 2656)),
            new Station(id("biomewind_03"), new BlockPos(3860, 69, 2412), new BlockPos(3893, 69, 2391)),
            new Station(id("biomewind_02"), new BlockPos(5266, 69, 2268), new BlockPos(5305, 69, 2272)));
    private static final int MAX_TICKS = 12000;

    private final List<String> report;
    private final Consumer<LevelTickEvent.Pre> ticking = this::onLevelTick;
    private final Consumer<FinalizeSpawnEvent> spawning = this::onSpawn;
    private final Consumer<EntityJoinLevelEvent> joining = this::onJoin;
    private final Consumer<EntityLeaveLevelEvent> leaving = this::onLeave;
    private final Map<UUID, Guchong03Entity> pending = new HashMap<>();
    private final Map<UUID, Guchong03Entity> born = new HashMap<>();
    private final Set<UUID> tried = new HashSet<>();
    private final boolean[] readyReported = new boolean[STATIONS.size()];
    private final int[] stationWindows = new int[STATIONS.size()];
    private ServerPlayer observer;
    private ServerLevel renjian;
    private Guchong03Entity target;
    private Aim aim;
    private Fabao01701Entity net;
    private UUID captured;
    private int station;
    private int stationAt;
    private int windows;
    private long lastCountedWindowTick = Long.MIN_VALUE;
    private int selectedAt;
    private int casts;
    private int nextMove;
    private int nextInput;

    RenjianLocustNaturalCheck(List<String> report, String levelId) {
        super("人间道自然飞蝗摄网", report, levelId);
        this.report = report;
    }

    @Override
    protected boolean step(int step, int local) {
        if (step == 0) {
            onServer(this::start);
            return true;
        }
        onServer(player -> observe(player, local));
        cast(local);
        return false;
    }

    private void start(ServerPlayer player) {
        ServerLevel destination = player.server.getLevel(RENJIAN);
        var rules = player.serverLevel().getGameRules();
        var players = player.server.getPlayerList();
        if (!player.level().dimension().equals(Level.OVERWORLD) || !FROZEN_PLAYER.equals(player.getUUID())
                || player.serverLevel().getSeed() != WORLD_SEED || destination == null
                || !(destination.getChunkSource().getGenerator() instanceof LegacyDimensionChunkGenerator generator)
                || generator.terrainSeed() != TERRAIN_SEED || player.serverLevel().getDifficulty() != Difficulty.NORMAL
                || player.gameMode.getGameModeForPlayer() != GameType.SURVIVAL
                || !rules.getBoolean(GameRules.RULE_DOMOBSPAWNING) || !rules.getBoolean(GameRules.RULE_DOMOBLOOT)
                || !rules.getBoolean(GameRules.RULE_DAYLIGHT) || rules.getBoolean(XiulianRules.BIRTH_SELECTION)
                || players.getViewDistance() != 2 || players.getSimulationDistance() != 5
                || !itemId(player.getInventory().getItem(0)).equals(NET_ITEM) || insectCount(player) != 0) {
            fail("冻结副本前置条件不符：主世界、固定玩家与种子、普通难度生存、自然规则、视距2/5及快捷栏0摄虫网");
            finish();
            return;
        }
        observer = player;
        renjian = destination;
        report.add("INFO REPLAY_LOCUST_START dimension=" + player.level().dimension().location()
                + " uuid=" + player.getUUID() + " worldSeed=" + WORLD_SEED + " terrainSeed=" + TERRAIN_SEED
                + " serverView=" + players.getViewDistance() + " serverSimulation=" + players.getSimulationDistance());
        NeoForge.EVENT_BUS.addListener(ticking);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, spawning);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, joining);
        NeoForge.EVENT_BUS.addListener(leaving);
        navigate(0);
    }

    private void navigate(int tick) {
        Station current = STATIONS.get(station);
        BlockPos feet = current.feet();
        windows = 0;
        stationAt = tick;
        observer.teleportTo(renjian, feet.getX() + 0.5, feet.getY(), feet.getZ() + 0.5, 0, 0);
        report.add("INFO REPLAY_STATION_NAV biome=" + current.biome() + " feet=" + feet);
    }

    private void onLevelTick(LevelTickEvent.Pre event) {
        if (event.getLevel() != renjian || observer.level() != renjian) return;
        Station current = STATIONS.get(station);
        BlockPos anchor = current.anchor();
        BlockPos feet = observer.blockPosition();
        boolean atStation = feet.distSqr(current.feet()) <= 16 && renjian.hasChunkAt(anchor)
                && renjian.isPositionEntityTicking(anchor) && biome(feet).equals(current.biome()) && biome(anchor).equals(current.biome())
                && renjian.getBlockState(anchor.below()).is(Blocks.GRASS_BLOCK) && renjian.isEmptyBlock(anchor)
                && renjian.isEmptyBlock(anchor.above()) && renjian.getRawBrightness(anchor, 0) > 8;
        if (!atStation) return;
        if (!readyReported[station]) {
            readyReported[station] = true;
            report.add("INFO REPLAY_STATION_READY biome=" + current.biome() + " anchor=" + anchor);
        }
        long now = renjian.getGameTime();
        // 调度窗口只用于取样统计和换站，不参与出生身份判定。
        if (now % 400 == 0 && now != lastCountedWindowTick) {
            lastCountedWindowTick = now;
            windows++;
            stationWindows[station]++;
            report.add("INFO REPLAY_CLOCK biome=" + current.biome() + " gameTime=" + now + " stationWindow=" + stationWindows[station]);
        }
    }

    private void onSpawn(FinalizeSpawnEvent event) {
        if (event.getLevel() != renjian || event.getSpawnType() != MobSpawnType.NATURAL || event.isSpawnCancelled()
                || !(event.getEntity() instanceof Guchong03Entity insect)) return;
        pending.put(insect.getUUID(), insect);
    }

    private void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel() != renjian || event.loadedFromDisk() || event.isCanceled()) return;
        if (event.getEntity() instanceof Fabao01701Entity projectile
                && observer.getStringUUID().equals(projectile.masterUuid())
                && "摄虫网".equals(projectile.skill())) {
            net = projectile;
            return;
        }
        if (event.getEntity() instanceof Guchong03Entity insect && pending.remove(insect.getUUID()) == insect) {
            born.put(insect.getUUID(), insect);
            long gameTime = renjian.getGameTime();
            report.add("INFO REPLAY_LOCUST_JOIN uuid=" + insect.getUUID() + " finalize=NATURAL loadedFromDisk=false"
                    + " pos=" + insect.blockPosition() + " gameTime=" + gameTime
                    + " countedWindow=" + (gameTime == lastCountedWindowTick)
                    + " level=" + EntityAttributes.level(insect));
        }
    }

    private void onLeave(EntityLeaveLevelEvent event) {
        if (captured != null || event.getLevel() != renjian || !(event.getEntity() instanceof Guchong03Entity insect)
                || born.get(insect.getUUID()) != insect || insect.getRemovalReason() != Entity.RemovalReason.DISCARDED
                || net == null || net.isRemoved() || net.timer1() != 36
                || !net.getBoundingBox().inflate(2).intersects(insect.getBoundingBox()) || insectCount(observer) != 1) return;
        for (int slot = 0; slot < observer.getInventory().getContainerSize(); slot++) {
            ItemStack stack = observer.getInventory().getItem(slot);
            if (!itemId(stack).equals(LOCUST_ITEM)) continue;
            var data = GuchongJarData.read(stack);
            if (data.getString("guchongid").equals(id("guchong_03").toString()) && !data.getBoolean("guchongxvnfu")
                    && data.getDouble("LEVELnumber") == EntityAttributes.level(insect)
                    && data.getDouble("attack_Num") == EntityAttributes.stat(insect, Stat.ATTACK)
                    && data.getDouble("health_Num") == insect.getHealth()) {
                captured = insect.getUUID();
                report.add("INFO REPLAY_CAPTURE wild=" + captured + " net=" + net.getUUID() + " netAge=36 slot=" + slot + " count=0->1");
                return;
            }
        }
    }

    private void observe(ServerPlayer player, int tick) {
        aim = null;
        if (captured != null) {
            report.add("INFO REPLAY_LOCUST_COMPLETE uuid=" + captured);
            stop();
            return;
        }
        if (tick >= MAX_TICKS || player != observer || !player.isAlive() || player.level() != renjian) {
            report.add("INFO REPLAY_LOCUST_MISS 自然入场飞蝗=" + born.size() + "，尝试个体=" + tried.size()
                    + "，风站机会=" + List.of(stationWindows[0], stationWindows[1], stationWindows[2])
                    + "，仍在人间道且存活=" + (player.level() == renjian && player.isAlive()));
            stop();
            return;
        }
        if (net != null && !net.isRemoved()) return;
        if (target != null && (!target.isAlive() || tick - selectedAt >= 400 || casts >= 7)) {
            target = null;
            navigate(tick);
            return;
        }
        if (target == null) {
            target = born.values().stream().filter(insect -> insect.isAlive() && insect.level() == renjian
                            && !tried.contains(insect.getUUID()) && player.distanceToSqr(insect) <= 96 * 96)
                    .min(Comparator.comparingDouble(player::distanceToSqr)).orElse(null);
            if (target == null) {
                if (windows >= 4 || tick - stationAt >= 2200) {
                    station = (station + 1) % STATIONS.size();
                    navigate(tick);
                }
                return;
            }
            tried.add(target.getUUID());
            selectedAt = tick;
            nextMove = tick;
            casts = 0;
            report.add("INFO REPLAY_NET_TARGET uuid=" + target.getUUID());
        }
        double distance = player.distanceToSqr(target);
        if (tick >= nextMove && tick >= nextInput && (distance > 4.5 || distance < 0.6)) {
            approach(player, target);
            nextMove = tick + 12;
        } else if (distance >= 0.6 && distance <= 4.5 && Math.abs(player.getY() - target.getY()) <= 1.25) {
            aim = new Aim(target.getId(), target.getUUID());
        }
    }

    private void cast(int tick) {
        var player = minecraft.player;
        if (aim == null || tick < nextInput || player == null || minecraft.level == null || minecraft.screen != null
                || minecraft.gameMode == null || !itemId(player.getInventory().getItem(0)).equals(NET_ITEM)) return;
        Entity insect = minecraft.level.getEntity(aim.entityId());
        if (insect == null || !insect.isAlive() || !aim.uuid().equals(insect.getUUID())) return;
        player.getInventory().selected = 0;
        Vec3 lead = insect.position().subtract(player.position());
        double scale = switch (casts % 3) { case 0 -> 0.45; case 1 -> 0.7; default -> 0.25; };
        player.lookAt(EntityAnchorArgument.Anchor.EYES, player.position().add(lead.x * scale, -0.18, lead.z * scale));
        player.connection.send(new ServerboundMovePlayerPacket.Rot(player.getYRot(), player.getXRot(), player.onGround()));
        report.add("INFO REPLAY_CLIENT_NET uuid=" + insect.getUUID() + " cast=" + ++casts);
        minecraft.gameMode.useItem(player, InteractionHand.MAIN_HAND);
        player.swing(InteractionHand.MAIN_HAND);
        nextInput = tick + 56;
    }

    private void approach(ServerPlayer player, Entity insect) {
        double base = Math.atan2(player.getZ() - insect.getZ(), player.getX() - insect.getX());
        for (int i = 0; i < 8; i++) {
            double angle = base + i * Math.PI / 4;
            double x = insect.getX() + 1.55 * Math.cos(angle);
            double z = insect.getZ() + 1.55 * Math.sin(angle);
            for (int dy : new int[] {0, -1, 1, -2, 2}) {
                BlockPos feet = BlockPos.containing(x, Math.floor(insect.getY()) + dy, z);
                Vec3 pos = new Vec3(x, feet.getY(), z);
                if (renjian.hasChunkAt(feet) && renjian.isPositionEntityTicking(feet)
                        && renjian.getBlockState(feet.below()).isCollisionShapeFullBlock(renjian, feet.below())
                        && renjian.getFluidState(feet).isEmpty() && renjian.getFluidState(feet.above()).isEmpty()
                        && renjian.noCollision(player, player.getBoundingBox().move(pos.subtract(player.position())))) {
                    player.teleportTo(renjian, x, feet.getY(), z, player.getYRot(), player.getXRot());
                    return;
                }
            }
        }
    }

    private void stop() {
        NeoForge.EVENT_BUS.unregister(ticking);
        NeoForge.EVENT_BUS.unregister(spawning);
        NeoForge.EVENT_BUS.unregister(joining);
        NeoForge.EVENT_BUS.unregister(leaving);
        finish();
    }

    private int insectCount(ServerPlayer player) {
        return player.getInventory().countItem(BuiltInRegistries.ITEM.get(LOCUST_ITEM));
    }

    private ResourceLocation biome(BlockPos pos) {
        return renjian.getBiome(pos).unwrapKey().orElseThrow().location();
    }

    private static ResourceLocation itemId(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem());
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", path);
    }

    private record Station(ResourceLocation biome, BlockPos feet, BlockPos anchor) { }
    private record Aim(int entityId, UUID uuid) { }
}
