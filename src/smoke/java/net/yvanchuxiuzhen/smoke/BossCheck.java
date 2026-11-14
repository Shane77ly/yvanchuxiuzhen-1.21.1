package net.yvanchuxiuzhen.smoke;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.yvanchuxiuzhen.system.fulu.api.NpcTalismans;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.skill.api.SkillSummons;

/** 首领实体注册、客户端渲染映射与存档重进检查。 */
final class BossCheck extends SystemCheck {
    private static final String MOD = "yvanchuxiuzhen";
    private static final List<String> IDS = List.of("ycxzboss_01", "ycxzboss_02", "ycxzboss_0201", "ycxzboss_04");
    private static final List<String> PERSISTENT_IDS = List.of("ycxzboss_01", "ycxzboss_02", "ycxzboss_04");
    private final Screenshots screenshots;
    private final List<String> report;
    private final Consumer<LivingIncomingDamageEvent> damageListener = this::recordDamage;
    private final Consumer<LivingDeathEvent> deathListener = this::recordDeath;
    private final Map<String, UUID> entities = new LinkedHashMap<>();
    private final Map<String, Mob> spawned = new LinkedHashMap<>();
    private int executed = -1;
    private BlockPos floor;
    private Difficulty originalDifficulty;
    private GameType originalMode;
    private Vec3 originalPosition;
    private float originalYaw;
    private float originalPitch;

    BossCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("妖兽首领", report, levelId);
        this.screenshots = screenshots;
        this.report = report;
    }

    @Override
    protected boolean step(int step, int local) {
        return switch (step) {
            case 0 -> action(step, local, 8, this::prepare);
            case 1 -> actionAt(step, local, 1, this::launchProjectiles);
            case 2 -> actionAt(step, local, 1, this::checkProjectiles);
            case 3 -> local >= 20;
            case 4 -> checkBeforeSave(step, local);
            case 5 -> { leaveWorld(); yield true; }
            case 6 -> reopened(local);
            case 7 -> inWorld(local);
            case 8 -> action(step, local, 5, this::checkAfterSave);
            case 9 -> { onServer(this::cleanup); finish(); yield true; }
            default -> throw new IllegalStateException("未知首领检查步骤 " + step);
        };
    }

    private boolean action(int step, int local, int wait, Consumer<ServerPlayer> action) {
        if (executed != step) {
            executed = step;
            onServer(action);
        }
        return local >= wait;
    }

    private boolean actionAt(int step, int local, int wait, Consumer<ServerPlayer> action) {
        if (local < wait) return false;
        if (executed != step) {
            executed = step;
            onServer(action);
        }
        return true;
    }

    private void prepare(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        originalDifficulty = level.getDifficulty();
        originalMode = player.gameMode.getGameModeForPlayer();
        originalPosition = player.position();
        originalYaw = player.getYRot();
        originalPitch = player.getXRot();
        NeoForge.EVENT_BUS.addListener(damageListener);
        NeoForge.EVENT_BUS.addListener(deathListener);
        player.server.setDifficulty(Difficulty.NORMAL, true);
        floor = player.blockPosition().above(16);
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -1; dz <= 20; dz++) {
                level.setBlock(floor.offset(dx, -1, dz), Blocks.STONE.defaultBlockState(), 2);
                for (int dy = 0; dy <= 7; dy++) {
                    level.setBlock(floor.offset(dx, dy, dz), Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
        player.teleportTo(level, floor.getX() + 0.5, floor.getY(), floor.getZ() + 0.5, 0, 0);
        for (String id : IDS) {
            ResourceLocation key = ResourceLocation.fromNamespaceAndPath(MOD, id);
            var type = BuiltInRegistries.ENTITY_TYPE.get(key);
            expect("实体注册ID " + id, key, BuiltInRegistries.ENTITY_TYPE.getKey(type));
            Entity probe = type.create(level);
            expectTrue("实体构造器可创建 " + id, probe != null);
        }
        for (String id : List.of("ycxzboss_01_spawn_egg", "ycxzboss_02_spawn_egg", "ycxzboss_04_spawn_egg")) {
            ResourceLocation key = ResourceLocation.fromNamespaceAndPath(MOD, id);
            expectTrue("刷怪蛋物品已登记 " + id, BuiltInRegistries.ITEM.containsKey(key)
                    && !BuiltInRegistries.ITEM.get(key).equals(Items.AIR));
        }
        for (int index = 0; index < PERSISTENT_IDS.size(); index++) {
            String id = PERSISTENT_IDS.get(index);
            ResourceLocation key = ResourceLocation.fromNamespaceAndPath(MOD, id);
            Entity created = BuiltInRegistries.ENTITY_TYPE.get(key).create(level);
            if (!(created instanceof Mob mob)) {
                throw new IllegalStateException("首领实体未实现Mob: " + id);
            }
            BlockPos spawn = floor.offset(0, 1, 4 + index * 6);
            mob.moveTo(spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5, 0, 0);
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(spawn), MobSpawnType.MOB_SUMMONED, null);
            mob.setNoAi(true);
            expectTrue("首领实体可加入世界 " + id, level.addFreshEntity(mob));
            entities.put(id, mob.getUUID());
            spawned.put(id, mob);
        }
    }

    private boolean checkBeforeSave(int step, int local) {
        if (local < 20) return false;
        if (executed != step) {
            executed = step;
            onServer(this::verifyServerBeforeSave);
            verifyClientBeforeSave();
            screenshots.take("r07_boss_01.png");
        }
        return local >= 35;
    }

    private void launchProjectiles(ServerPlayer player) {
        Entity owner = player.serverLevel().getEntity(entities.get("ycxzboss_02"));
        owner.setYRot(90);
        owner.setXRot(0);
        owner.getPersistentData().putDouble("fulu45level", 0);
        owner.getPersistentData().putDouble("mana_Att", 3);
        owner.getPersistentData().putDouble("fululevel46", 2);
        NpcTalismans.shootBossCannon(player.serverLevel(), owner);
        NpcTalismans.shootBossFireball(player.serverLevel(), owner);
        SkillSummons.shootBossFireball(player.serverLevel(), owner);
    }

    private void checkProjectiles(ServerPlayer player) {
        Entity owner = player.serverLevel().getEntity(entities.get("ycxzboss_02"));
        Map<String, Entity> projectiles = new LinkedHashMap<>();
        for (Entity entity : player.serverLevel().getEntitiesOfClass(Entity.class, owner.getBoundingBox().inflate(20),
                candidate -> candidate instanceof Projectile projectile && projectile.getOwner() == owner)) {
            projectiles.put(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath(), entity);
        }
        String uuid = owner.getStringUUID();
        Entity cannon = projectiles.get("projectile_fulutexiao_45");
        Entity talismanFireball = projectiles.get("projectile_fulutexiao_46");
        Entity skillFireball = projectiles.get("projectile_skill_fire_01");
        expectTrue("首领公开入口生成45号飞雷弹体", cannon != null);
        expectTrue("45号弹体首刻记录实体主人", cannon != null
                && cannon.getPersistentData().getString("belongToMasterUUID").equals(uuid));
        expectTrue("首领公开入口生成46号火球弹体", talismanFireball != null);
        expectTrue("46号弹体首刻记录攻击者", talismanFireball != null
                && talismanFireball.getPersistentData().getString("AttackUUID").equals(uuid));
        expectTrue("技能系统首领火球入口生成独立技能弹体", skillFireball != null);
        expectTrue("46号弹体继承首领实体团队", talismanFireball != null
                && Relations.entityTeam(talismanFireball).equals(Relations.entityTeam(owner)));
        projectiles.values().forEach(Entity::discard);
    }

    private void verifyServerBeforeSave(ServerPlayer player) {
        for (String id : PERSISTENT_IDS) {
            Entity entity = player.serverLevel().getEntity(entities.get(id));
            expectTrue("首领运行期存在且有生命值 " + id + " " + describe(entity), entity instanceof Mob mob && mob.isAlive()
                    && mob.getMaxHealth() > 0);
        }
    }

    private void verifyClientBeforeSave() {
        for (String id : PERSISTENT_IDS) {
            expectTrue("中文名称已登记 " + id, I18n.exists("entity." + MOD + "." + id));
            Entity clientEntity = findClientEntity(entities.get(id));
            expectTrue("首领同步到客户端 " + id, clientEntity != null);
            if (clientEntity != null) {
                expectTrue("客户端渲染器已登记 " + id, minecraft.getEntityRenderDispatcher().getRenderer(clientEntity) != null);
            }
        }
        Entity axeProbe = BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.fromNamespaceAndPath(MOD, "ycxzboss_0201"))
                .create(minecraft.level);
        expectTrue("飞斧客户端渲染器已登记", axeProbe != null
                && minecraft.getEntityRenderDispatcher().getRenderer(axeProbe) != null);
    }

    private Entity findClientEntity(UUID uuid) {
        for (Entity entity : minecraft.level.entitiesForRendering()) {
            if (entity.getUUID().equals(uuid)) return entity;
        }
        return null;
    }

    private void checkAfterSave(ServerPlayer player) {
        for (String id : PERSISTENT_IDS) {
            Entity entity = player.serverLevel().getEntity(entities.get(id));
            expectTrue("真实存档重进保留首领 " + id + " 难度=" + player.serverLevel().getDifficulty()
                    + " 当前=" + describe(entity) + " 保存前实例=" + describe(spawned.get(id)),
                    entity instanceof Mob mob && mob.isAlive());
        }
    }

    private String describe(Entity entity) {
        if (entity == null) return "未加载";
        return entity.getUUID() + " 位置=" + entity.position() + " 移除=" + entity.getRemovalReason()
                + (entity instanceof Mob mob ? " 生命=" + mob.getHealth() + "/" + mob.getMaxHealth() : "");
    }

    private void recordDamage(LivingIncomingDamageEvent event) {
        if (entities.containsValue(event.getEntity().getUUID())) {
            report.add("INFO 妖兽首领 受伤诊断 目标=" + describe(event.getEntity())
                    + " 类型=" + event.getSource().getMsgId() + " 原始伤害=" + event.getOriginalAmount()
                    + " 伤害=" + event.getAmount() + " 攻击者=" + describe(event.getSource().getEntity())
                    + " 直接来源=" + describe(event.getSource().getDirectEntity()));
        }
    }

    private void recordDeath(LivingDeathEvent event) {
        if (entities.containsValue(event.getEntity().getUUID())) {
            report.add("INFO 妖兽首领 死亡诊断 目标=" + describe(event.getEntity())
                    + " 类型=" + event.getSource().getMsgId() + " 攻击者=" + describe(event.getSource().getEntity())
                    + " 直接来源=" + describe(event.getSource().getDirectEntity()));
        }
    }

    private void cleanup(ServerPlayer player) {
        NeoForge.EVENT_BUS.unregister(damageListener);
        NeoForge.EVENT_BUS.unregister(deathListener);
        for (UUID uuid : entities.values()) {
            Entity entity = player.serverLevel().getEntity(uuid);
            if (entity != null) entity.discard();
        }
        player.server.setDifficulty(originalDifficulty, true);
        player.setGameMode(originalMode);
        player.teleportTo(player.serverLevel(), originalPosition.x, originalPosition.y, originalPosition.z, originalYaw, originalPitch);
    }
}
