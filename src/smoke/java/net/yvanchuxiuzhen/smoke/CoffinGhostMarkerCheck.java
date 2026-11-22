package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.creature.CreatureSetup;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationAfterBreakthroughEvent;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationAfterRecoveryEvent;

/** 独立新开发档。只预置水槽、阵营、已选招和恢复样本，不调用呼吸、爆气、战斗计时或恢复函数。 */
final class CoffinGhostMarkerCheck extends SystemCheck {
    private final List<BlockPos> tank = new ArrayList<>();
    private final List<Mob> mobs = new ArrayList<>();
    private ServerPlayer player;
    private ServerLevel level;
    private Mob wet, control, king, enemy, ally, ordinary, ghost;
    private GameType mode;
    private Difficulty difficulty;
    private float health, beforeRecovery;
    private double correction;
    private boolean registered, armed, observedBurst, hit, inCombatSample, outSample, cleared;
    private int markerDeaths, wetSamples, controlSamples, beforeClear, sampling;
    private long start;

    CoffinGhostMarkerCheck(List<String> report, String levelId) {
        super("箭头水下呼吸、小尸王爆气与游魂受击恢复", report, levelId);
    }

    @Override
    protected boolean step(int step, int local) {
        onServer(p -> {
            try {
                if (step == 0) prepare(p);
                else if (!armed && wet.tickCount >= 5 && control.tickCount >= 5) {
                    require(wet.isEyeInFluid(FluidTags.WATER) && control.isEyeInFluid(FluidTags.WATER), "水槽未覆盖两者眼部");
                    wet.setAirSupply(80);
                    control.setAirSupply(80);
                    armed = true;
                    king.getPersistentData().putString("gongjizhe", ordinary.getStringUUID());
                    king.getPersistentData().putString("jineng", "爆气");
                } else if (!hit && wetSamples >= 12 && controlSamples >= 12 && observedBurst && p.tickCount > 65) {
                    control.discard();
                    wet.discard();
                    expect("箭头无死亡事件", 0, markerDeaths);
                    require(score("zdztbegin") == 0 && !Cultivation.thunderOpen(p), "需要未入战且不在雷劫中的玩家");
                    // 定位到近战距离，只调用一次游魂原生攻击入口；不取消伤害，不人为置入战或离战分数。
                    ghost.setPos(p.getX() + 1, p.getY(), p.getZ());
                    float hp = p.getHealth();
                    require(ghost.doHurtTarget(p) && p.isAlive() && p.getHealth() < hp, "游魂近战未使玩家实际扣血");
                    require(score("zdztbegin") == 1, "实际受击没有置入战");
                    hit = true;
                    ghost.setPos(king.getX(), king.getY() + 6, king.getZ());
                } else if (outSample && inCombatSample && cleared) {
                    cleanup();
                }
                if (step != 0 && registered && (local > 1200 || level.getGameTime() - start > 1000)) {
                    throw new IllegalStateException("超时：水下刻=" + wetSamples + "/" + controlSamples + "，爆气=" + observedBurst
                            + "，受击=" + hit + "，战斗恢复=" + inCombatSample + "，离战=" + cleared + "，平时恢复=" + outSample);
                }
            } catch (RuntimeException ex) {
                fail(ex.toString());
                cleanup();
            }
        });
        return step == 0;
    }

    private void prepare(ServerPlayer p) {
        player = p;
        level = p.serverLevel();
        mode = p.gameMode.getGameModeForPlayer();
        difficulty = level.getDifficulty();
        health = p.getHealth();
        correction = Cultivation.tizhiDouble(p, "HealthRestorePercentage");
        start = level.getGameTime();
        p.server.setDifficulty(Difficulty.NORMAL, true);
        p.setGameMode(GameType.SURVIVAL);
        BlockPos o = p.blockPosition().above(20);
        for (BlockPos pos : BlockPos.betweenClosed(o.offset(-2, -1, -1), o.offset(2, 2, 1))) {
            require(level.getBlockState(pos).is(Blocks.AIR), "夹具需要空中位置：" + pos);
        }
        // 封闭槽先铺壳再加水，避免流出夹具范围。
        for (BlockPos pos : BlockPos.betweenClosed(o.offset(-2, -1, -1), o.offset(2, 2, 1))) {
            if (Math.abs(pos.getX() - o.getX()) == 2 || pos.getZ() != o.getZ()
                    || pos.getY() < o.getY() || pos.getY() > o.getY() + 1) block(pos, false);
        }
        for (BlockPos pos : BlockPos.betweenClosed(o.offset(-1, 0, 0), o.offset(1, 1, 0))) block(pos, true);
        Vec3 at = Vec3.atBottomCenterOf(o);
        wet = place(creature("jiantou"), at.add(-1, 0, 0));
        control = place(EntityType.PIG.create(level), at.add(1, 0, 0));
        at = at.add(10, 0, 0);
        king = place(creature("guiguai_03"), at);
        ally = place(creature("jiantou"), at.add(-2, 0, 0));
        enemy = place(creature("jiantou"), at.add(2, 0, 0));
        ordinary = place(EntityType.PIG.create(level), at.add(0, 0, 3));
        ghost = place(creature("guiguai_01"), at.add(0, 6, 0));
        Relations.setTeam(ally, "marker_ally");
        Relations.addFriendlyTeam(king, "marker_ally");
        Relations.setTeam(enemy, "marker_enemy");
        Relations.setTeam(ordinary, "marker_enemy");
        NeoForge.EVENT_BUS.register(this);
        registered = true;
    }

    private Mob creature(String id) { return CreatureSetup.<Mob>entityType(id).get().create(level); }

    private Mob place(Mob mob, Vec3 pos) {
        mob.moveTo(pos.x, pos.y, pos.z, 0, 0);
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
        mob.setNoAi(true);
        mob.setNoGravity(true);
        require(level.noCollision(mob, mob.getBoundingBox()) && level.addFreshEntity(mob), "无法布置 " + mob.getType());
        mobs.add(mob);
        return mob;
    }

    private void block(BlockPos pos, boolean water) {
        require(level.setBlock(pos, (water ? Blocks.WATER : Blocks.GLASS).defaultBlockState(), 2), "不能布置水槽：" + pos);
        tank.add(pos.immutable());
    }

    @SubscribeEvent
    public void entityPost(EntityTickEvent.Post e) {
        if (!armed) return;
        if (e.getEntity() == wet && wetSamples < 12) {
            wetSamples++;
            expectTrue("水下箭头第" + wetSamples + "刻眼部在水中", wet.isEyeInFluid(FluidTags.WATER));
            expect("箭头实际空气值", 80, wet.getAirSupply());
        } else if (e.getEntity() == control && controlSamples < 12) {
            controlSamples++;
            expectTrue("普通对照眼部在水中", control.isEyeInFluid(FluidTags.WATER));
            expect("普通对照逐刻耗气", 80 - controlSamples, control.getAirSupply());
        } else if (e.getEntity() == king && !observedBurst) {
            observedBurst = true;
            expectTrue("敌方箭头直接移除", enemy.getRemovalReason() == Entity.RemovalReason.DISCARDED);
            expectTrue("友方箭头留场且未受击退", ally.isAlive() && ally.getDeltaMovement().lengthSqr() < 1e-8);
            Vec3 push = new Vec3(ordinary.getX() - king.getX(), 0.5, ordinary.getZ() - king.getZ());
            expectTrue("普通敌方留场且沿径向击退", ordinary.isAlive() && ordinary.getDeltaMovement().distanceToSqr(push) < 1e-8);
            expect("爆气已收招", "", king.getPersistentData().getString("jineng"));
        }
    }

    @SubscribeEvent
    public void death(LivingDeathEvent e) {
        if (e.getEntity() == enemy || e.getEntity() == ally) markerDeaths++;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void beforeCombatClock(PlayerTickEvent.Pre e) {
        if (hit && e.getEntity() == player && score("zdztbegin") == 1) {
            beforeClear = score("zdzttick") + 1 - player.getLastHurtByMobTimestamp();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void afterCombatClock(PlayerTickEvent.Pre e) {
        if (hit && e.getEntity() == player && !cleared && score("zdztbegin") == 0) {
            expect("离战时计数与受击时间之差", 100, beforeClear);
            expect("离战同时清计数", 0, score("zdzttick"));
            cleared = true;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void beforeRecovery(CultivationAfterBreakthroughEvent e) {
        if (!hit || e.player() != player) return;
        boolean fighting = score("zdztbegin") == 1;
        if (fighting ? inCombatSample : !cleared || outSample) return;
        // 两组取相同缺口、基础恢复总量6：战中 round(1.5)=2，离战=6。只预置输入，不写结算结果。
        beforeRecovery = player.getMaxHealth() - 8;
        player.setHealth(beforeRecovery);
        Cultivation.setTizhiDouble(player, "HealthRestorePercentage",
                (6 - PlayerAttributes.value(player, Stat.HEALTH_REC)) / player.getMaxHealth());
        sampling = fighting ? 2 : 6;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void afterRecovery(CultivationAfterRecoveryEvent e) {
        if (e.player() != player || sampling == 0) return;
        expect(sampling == 2 ? "受击后战斗中的实际回血" : "自然离战后的实际回血", sampling, player.getHealth() - beforeRecovery);
        if (sampling == 2) inCombatSample = true;
        else outSample = true;
        sampling = 0;
        Cultivation.setTizhiDouble(player, "HealthRestorePercentage", correction);
    }

    private int score(String name) {
        var board = level.getScoreboard();
        var objective = board.getObjective(name);
        var value = objective == null ? null : board.getPlayerScoreInfo(player, objective);
        return value == null ? 0 : value.value();
    }

    private static void require(boolean ok, String message) {
        if (!ok) throw new IllegalStateException(message);
    }

    private void cleanup() {
        if (registered) NeoForge.EVENT_BUS.unregister(this);
        registered = false;
        for (Mob mob : mobs) if (!mob.isRemoved()) mob.discard();
        for (BlockPos pos : tank.reversed()) level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
        Cultivation.setTizhiDouble(player, "HealthRestorePercentage", correction);
        player.setHealth(health);
        player.setGameMode(mode);
        player.server.setDifficulty(difficulty, true);
        finish();
    }
}
