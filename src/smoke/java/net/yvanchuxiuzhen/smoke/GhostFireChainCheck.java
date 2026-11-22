package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.yvanchuxiuzhen.system.creature.CreatureSetup;
import net.yvanchuxiuzhen.system.creature.entity.Guiguai02Entity;
import net.yvanchuxiuzhen.system.creature.entity.Guiguai03Entity;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;
import net.yvanchuxiuzhen.system.xiulian.api.MainTechnique;

/** 独立开发档：只预置03已选招；计数、02选敌、施伤及死亡奖励由生产代码推进。 */
final class GhostFireChainCheck extends SystemCheck {
    private static final String SCORE = "zhaohuanwu";
    private static final ResourceKey<DamageType> SPELL = ResourceKey.create(Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", "linglidamage_player"));
    private final List<Entity> fixtures = new ArrayList<>();
    private final List<Guiguai02Entity> children = new ArrayList<>();
    private final List<Integer> birthTicks = new ArrayList<>();
    private ServerPlayer player;
    private ServerLevel level;
    private Difficulty difficulty;
    private Guiguai03Entity king;
    private Mob bait, enemy;
    private Guiguai02Entity idle, blast;
    private Vec3 arena;
    private boolean listening, inCast, castDone, blastPlaced, locked, primed, attacking;
    private int idleTicks, noTargetSamples, hits, enemyDeaths, blastDeaths, idleDeaths, playerKills;
    private double standBeforeBlast, meritsBeforeBlast;

    GhostFireChainCheck(List<String> report, String levelId) {
        super("小尸王与鬼火的两种结局", report, levelId);
    }

    @Override
    protected boolean step(int step, int local) {
        onServer(p -> {
            try {
                if (step == 0) prepare(p);
                else if (!attacking && castDone && idleTicks >= 101 && blast != null && blast.isRemoved()) {
                    verify();
                } else if (attacking && idle.isRemoved()) {
                    expect("玩家击杀后的原生移除原因", Entity.RemovalReason.KILLED, idle.getRemovalReason());
                    expect("直到死亡清理结束的02死亡事件总数", 1, idleDeaths);
                    cleanup();
                } else if (local > 450) {
                    fail("等待超时：出生刻=" + birthTicks + "，收招=" + castDone + "，无目标样本刻=" + idleTicks
                            + "，锁敌=" + locked + "，准备=" + primed + "，命中/敌死=" + hits + "/" + enemyDeaths);
                    cleanup();
                }
            } catch (RuntimeException ex) {
                fail("步骤 " + step + "：" + ex);
                cleanup();
            }
        });
        return step == 0;
    }

    private void prepare(ServerPlayer p) {
        player = p;
        level = p.serverLevel();
        difficulty = level.getDifficulty();
        if (difficulty == Difficulty.PEACEFUL) p.server.setDifficulty(Difficulty.NORMAL, true);
        arena = Vec3.atBottomCenterOf(p.blockPosition().above(24));
        king = place(CreatureSetup.<Guiguai03Entity>entityType("guiguai_03").get().create(level), arena);
        bait = place(EntityType.PIG.create(level), arena.add(18, 0, 0));
        enemy = place(EntityType.PIG.create(level), arena.add(0, 0, 18));
        Relations.setTeam(bait, "ghost_check_cast_target");
        Relations.setTeam(enemy, "ghost_check_blast_target");
        // 预置03本轮选中的招式与目标；不预置施法刻、子实体或召唤分数。
        king.getPersistentData().putString("jineng", "召唤鬼火");
        king.getPersistentData().putString("gongjizhe", bait.getStringUUID());
        NeoForge.EVENT_BUS.register(this);
        listening = true;
    }

    private <T extends Mob> T place(T mob, Vec3 pos) {
        mob.moveTo(pos.x, pos.y, pos.z, 0, 0);
        if (!level.noCollision(mob, mob.getBoundingBox())) throw new IllegalStateException("夹具位置不空：" + pos);
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
        mob.setNoAi(true);
        mob.setNoGravity(true);
        if (!level.addFreshEntity(mob)) throw new IllegalStateException("夹具入场失败：" + mob.getType());
        fixtures.add(mob);
        return mob;
    }

    @SubscribeEvent
    public void join(EntityJoinLevelEvent event) {
        if (event.getLevel() != level || !inCast || !(event.getEntity() instanceof Guiguai02Entity ghost)) return;
        children.add(ghost);
        birthTicks.add(king.getPersistentData().getInt("entitytick"));
        var objective = level.getScoreboard().getObjective(SCORE);
        var score = objective == null ? null : level.getScoreboard().getPlayerScoreInfo(ghost, objective);
        expectTrue("出生 #" + children.size() + " 已标记召唤物且继承03目标", score != null && score.value() == 1
                && bait.getStringUUID().equals(ghost.getPersistentData().getString("gongjizhe")));
        ghost.setNoAi(true);
        ghost.setNoGravity(true);
        if (children.size() == 1) idle = ghost;
        if (children.size() == 2) blast = ghost;
    }

    @SubscribeEvent
    public void beforeTick(EntityTickEvent.Pre event) {
        Entity e = event.getEntity();
        if (e.level() != level) return;
        if (e == king) inCast = !castDone;
        if (!children.contains(e)) return;
        e.setDeltaMovement(Vec3.ZERO);
        // 首个实体刻前只隔离位置并撤掉继承的当前目标。后续不写目标、技能、阶段或寿命。
        if ((e == idle && idleTicks == 0) || (e == blast && !blastPlaced)) {
            Vec3 pos = e == idle ? arena.add(0, 18, 0) : enemy.position().add(1.2, 0, 0);
            e.setPos(pos.x, pos.y, pos.z);
            e.getPersistentData().remove("gongjizhe");
            if (e == blast) blastPlaced = true;
        }
        if (e == blast && "准备".equals(e.getPersistentData().getString("jinengjinxing"))) {
            // 预置可致死且不闪避的靶；紧贴爆发刻取基线，避开每秒吸收。
            enemy.setHealth(0.1F);
            enemy.getRandom().setSeed(1L);
            primed = true;
            standBeforeBlast = read(CultivationValue.EXPERIENCE_STAND);
            meritsBeforeBlast = read(CultivationValue.MERITS);
        }
    }

    @SubscribeEvent
    public void afterTick(EntityTickEvent.Post event) {
        Entity e = event.getEntity();
        if (e != king && e != idle && e != blast) return;
        var data = e.getPersistentData();
        if (e == king) {
            inCast = false;
            if (!castDone && data.getString("jineng").isEmpty()) {
                castDone = true;
                expect("收招刻", 36, king.tickCount);
            }
        } else if (e == idle && !attacking) {
            idleTicks++;
            if (idleTicks >= 99 && idleTicks <= 101) {
                boolean noTarget = data.getString("gongjizhe").isEmpty() && data.getString("linshigongjizhe").isEmpty();
                if (e.isAlive() && noTarget && data.getString("jinengjinxing").isEmpty()) noTargetSamples++;
                expect("无目标第" + idleTicks + "刻的召唤计时", idleTicks % 100, data.getDouble("xiaoshi"));
                if (idleTicks >= 100) expect("百刻只置普攻", "普攻", data.getString("jineng"));
            }
        } else if (e == blast && enemy.getStringUUID().equals(data.getString("gongjizhe"))
                && enemy.getStringUUID().equals(data.getString("linshigongjizhe"))) locked = true;
    }

    private boolean fromBlast(DamageSource source) {
        return source.is(SPELL) && source.getDirectEntity() == blast && source.getEntity() == blast;
    }

    @SubscribeEvent
    public void damage(LivingDamageEvent.Post event) {
        if (event.getEntity() == enemy && fromBlast(event.getSource()) && event.getNewDamage() > 0) hits++;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void death(LivingDeathEvent event) {
        if (event.getEntity().level() != level) return;
        if (event.getEntity() == enemy) {
            enemyDeaths++;
            expectTrue("敌人死亡的火伤直接来源及致因均为这只02", fromBlast(event.getSource()));
            expect("自爆击杀未给旁观玩家待吸收修为", standBeforeBlast, read(CultivationValue.EXPERIENCE_STAND));
            expect("自爆击杀未给旁观玩家功德", meritsBeforeBlast, read(CultivationValue.MERITS));
        }
        if (event.getEntity() == blast) blastDeaths++;
        if (event.getEntity() == idle) {
            idleDeaths++;
            if (attacking && event.getSource().getEntity() == player && event.getSource().getDirectEntity() == player) playerKills++;
        }
    }

    private void verify() {
        expect("03实际出生刻（各三只）", List.of(10, 10, 10, 17, 17, 17, 24, 24, 24), birthTicks);
        expect("不同实体数", 9, children.stream().map(Entity::getUUID).distinct().count());
        expect("无目标第99、100、101刻均在场、未准备", 3, noTargetSamples);
        expectTrue("02自主锁敌并进入准备", locked && primed);
        expect("归因正确的实际伤害次数", 1, hits);
        expect("目标死亡次数", 1, enemyDeaths);
        expect("自爆鬼火移除原因", Entity.RemovalReason.DISCARDED, blast.getRemovalReason());
        expect("自爆鬼火自身死亡次数", 0, blastDeaths);
        expect("主动击杀之前无目标鬼火死亡次数", 0, idleDeaths);
        if (!idle.isAlive()) throw new IllegalStateException("百刻存活样本已失效");
        playerKill();
    }

    private void playerKill() {
        // 同一次服务端任务内布置残血、空手、无功法加成，原生近战一次，立即读真实差值再还原。
        var technique = Cultivation.mainTechnique(player);
        var held = player.getMainHandItem();
        double stand = read(CultivationValue.EXPERIENCE_STAND), merits = read(CultivationValue.MERITS);
        double vigorous = read(CultivationValue.VIGOROUS);
        try {
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            Cultivation.setMainTechnique(player, MainTechnique.NONE);
            idle.setPos(player.getX(), player.getY(), player.getZ() + 1.2);
            idle.setHealth(0.1F);
            expect("受击鬼火的出生等级", 1, idle.getPersistentData().getDouble("LEVELnumber"));
            attacking = true;
            player.attack(idle);
            expectTrue("原生玩家近战实际致死", idle.isDeadOrDying());
            expect("鬼火服务端死亡事件总数", 1, idleDeaths);
            expect("玩家直接归因的死亡事件数", 1, playerKills);
            expect("主动击杀待吸收修为增量", 10, read(CultivationValue.EXPERIENCE_STAND) - stand);
            expect("主动击杀功德增量", 3, read(CultivationValue.MERITS) - merits);
        } finally {
            player.setItemInHand(InteractionHand.MAIN_HAND, held);
            Cultivation.setMainTechnique(player, technique);
            Cultivation.set(player, CultivationValue.EXPERIENCE_STAND, stand);
            Cultivation.set(player, CultivationValue.MERITS, merits);
            Cultivation.set(player, CultivationValue.VIGOROUS, vigorous);
        }
    }

    private double read(CultivationValue value) {
        return Cultivation.get(player, value);
    }

    private void cleanup() {
        if (listening) {
            NeoForge.EVENT_BUS.unregister(this);
            listening = false;
        }
        var objective = level.getScoreboard().getObjective(SCORE);
        for (Guiguai02Entity child : children) {
            if (objective != null) level.getScoreboard().resetSinglePlayerScore(child, objective);
            if (!child.isRemoved()) child.discard();
        }
        for (Entity entity : fixtures) if (!entity.isRemoved()) entity.discard();
        if (difficulty == Difficulty.PEACEFUL) player.server.setDifficulty(difficulty, true);
        finish();
    }
}
