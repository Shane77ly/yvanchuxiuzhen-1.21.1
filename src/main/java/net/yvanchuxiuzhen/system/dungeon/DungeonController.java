package net.yvanchuxiuzhen.system.dungeon;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
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
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 控制器每秒推进来源阶段；胜利、失败、超时和延后回程各自保留原顺序。 */
public final class DungeonController {
    private static final List<ReturnTask> RETURNS = new ArrayList<>();

    private DungeonController() {
    }

    public static void tick(DungeonBlockEntity block) {
        if (!block.kind().equals("fubenblock")) return;
        CompoundTag data = block.getPersistentData();
        if (data.getBoolean("fubenopen")) {
            DungeonDefinitions.reset(block);
            DungeonDefinitions.parse(block);
            data.putString("fubenjindu", "准备");
            data.putBoolean("fubenstate", true);
            data.putBoolean("fubenopen", false);
            data.putInt("fubentick", 0);
        } else if (data.getBoolean("fubenstate")) {
            data.putInt("fubentick", data.getInt("fubentick") + 1);
            String phase = data.getString("fubenjindu");
            if (phase.equals("准备") && data.getInt("fubentick") >= 7) {
                for (ServerPlayer player : players(block)) title(player, data.getString("fubenname"), "black", false);
                data.putInt("fubentick", 0);
                data.putInt("fubenbocinum", 1);
                data.putInt("fubenentitynum", data.getInt("fybenboci1num"));
                data.putString("fubenjindu", "释放敌人");
            } else if (phase.equals("释放敌人") && data.getInt("fubentick") >= 1) {
                spawn(block);
                data.putInt("fubentick", 0);
                data.putString("fubenjindu", "监测敌人是否死亡");
            } else if (phase.equals("监测敌人是否死亡")) {
                List<Entity> entities = entities(block);
                long players = entities.stream().filter(ServerPlayer.class::isInstance).count();
                long enemies = entities.stream().filter(entity -> !(entity instanceof ServerPlayer) && entity.getPersistentData().getBoolean("fubenentity")).count();
                if (players <= 0) {
                    data.putString("fubenjindu", "失败");
                    data.putInt("fubentick", 0);
                } else if (enemies <= 0) {
                    data.putString("fubenjindu", data.getInt("fubenbocinum") >= data.getInt("fubenbociMax") ? "成功" : "下一波");
                }
            } else if (phase.equals("下一波")) {
                data.putInt("fubentick", 0);
                data.putInt("fubenbocinum", data.getInt("fubenbocinum") + 1);
                data.putInt("fubenentitynum", data.getInt("fybenboci" + data.getInt("fubenbocinum") + "num"));
                data.putString("fubenjindu", "释放敌人");
            } else if (phase.equals("失败")) {
                data.putInt("fubentick", 0);
                data.putInt("fubenjieshutime", 0);
                data.putBoolean("fubenstate", false);
                data.putString("fubenjindu", "");
                for (Entity entity : entities(block)) if (entity.getPersistentData().getBoolean("fubenentity")) entity.discard();
                for (ServerPlayer player : players(block)) {
                    title(player, "失 败", "black", true);
                    if (player.gameMode.getGameModeForPlayer() == GameType.ADVENTURE && !player.getPersistentData().getString("gamemoshi").equals("冒险")) player.setGameMode(GameType.SURVIVAL);
                    player.getPersistentData().putBoolean("fubenfanhui", true);
                    if (!Relations.entityTemporaryTeam(player).isEmpty()) Relations.setEntityTemporaryTeam(player, "");
                    RETURNS.add(new ReturnTask(player, block, 2));
                }
            } else if (phase.equals("成功")) {
                data.putInt("fubentick", 0);
                for (ServerPlayer player : players(block)) {
                    title(player, "通 关", "gold", true);
                    player.getPersistentData().putBoolean("fubenfanhui", true);
                    if (!Relations.entityTemporaryTeam(player).isEmpty()) Relations.setEntityTemporaryTeam(player, "");
                }
                give(block);
                data.putInt("fubenjieshutime", 11);
                data.putBoolean("fubenclose", true);
                data.putBoolean("fubenstate", false);
                data.putString("fubenjindu", "");
            }
            data.putInt("fubenjieshutime", data.getInt("fubenjieshutime") - 1);
            for (ServerPlayer player : players(block)) player.displayClientMessage(Component.literal("倒计时：" + data.getInt("fubenjieshutime") + "秒"), true);
            if (data.getInt("fubenjieshutime") <= 0) {
                data.putString("fubenjindu", "失败");
                data.putInt("fubenjieshutime", 0);
            }
        } else if (data.getBoolean("fubenclose")) {
            data.putInt("fubenjieshutime", data.getInt("fubenjieshutime") - 1);
            for (ServerPlayer player : players(block)) player.displayClientMessage(Component.literal(data.getInt("fubenjieshutime") + "秒后退出副本"), true);
            if (data.getInt("fubenjieshutime") <= 0) {
                data.putInt("fubenjieshutime", 0);
                data.putBoolean("fubenclose", false);
                for (ServerPlayer player : players(block)) {
                    if (DungeonPlayers.returnFromController(player, block)) return;
                }
            }
        }
        block.changed();
    }

    private static void spawn(DungeonBlockEntity block) {
        ServerLevel level = (ServerLevel) block.getLevel();
        CompoundTag data = block.getPersistentData();
        int spawn = data.getInt("fubenentityspawnnum");
        int count = data.getInt("fybenboci" + data.getInt("fubenbocinum") + "num");
        Entity first = null;
        for (int index = 0; index < count; index++) {
            CompoundTag tag = data.getCompound("mobtagdata" + spawn++);
            BlockPos pos = block.getBlockPos().offset(tag.getInt("entityshengchengX"), tag.getInt("entityshengchengY"), tag.getInt("entityshengchengZ"));
            Entity entity = BuiltInRegistries.ENTITY_TYPE.get(DungeonRewards.id(tag.getString("entityshengchengid"))).create(level);
            if (entity == null) continue;
            float angle = tag.getInt("entityshengchengchaoxiang");
            entity.moveTo(pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, level.random.nextFloat() * 360, 0);
            entity.setYRot(angle);
            entity.setXRot(0);
            entity.setYBodyRot(angle);
            entity.setYHeadRot(angle);
            entity.yRotO = angle;
            entity.xRotO = 0;
            if (entity instanceof LivingEntity living) { living.yBodyRotO = angle; living.yHeadRotO = angle; }
            CompoundTag state = entity.getPersistentData();
            state.putBoolean("fubenentity", true);
            state.putInt("fubenzuobiaoX", block.getBlockPos().getX());
            state.putInt("fubenzuobiaoY", block.getBlockPos().getY());
            state.putInt("fubenzuobiaoZ", block.getBlockPos().getZ());
            if (first == null) {
                first = entity;
                Relations.addFriendlyTeam(entity, "isfubenentity");
            } else Relations.shareEntityAllegiance(first, entity);
            Relations.setTeam(entity, "isfubenentity");
            Vec3 center = Vec3.atLowerCornerOf(pos);
            String attacker = level.getEntitiesOfClass(ServerPlayer.class, new AABB(center, center).inflate(12), Entity::isAlive).stream()
                    .sorted(Comparator.comparingDouble(player -> player.distanceToSqr(center))).map(Entity::getStringUUID).findFirst().orElse("");
            DungeonCreatures.prepare(entity, tag.getInt("entityshengchenglevel"), attacker);
            if (entity instanceof Mob mob) mob.finalizeSpawn(level, level.getCurrentDifficultyAt(entity.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
            level.addFreshEntity(entity);
        }
        data.putInt("fubenentityspawnnum", spawn);
    }

    private static void give(DungeonBlockEntity block) {
        ServerLevel level = (ServerLevel) block.getLevel();
        for (ServerPlayer player : players(block)) {
            for (int slot = 0; slot < 18; slot++) {
                ItemStack reward = block.getItem(slot);
                if (!reward.isEmpty() && Mth.nextInt(level.random, 1, 100) <= block.getPersistentData().getInt("gift" + slot + "qvanzhong")) ItemHandlerHelper.giveItemToPlayer(player, reward.copy());
            }
        }
    }

    private static List<Entity> entities(DungeonBlockEntity block) {
        CompoundTag data = block.getPersistentData();
        Vec3 center = Vec3.atLowerCornerOf(block.getBlockPos()).add(data.getInt("fubenzhongxinX"), data.getInt("fubenzhongxinY"), data.getInt("fubenzhongxinZ"));
        return block.getLevel().getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(data.getInt("fubenbianchang") / 2.0), entity -> true).stream()
                .sorted(Comparator.comparingDouble(entity -> entity.distanceToSqr(center))).toList();
    }

    private static List<ServerPlayer> players(DungeonBlockEntity block) {
        return entities(block).stream().filter(ServerPlayer.class::isInstance).map(ServerPlayer.class::cast).toList();
    }

    private static void title(ServerPlayer player, String text, String color, boolean bold) {
        player.server.getCommands().performPrefixedCommand(player.createCommandSourceStack().withSuppressedOutput().withPermission(4),
                "title @p title [{\"text\":\"" + text + "\",\"color\":\"" + color + "\",\"bold\":" + bold + ",\"italic\":false,\"underlined\":false,\"strikethrough\":false,\"obfuscated\":false}]");
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
