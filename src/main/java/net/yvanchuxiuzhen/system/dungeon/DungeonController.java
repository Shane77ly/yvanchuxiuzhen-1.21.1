package net.yvanchuxiuzhen.system.dungeon;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.yvanchuxiuzhen.system.creature.api.DungeonCreatures;
import net.yvanchuxiuzhen.system.dungeon.content.DungeonBlockEntity;
import net.yvanchuxiuzhen.system.dungeon.data.ControllerMobSpawn;
import net.yvanchuxiuzhen.system.dungeon.data.DungeonData;
import net.yvanchuxiuzhen.system.dungeon.data.DungeonTransientData;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 控制器每秒推进来源阶段；胜利、失败、超时和延后回程各自保留原顺序。 */
public final class DungeonController {
    private static final List<ReturnTask> RETURNS = new ArrayList<>();

    private DungeonController() {
    }

    public static void tick(DungeonBlockEntity block) {
        if (!block.kind().equals("fubenblock")) return;
        if (block.openRequested()) {
            DungeonDefinitions.reset(block);
            DungeonDefinitions.parse(block);
            block.setPhase("准备");
            block.setActive(true);
            block.setOpenRequested(false);
            block.setControllerTick(0);
        } else if (block.active()) {
            block.setControllerTick(block.controllerTick() + 1);
            String phase = block.phase();
            if (phase.equals("准备") && block.controllerTick() >= 7) {
                DungeonTitles.prepare(players(block), block.dungeonName());
                block.setControllerTick(0);
                block.setWaveIndex(1);
                block.setWaveEntityCount(block.waveCount(1));
                block.setPhase("释放敌人");
            } else if (phase.equals("释放敌人") && block.controllerTick() >= 1) {
                spawn(block);
                block.setControllerTick(0);
                block.setPhase("监测敌人是否死亡");
            } else if (phase.equals("监测敌人是否死亡")) {
                List<Entity> entities = entities(block);
                long players = entities.stream().filter(ServerPlayer.class::isInstance).count();
                long enemies = entities.stream().filter(entity -> !(entity instanceof ServerPlayer) && DungeonCreatures.isDungeonEntity(entity)).count();
                if (players <= 0) {
                    block.setPhase("失败");
                    block.setControllerTick(0);
                } else if (enemies <= 0) {
                    block.setPhase(block.waveIndex() >= block.waveMax() ? "成功" : "下一波");
                }
            } else if (phase.equals("下一波")) {
                block.setControllerTick(0);
                block.setWaveIndex(block.waveIndex() + 1);
                block.setWaveEntityCount(block.waveCount(block.waveIndex()));
                block.setPhase("释放敌人");
            } else if (phase.equals("失败")) {
                block.setControllerTick(0);
                block.setEndTime(0);
                block.setActive(false);
                block.setPhase("");
                for (Entity entity : entities(block)) if (DungeonCreatures.isDungeonEntity(entity)) entity.discard();
                for (ServerPlayer player : players(block)) {
                    DungeonTitles.result(player, false);
                    DungeonTransientData transientState = player.getData(DungeonData.PLAYER_TRANSIENT);
                    if (player.gameMode.getGameModeForPlayer() == GameType.ADVENTURE && !transientState.previousGameMode().equals("冒险")) {
                        player.setGameMode(GameType.SURVIVAL);
                    }
                    player.setData(DungeonData.PLAYER_TRANSIENT, transientState.withPendingReturn(true));
                    if (!Relations.entityTemporaryTeam(player).isEmpty()) Relations.setEntityTemporaryTeam(player, "");
                    RETURNS.add(new ReturnTask(player, block, 2));
                }
            } else if (phase.equals("成功")) {
                block.setControllerTick(0);
                for (ServerPlayer player : players(block)) {
                    DungeonTitles.result(player, true);
                    player.setData(DungeonData.PLAYER_TRANSIENT, player.getData(DungeonData.PLAYER_TRANSIENT).withPendingReturn(true));
                    if (!Relations.entityTemporaryTeam(player).isEmpty()) Relations.setEntityTemporaryTeam(player, "");
                }
                give(block);
                block.setEndTime(11);
                block.setClosing(true);
                block.setActive(false);
                block.setPhase("");
            }
            block.setEndTime(block.endTime() - 1);
            for (ServerPlayer player : players(block)) player.displayClientMessage(Component.literal("倒计时：" + block.endTime() + "秒"), true);
            if (block.endTime() <= 0) {
                block.setPhase("失败");
                block.setEndTime(0);
            }
        } else if (block.closing()) {
            block.setEndTime(block.endTime() - 1);
            for (ServerPlayer player : players(block)) player.displayClientMessage(Component.literal(block.endTime() + "秒后退出副本"), true);
            if (block.endTime() <= 0) {
                block.setEndTime(0);
                block.setClosing(false);
                for (ServerPlayer player : players(block)) {
                    if (DungeonPlayers.returnFromController(player, block)) return;
                }
            }
        }
        block.changed();
    }

    private static void spawn(DungeonBlockEntity block) {
        ServerLevel level = (ServerLevel) block.getLevel();
        int spawn = block.spawnCursor();
        int count = block.waveCount(block.waveIndex());
        List<ControllerMobSpawn> entries = block.mobSpawns();
        Entity first = null;
        for (int index = 0; index < count && spawn < entries.size(); index++) {
            ControllerMobSpawn entry = entries.get(spawn++);
            BlockPos pos = block.getBlockPos().offset(entry.x(), entry.y(), entry.z());
            Entity entity = BuiltInRegistries.ENTITY_TYPE.get(entry.entityId()).create(level);
            if (entity == null) continue;
            float angle = entry.facing();
            entity.moveTo(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, level.random.nextFloat() * 360, 0);
            entity.setYRot(angle);
            entity.setXRot(0);
            entity.setYBodyRot(angle);
            entity.setYHeadRot(angle);
            entity.yRotO = angle;
            entity.xRotO = 0;
            if (entity instanceof LivingEntity living) { living.yBodyRotO = angle; living.yHeadRotO = angle; }
            DungeonCreatures.setDungeonEntity(entity, true);
            entity.setData(DungeonData.CONTROLLER_POS, block.getBlockPos());
            if (first == null) {
                first = entity;
                Relations.addFriendlyTeam(entity, "isfubenentity");
            } else Relations.shareEntityAllegiance(first, entity);
            Relations.setTeam(entity, "isfubenentity");
            Vec3 center = Vec3.atLowerCornerOf(pos);
            String attacker = level.getEntitiesOfClass(ServerPlayer.class, new AABB(center, center).inflate(12), Entity::isAlive).stream()
                    .sorted(Comparator.comparingDouble(player -> player.distanceToSqr(center))).map(Entity::getStringUUID).findFirst().orElse("");
            DungeonCreatures.prepare(entity, entry.level(), attacker);
            if (entity instanceof Mob mob) mob.finalizeSpawn(level, level.getCurrentDifficultyAt(entity.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
            level.addFreshEntity(entity);
        }
        block.setSpawnCursor(spawn);
    }

    private static void give(DungeonBlockEntity block) {
        ServerLevel level = (ServerLevel) block.getLevel();
        for (ServerPlayer player : players(block)) {
            for (int slot = 0; slot < 18; slot++) {
                ItemStack reward = block.getItem(slot);
                if (!reward.isEmpty() && Mth.nextInt(level.random, 1, 100) <= block.rewardWeight(slot)) ItemHandlerHelper.giveItemToPlayer(player, reward.copy());
            }
        }
    }

    private static List<Entity> entities(DungeonBlockEntity block) {
        Vec3 center = Vec3.atLowerCornerOf(block.getBlockPos()).add(block.centerOffsetX(), block.centerOffsetY(), block.centerOffsetZ());
        return block.getLevel().getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(block.sideLength() / 2.0), entity -> true).stream()
                .sorted(Comparator.comparingDouble(entity -> entity.distanceToSqr(center))).toList();
    }

    private static List<ServerPlayer> players(DungeonBlockEntity block) {
        return entities(block).stream().filter(ServerPlayer.class::isInstance).map(ServerPlayer.class::cast).toList();
    }

    public static void endTick(ServerTickEvent.Post event) {
        for (int index = RETURNS.size() - 1; index >= 0; index--) {
            ReturnTask task = RETURNS.get(index);
            if (--task.remaining <= 0) {
                if (!DungeonPlayers.returnFromController(task.player, task.block)) RETURNS.remove(index);
            }
        }
    }

    public static void clearDelayedReturns() { RETURNS.clear(); }

    private static final class ReturnTask {
        private final ServerPlayer player;
        private final DungeonBlockEntity block;
        private int remaining;
        private ReturnTask(ServerPlayer player, DungeonBlockEntity block, int remaining) {
            this.player = player; this.block = block; this.remaining = remaining;
        }
    }
}
