package net.yvanchuxiuzhen.smoke;

import java.util.List;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.world.portal.DimensionPortalSpec;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/** 可控的原版怪物近战、战斗恢复中断与同档重进；不写入战斗记分板。 */
final class RenjianCombatLifecycleCheck extends SystemCheck {
    private static final BlockPos ARENA = new BlockPos(8, 180, 8);
    private static final String POWER_RESTORE = "PowerRestorePercentage";
    private final BooleanSupplier readyToExit;
    private final Runnable recordBeforeSave;
    private UUID playerId;
    private Zombie attacker;
    private ServerPlayer outgoing;
    private boolean observing;
    private boolean disconnected;
    private Sample beforeTick;
    private volatile Sample atLogin;
    private Sample beforeSave;
    private volatile boolean quarterRecovery;
    private volatile boolean fullRecovery;
    private volatile boolean expired;
    private boolean badTick;

    private record Sample(int active, int count, int hurtTime, double power, double cultivationTimer) {
    }

    RenjianCombatLifecycleCheck(List<String> report, String levelId, BooleanSupplier readyToExit, Runnable recordBeforeSave) {
        super("人间普通战斗", report, levelId);
        this.readyToExit = readyToExit;
        this.recordBeforeSave = recordBeforeSave;
    }

    @Override
    protected boolean step(int step, int local) {
        try {
            return advance(step, local);
        } catch (RuntimeException exception) {
            stop("步骤 " + step + " 异常：" + exception);
            return false;
        }
    }

    private boolean advance(int step, int local) {
        switch (step) {
            case 0 -> onServer(this::prepare);
            case 1 -> {
                if (!inHumanRealm() || minecraft.player.distanceToSqr(8.5, 180, 8.5) > 1 || local < 20)
                    return await(local, "人间场地未同步");
                onServer(this::strike);
            }
            case 2 -> {
                if (!quarterRecovery || !expired || !fullRecovery) return await(local, "首次近战未完成战斗计数或双档恢复");
            }
            case 3 -> {
                if (!disconnected) {
                    if (!readyToExit.getAsBoolean()) return false;
                    onServer(player -> {
                        outgoing = player;
                        quarterRecovery = false;
                        expired = false;
                        fullRecovery = false;
                        strike(player);
                        beforeSave = sample(player);
                        expectTrue("离档前战斗确实仍在进行：" + beforeSave, beforeSave.active() == 1);
                    });
                    recordBeforeSave.run();
                    disconnectWorld();
                    disconnected = true;
                    return false;
                }
                return reopened(local);
            }
            case 4 -> {
                if (!inWorld(local)) {
                    if (local > 1200) stop(null);
                    return false;
                }
                WorldStopDiagnostics.arm(minecraft.getSingleplayerServer());
                expectTrue("磁盘重开仍在人间道", inHumanRealm());
                if (atLogin == null) {
                    stop("未收到同 UUID 新服务端玩家的真实登录事件");
                    return false;
                }
                expect("登录即读回激活记分", 1, atLogin.active());
                expect("登录即读回原生 HurtByTimestamp", beforeSave.hurtTime(), atLogin.hurtTime());
                expectTrue("登录读回的非零战斗计数未清空或倒退，保存前=" + beforeSave.count()
                        + "，登录时=" + atLogin.count(), atLogin.count() > 0 && atLogin.count() >= beforeSave.count());
            }
            case 5 -> {
                if (!expired || !fullRecovery) return await(local, "重开后未自然结束战斗并恢复正常档");
                onServer(player -> {
                    expect("重开战斗终点服务端记分", 0, sample(player).active());
                    expect("重开战斗终点服务端计数", 0, sample(player).count());
                    Cultivation.setTizhiDouble(player, POWER_RESTORE, 0);
                });
                stop(null);
            }
            default -> throw new IllegalStateException("未知步骤 " + step);
        }
        return true;
    }

    private void prepare(ServerPlayer player) {
        playerId = player.getUUID();
        ServerLevel human = player.server.getLevel(DimensionPortalSpec.RENJIAN.dimension());
        if (human == null) throw new IllegalStateException("缺少人间道");
        player.server.setDifficulty(Difficulty.NORMAL, true);
        human.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, player.server);
        player.setGameMode(GameType.SURVIVAL);
        for (BlockPos pos : BlockPos.betweenClosed(ARENA.offset(-2, -1, -2), ARENA.offset(2, 3, 2))) {
            boolean boundary = pos.getY() == ARENA.getY() - 1 || pos.getY() == ARENA.getY() + 3;
            human.setBlockAndUpdate(pos, boundary ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState());
        }
        player.teleportTo(human, 8.5, 180, 8.5, 0, 0);
        attacker = EntityType.ZOMBIE.create(human);
        if (attacker == null) throw new IllegalStateException("缺少受控近战目标");
        attacker.setNoAi(true);
        attacker.setPersistenceRequired();
        attacker.moveTo(8.5, 180, 9.5, 0, 0);
        human.addFreshEntity(attacker);
        NeoForge.EVENT_BUS.register(this);
        observing = true;
    }

    private void strike(ServerPlayer player) {
        expect("受击前为凡人", 0, Cultivation.get(player, CultivationValue.LEVEL_NUMBER));
        expect("受击前战斗已消退", 0, sample(player).active());
        // 只提供非零且不触碰上限的恢复样本，恢复本身仍由每秒例程执行。
        Cultivation.setTizhiDouble(player, POWER_RESTORE, 0.01);
        Cultivation.set(player, CultivationValue.POWER, 0);
        player.setHealth(player.getMaxHealth());
        float health = player.getHealth();
        player.setDeltaMovement(0, 0, 0);
        expectTrue("原版 Zombie.doHurtTarget 实际扣血", attacker.doHurtTarget(player) && player.getHealth() < health && player.isAlive());
        expectTrue("原生受伤记录攻击者和人间道", player.getLastHurtByMob() == attacker
                && player.level().dimension().equals(DimensionPortalSpec.RENJIAN.dimension()));
        expect("真正受击触发战斗记分", 1, sample(player).active());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void before(PlayerTickEvent.Pre event) {
        if (event.getEntity() instanceof ServerPlayer player && player.getUUID().equals(playerId)) beforeTick = sample(player);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void after(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !player.getUUID().equals(playerId) || beforeTick == null) return;
        Sample now = sample(player);
        if (beforeTick.active() == 1 && beforeTick.hurtTime() == now.hurtTime()) {
            long difference = (long) beforeTick.count() + 1 - now.hurtTime();
            if (difference >= 100) {
                if (!expired) {
                    expect("来源清除边界恰为 100", 100, difference);
                    expect("达到来源差值时记分归零", 0, now.active());
                    expect("达到来源差值时计数归零", 0, now.count());
                    expired = now.active() == 0;
                }
            } else if (!badTick && (now.active() != 1 || now.count() != beforeTick.count() + 1)) {
                fail("差值 " + difference + " 未到 100 却提前清除，或一个生物刻未恰好增加 1");
                badTick = true;
            }
        }
        if (now.cultivationTimer() == beforeTick.cultivationTimer()) return;
        double max = PlayerAttributes.value(player, Stat.POWER_MAX);
        double amount = PlayerAttributes.value(player, Stat.POWER_REC) + max * 0.01;
        if (amount <= 0 || beforeTick.power() + amount >= max) return;
        if (now.active() == 1 && !quarterRecovery) {
            expect("战斗中一轮真实灵力恢复为四分之一", amount * 0.25, now.power() - beforeTick.power());
            quarterRecovery = true;
        } else if (now.active() == 0 && expired && !fullRecovery) {
            expect("脱战后一轮真实灵力恢复回到正常档", amount, now.power() - beforeTick.power());
            fullRecovery = true;
        }
    }

    @SubscribeEvent
    public void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && player.getUUID().equals(playerId) && player != outgoing
                && outgoing != null) atLogin = sample(player);
    }

    private boolean inHumanRealm() {
        return minecraft.player != null && minecraft.player.level().dimension().equals(DimensionPortalSpec.RENJIAN.dimension());
    }

    private static Sample sample(ServerPlayer player) {
        return new Sample(score(player, "zdztbegin"), score(player, "zdzttick"), player.getLastHurtByMobTimestamp(),
                Cultivation.get(player, CultivationValue.POWER), Cultivation.tizhiDouble(player, "jishiqi"));
    }

    private static int score(Player player, String name) {
        var scoreboard = player.getScoreboard();
        var objective = scoreboard.getObjective(name);
        var score = objective == null ? null : scoreboard.getPlayerScoreInfo(player, objective);
        return score == null ? 0 : score.value();
    }

    private boolean await(int local, String reason) {
        if (local > 450) stop(reason);
        return false;
    }

    private void stop(String failure) {
        if (failure != null) fail(failure);
        if (observing) NeoForge.EVENT_BUS.unregister(this);
        observing = false;
        finish();
    }
}
