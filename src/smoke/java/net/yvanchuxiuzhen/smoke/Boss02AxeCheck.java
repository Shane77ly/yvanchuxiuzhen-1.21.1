package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Pig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.yvanchuxiuzhen.system.creature.CreatureSetup;
import net.yvanchuxiuzhen.system.creature.entity.boss.Boss0201Entity;
import net.yvanchuxiuzhen.system.creature.entity.boss.Boss02Entity;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 02号首领预置飞斧候选后的原生阶段、子实体与死亡刻。 */
final class Boss02AxeCheck extends SystemCheck {
    private final List<Boss0201Entity> axes = new ArrayList<>();
    private final Consumer<EntityJoinLevelEvent> joinListener = this::onJoin;
    private final Consumer<EntityTickEvent.Post> tickListener = this::onTick;
    private final Consumer<LivingDeathEvent> deathListener = this::onDeath;
    private ServerLevel level;
    private Difficulty difficulty;
    private Boss02Entity boss;
    private Pig target;
    private int skillFrames;
    private int bossDeaths;
    private int axeDeaths;
    private String prepared;
    private String thrown;
    private String reset;
    private boolean axeNineteen;
    private boolean deathSeventyNine;
    private boolean listening;

    Boss02AxeCheck(List<String> report, String levelId) {
        super("02号首领飞斧与死亡刻", report, levelId);
    }

    @Override
    protected boolean step(int step, int local) {
        try {
            return switch (step) {
                case 0 -> { onServer(this::prepare); yield true; }
                case 1 -> {
                    boolean[] ready = {false};
                    onServer(player -> ready[0] = finishFlight());
                    if (!ready[0] && local > 200) throw new IllegalStateException("飞斧没有按时退场");
                    yield ready[0];
                }
                case 2 -> {
                    boolean[] ready = {false};
                    onServer(player -> {
                        if (!boss.isRemoved()) return;
                        expectTrue("首领第79死亡刻仍在，第80刻KILLED退场",
                                deathSeventyNine && boss.deathTime == 80 && boss.getRemovalReason() == Entity.RemovalReason.KILLED);
                        expect("原生死亡事件只发生一次", 1, bossDeaths);
                        cleanup(player);
                        ready[0] = true;
                    });
                    if (!ready[0] && local > 240) throw new IllegalStateException("首领死亡退场超时");
                    if (ready[0]) finish();
                    yield ready[0];
                }
                default -> throw new IllegalStateException("未定义的首领检查步骤 " + step);
            };
        } catch (RuntimeException error) {
            fail(error.getMessage());
            onServer(this::cleanup);
            finish();
            return false;
        }
    }

    private void prepare(ServerPlayer player) {
        level = player.serverLevel();
        difficulty = level.getDifficulty();
        player.server.setDifficulty(Difficulty.NORMAL, true);
        boss = CreatureSetup.<Boss02Entity>entityType("ycxzboss_02").get().create(level);
        target = EntityType.PIG.create(level);
        if (boss == null || target == null) throw new IllegalStateException("无法创建战斗样本");
        boss.moveTo(player.position().add(3, 12, 0));
        boss.finalizeSpawn(level, level.getCurrentDifficultyAt(boss.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
        target.moveTo(boss.position().add(7, 0, 0));
        target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1024);
        target.setHealth(1024);
        anchor(boss);
        anchor(target);
        Relations.addFriendlyTeam(boss, "smoke_boss02_ally");
        boss.getPersistentData().putDouble("attack_Num", 10);
        boss.getPersistentData().putString("jineng", "飞斧");
        NeoForge.EVENT_BUS.addListener(joinListener);
        NeoForge.EVENT_BUS.addListener(tickListener);
        NeoForge.EVENT_BUS.addListener(deathListener);
        listening = true;
        if (!level.addFreshEntity(target) || !level.addFreshEntity(boss)) throw new IllegalStateException("战斗样本未入场");
        boss.setTarget(target);
    }

    private static void anchor(Mob mob) {
        mob.setNoAi(true);
        mob.setNoGravity(true);
    }

    private void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel() != level || !(event.getEntity() instanceof Boss0201Entity axe)) return;
        CompoundTag data = axe.getPersistentData();
        if (!data.getString("AttackUUID").equals(boss.getStringUUID())) return;
        axes.add(axe);
        expect("飞斧由首领技能第8刻原生生成", 8, boss.getPersistentData().getDouble("entitytick"));
        expect("飞斧攻击值", 12, data.getDouble("AttackDamage"));
        expect("飞斧模式", "飞斧", data.getString("jinengname"));
        expect("飞斧继承团队", Relations.entityTeam(boss), Relations.entityTeam(axe));
        expect("飞斧继承友方表", Relations.friendlyTeams(boss), Relations.friendlyTeams(axe));
        expectTrue("来源出生队名保持在飞斧友方表，首领不是命中目标", Relations.isFriendly(axe, boss));
        float yaw = boss.getYRot();
        expectTrue("飞斧入场首帧头身朝向一致", axe.getYRot() == yaw && axe.getXRot() == 0
                && axe.yBodyRot == yaw && axe.getYHeadRot() == yaw && axe.yRotO == yaw && axe.xRotO == 0
                && axe.yBodyRotO == yaw && axe.yHeadRotO == yaw);
    }

    private void onTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (entity == boss) {
            if (boss.deathTime == 79 && !boss.isRemoved()) deathSeventyNine = true;
            if (skillFrames >= 20) return;
            skillFrames++;
            CompoundTag data = boss.getPersistentData();
            if (skillFrames == 1) prepared = phase(data);
            if (skillFrames == 8) thrown = phase(data);
            if (skillFrames == 20) reset = phase(data);
        } else if (axes.contains(entity) && entity.getPersistentData().getDouble("jinengtick") == 19 && !entity.isRemoved()) {
            axeNineteen = true;
        }
    }

    private void onDeath(LivingDeathEvent event) {
        if (event.getEntity() == boss) bossDeaths++;
        if (axes.contains(event.getEntity())) axeDeaths++;
    }

    private String phase(CompoundTag data) {
        return data.getString("jineng") + "/" + data.getString("jinengjinxing") + "/" + data.getDouble("entitytick")
                + "/" + data.getBoolean("donghua") + "/" + axes.size() + "/" + target.isAlive();
    }

    private boolean finishFlight() {
        if (axes.isEmpty() || !axes.getFirst().isRemoved()) return false;
        Boss0201Entity axe = axes.getFirst();
        expect("首领第1刻进入准备", "飞斧/准备/1.0/true/0/true", prepared);
        expect("首领第8刻完成飞掷", "飞斧/飞掷/8.0/true/1/true", thrown);
        expect("首领第20刻清空技能与计时", "//0.0/false/1/true", reset);
        expect("单次只生成一柄飞斧", 1, axes.size());
        expectTrue("飞斧第19刻仍在、第20刻非死亡退场", axeNineteen
                && axe.getPersistentData().getDouble("jinengtick") == 20 && axe.getRemovalReason() == Entity.RemovalReason.DISCARDED);
        expect("飞斧退场不发死亡事件", 0, axeDeaths);
        expectTrue("首领走原生致死伤害入口",
                boss.hurt(boss.damageSources().genericKill(), Float.MAX_VALUE) && boss.isDeadOrDying());
        return true;
    }

    private void cleanup(ServerPlayer player) {
        if (listening) {
            NeoForge.EVENT_BUS.unregister(joinListener);
            NeoForge.EVENT_BUS.unregister(tickListener);
            NeoForge.EVENT_BUS.unregister(deathListener);
            listening = false;
        }
        axes.forEach(Entity::discard);
        if (target != null) target.discard();
        if (boss != null) boss.discard();
        if (difficulty != null) player.server.setDifficulty(difficulty, true);
    }
}
