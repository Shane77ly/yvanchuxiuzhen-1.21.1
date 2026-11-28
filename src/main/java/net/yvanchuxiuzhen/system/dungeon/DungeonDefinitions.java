package net.yvanchuxiuzhen.system.dungeon;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.yvanchuxiuzhen.platform.content.ContentFiles;
import net.yvanchuxiuzhen.system.dungeon.content.DungeonBlockEntity;
import net.yvanchuxiuzhen.system.dungeon.data.ControllerMobSpawn;
import net.yvanchuxiuzhen.system.dungeon.data.DungeonDefinition;
import net.yvanchuxiuzhen.system.dungeon.data.DungeonDefinition.DungeonMobSpawn;
import net.yvanchuxiuzhen.system.dungeon.data.DungeonDefinition.DungeonRewardEntry;
import net.yvanchuxiuzhen.system.dungeon.data.MarkerInfo;

/** 副本强类型定义加载器，以及控制器重置、波次/奖励展开和镇妖塔44点配置。 */
public final class DungeonDefinitions {
    public static final String RESOURCE_PATH = "content/dungeon/dungeons.json";
    public static final String DEFAULT_ID = "default";
    private static Map<String, DungeonDefinition> byId;

    private DungeonDefinitions() {
    }

    public static List<DungeonDefinition> all() {
        return List.copyOf(index().values());
    }

    public static DungeonDefinition byId(String id) {
        return index().getOrDefault(id, DungeonDefinition.EMPTY);
    }

    public static void configureTower(ServerLevel level) {
        for (DungeonDefinition definition : all()) {
            if (definition.towerPos().isEmpty()) continue;
            BlockPos pos = definition.towerPos().get();
            if (level.getBlockEntity(pos) instanceof DungeonBlockEntity block) {
                block.setDefinitionId(definition.id());
                block.changed();
            }
            level.setBlock(pos.below(), DungeonPlayers.block("ginkgofloor").defaultBlockState(), 3);
        }
    }

    static void reset(DungeonBlockEntity block) {
        block.resetWaveCounts();
        block.setControllerTick(0);
        block.setWaveMax(0);
        block.setWaveIndex(0);
        block.setEndTime(0);
        block.setSpawnCursor(0);
        block.setWaveEntityCount(0);
        block.resetMobSpawns();
        // Q106：沿列表顺序遇到 X 偏移为 0 的占位符即停止清理。
        for (MarkerInfo marker : block.markers()) {
            if (marker.x() == 0) break;
            BlockPos pos = block.getBlockPos().offset(marker.x(), marker.y(), marker.z());
            if (block.getLevel().getBlockEntity(pos) instanceof DungeonBlockEntity found && found.kind().equals("fubenentity")) {
                block.getLevel().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }

    static void parse(DungeonBlockEntity block) {
        DungeonDefinition definition = block.customDefinition()
                .orElseGet(() -> byId(block.definitionId()));
        List<List<DungeonMobSpawn>> waves = definition.waves();
        block.setWaveMax(waves.size());
        block.setEndTime(definition.timeLimit());
        for (int waveIndex = 0; waveIndex < waves.size(); waveIndex++) {
            List<DungeonMobSpawn> mobs = waves.get(waveIndex);
            block.setWaveCount(waveIndex + 1, mobs.size());
            for (DungeonMobSpawn mob : mobs) {
                MarkerInfo marker = block.findMarker(mob.markerId()).orElse(MarkerInfo.EMPTY);
                block.addMobSpawn(new ControllerMobSpawn(
                        mob.entityId(), mob.level(),
                        marker.x(), marker.y(), marker.z(), marker.facing(), marker.attack()));
            }
        }
        block.resetRewardWeights();
        List<DungeonRewardEntry> rewards = definition.rewards();
        for (int slot = 0; slot < Math.min(18, rewards.size()); slot++) {
            DungeonRewardEntry reward = rewards.get(slot);
            block.setRewardWeight(slot, reward.weight());
            block.setItem(slot, DungeonRewards.create(reward));
        }
    }

    static void configure(DungeonBlockEntity block, double side) {
        int length = (int) side;
        if (side >= 1) {
            block.setSideLength(length);
            int half = length / 2;
            block.setCenterOffset(half, half, half);
        }
        for (int x = 0; x < length; x++) {
            for (int y = 0; y < length; y++) {
                for (int z = 0; z < length; z++) {
                    if (block.getLevel().getBlockEntity(block.getBlockPos().offset(x, y, z)) instanceof DungeonBlockEntity marker
                            && marker.kind().equals("fubenentity")) {
                        marker.setMarkerOffset(x, y, z);
                        block.addMarker(marker.toMarkerInfo());
                        block.incrementMarkerCount();
                        marker.changed();
                    }
                }
            }
        }
        block.changed();
    }

    private static Map<String, DungeonDefinition> index() {
        if (byId == null) {
            Map<String, DungeonDefinition> loaded = new LinkedHashMap<>();
            for (DungeonDefinition definition : ContentFiles.read(RESOURCE_PATH, DungeonDefinition.CODEC)) {
                loaded.put(definition.id(), definition);
            }
            byId = Map.copyOf(loaded);
        }
        return byId;
    }
}
