package net.yvanchuxiuzhen.smoke;

import java.util.List;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.world.portal.DimensionPortalSpec;
import net.yvanchuxiuzhen.system.xiulian.XiulianWorldData;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationText;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/** 独立新档的原版近战击杀、每秒吸收和实际死亡重生。定位与伤害布置不经过玩法结算接口。 */
final class RenjianDeathCheck extends SystemCheck {
    private static final BlockPos ARENA = new BlockPos(8, 180, 8);
    private final Screenshots screenshots;
    private volatile int targetId;
    private volatile boolean rewardObserved;
    private volatile boolean deathObserved;
    private volatile boolean respawnObserved;
    private volatile int postTicks;
    private Mob target;
    private Mob attacker;
    private int killCase;
    private volatile int deathCase = -1;
    private int playerId;
    private double gained;
    private double beforeExperience;
    private double beforeVigorous;
    private double beforeMerits;
    private double beforeLifetime;
    private ServerPlayer respawned;

    RenjianDeathCheck(List<String> report, String levelId, Screenshots screenshots) {
        super("人间真实死亡与击杀", report, levelId);
        this.screenshots = screenshots;
    }

    @Override
    protected boolean step(int step, int local) {
        try {
            return advance(step, local);
        } catch (RuntimeException exception) {
            abort("步骤 " + step + " 异常：" + exception);
            return false;
        }
    }

    private boolean advance(int step, int local) {
        switch (step) {
            case 0 -> onServer(this::prepare);
            case 1, 4 -> onServer(player -> prepareKill(player, step == 1 ? 0 : 1));
            case 2, 5 -> {
                Entity victim = minecraft.level.getEntity(targetId);
                if (victim == null || !minecraft.player.getMainHandItem().is(Items.IRON_SWORD)
                        || minecraft.player.distanceToSqr(victim) >= 9) return await(local, "目标或近战物品未同步");
                minecraft.gameMode.attack(minecraft.player, victim);
            }
            case 3, 6 -> {
                if (!rewardObserved || !absorbed(minecraft.player)) return await(local, "死亡奖励或下一真实修炼周期未同步");
                onServer(player -> expectTrue("服务端正常吸收且待吸收清零", absorbed(player)));
                expect("本人功德", beforeMerits + (killCase == 0 ? -1 : 2),
                        Cultivation.get(minecraft.player, CultivationValue.MERITS));
                expect("本人罡煞", beforeVigorous - 1, Cultivation.get(minecraft.player, CultivationValue.VIGOROUS));
            }
            case 7 -> onServer(this::enterHumanRealm);
            case 8 -> {
                if (!inHumanRealm() || minecraft.player.distanceToSqr(8.5, 180, 8.5) >= 9) return await(local, "人间场地未同步");
                onServer(player -> hurtPlayer(player, 0));
            }
            case 9, 12, 16 -> {
                if (!(minecraft.screen instanceof DeathScreen) || local < 20) return await(local, "缺少真实死亡界面");
                if (!deathObserved || !markerMatchesClient(deathCase == 0 ? "" : "人间界"))
                    return await(local, "旧玩家死亡标记未同步");
                screenshots.take("renjian_death_" + deathCase + ".png");
                minecraft.player.respawn();
            }
            case 10, 13, 17 -> {
                if (!respawnObserved || postTicks < 21 || minecraft.screen != null || !inHumanRealm()
                        || !minecraft.player.isAlive() || !markerMatchesClient(step == 13 ? "人间界" : ""))
                    return await(local, "人间重生资源/标记或 20 刻尾段未完成");
                onServer(player -> {
                    expectTrue("真实重生已替换为事件新玩家", player == respawned);
                    expect("服务端重生寿元保持", beforeLifetime - 30, Cultivation.get(player, CultivationValue.LIFETIME));
                });
                expect("本人重生寿元", beforeLifetime - 30, Cultivation.get(minecraft.player, CultivationValue.LIFETIME));
                if (step == 17) {
                    screenshots.take("renjian_revive_return.png");
                    complete();
                }
            }
            case 11 -> {
                if (postTicks < 61) return await(local, "原版重生出生保护尚未结束");
                onServer(player -> hurtPlayer(player, 1));
            }
            case 14 -> onServer(player -> {
                moveToArena(player, player.server.overworld());
                player.setRespawnPosition(Level.OVERWORLD, ARENA, 0, true, false);
            });
            case 15 -> {
                if (!minecraft.level.dimension().equals(Level.OVERWORLD)) return await(local, "主世界返回未同步");
                if (postTicks < 61) return await(local, "原版重生出生保护尚未结束");
                onServer(player -> hurtPlayer(player, 2));
            }
            default -> throw new IllegalStateException("未知步骤 " + step);
        }
        return true;
    }

    private void prepare(ServerPlayer player) {
        if (!player.level().dimension().equals(Level.OVERWORLD)
                || !Cultivation.get(player, CultivationText.REVIVE).isEmpty()
                || Cultivation.get(player, CultivationValue.LEVEL_NUMBER) != 0) {
            throw new IllegalStateException("需要单独运行的全新主世界凡人档，初始重生标记必须为空");
        }
        NeoForge.EVENT_BUS.register(this);
        player.server.setDifficulty(Difficulty.NORMAL, true);
        player.level().getGameRules().getRule(GameRules.RULE_KEEPINVENTORY).set(true, player.server);
        player.level().getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, player.server);
        player.level().getGameRules().getRule(GameRules.RULE_NATURAL_REGENERATION).set(false, player.server);
        player.setGameMode(GameType.SURVIVAL);
        XiulianWorldData.get(player.server).setDailyExperience(0);
        moveToArena(player, player.server.overworld());
        player.getInventory().setItem(player.getInventory().selected, Items.IRON_SWORD.getDefaultInstance());
    }

    private void prepareKill(ServerPlayer player, int sample) {
        killCase = sample;
        rewardObserved = false;
        beforeExperience = Cultivation.get(player, CultivationValue.EXPERIENCE);
        beforeVigorous = Cultivation.get(player, CultivationValue.VIGOROUS);
        beforeMerits = Cultivation.get(player, CultivationValue.MERITS);
        expect("每次攻击前无残留待吸收", 0, Cultivation.get(player, CultivationValue.EXPERIENCE_STAND));
        target = controlledMob(player.serverLevel(), sample == 0 ? EntityType.COW : EntityType.ZOMBIE, 8.5, 180, 10.5);
        target.setHealth(0.1F);
        targetId = target.getId();
    }

    private void enterHumanRealm(ServerPlayer player) {
        ServerLevel human = player.server.getLevel(DimensionPortalSpec.RENJIAN.dimension());
        if (human == null) throw new IllegalStateException("缺少人间道");
        buildPlatform(human, new BlockPos(0, 32, 0));
        moveToArena(player, human);
        player.setRespawnPosition(human.dimension(), ARENA, 0, true, false);
        attacker = controlledMob(human, EntityType.ZOMBIE, 8.5, 180, 10.5);
    }

    private void hurtPlayer(ServerPlayer player, int sample) {
        deathCase = sample;
        deathObserved = false;
        respawnObserved = false;
        postTicks = 0;
        playerId = player.getId();
        beforeLifetime = Cultivation.get(player, CultivationValue.LIFETIME);
        expect("真实死亡前标记 " + sample, sample < 2 ? "" : "人间界", Cultivation.get(player, CultivationText.REVIVE));
        DamageSource source = sample == 1 ? player.damageSources().mobAttack(attacker) : player.damageSources().fall();
        expectTrue("原版 hurt 实际造成死亡 " + sample, player.hurt(source, 1000) && player.isDeadOrDying());
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onDeath(LivingDeathEvent event) {
        if (event.getEntity() == target) {
            if (!(event.getSource().getEntity() instanceof ServerPlayer killer)) {
                fail("主世界原生近战死亡没有玩家伤害来源");
                return;
            }
            expectTrue("主世界原生近战伤害双重归属", killer.level().dimension().equals(Level.OVERWORLD)
                    && event.getSource().getDirectEntity() == killer);
            expect("死者未分配层数镜像", 0, EntityAttributes.level(target));
            gained = killCase == 0 ? 2 : 4;
            expect("死亡当刻仍未吸收", beforeExperience, Cultivation.get(killer, CultivationValue.EXPERIENCE));
            expect("死亡当刻待吸收", gained, Cultivation.get(killer, CultivationValue.EXPERIENCE_STAND));
            expect("死亡当刻罡煞", beforeVigorous - 1, Cultivation.get(killer, CultivationValue.VIGOROUS));
            expect("死亡当刻功德", beforeMerits + (killCase == 0 ? -1 : 2), Cultivation.get(killer, CultivationValue.MERITS));
            rewardObserved = true;
        } else if (deathCase >= 0 && event.getEntity() instanceof ServerPlayer player && player.getId() == playerId) {
            expect("死亡发生世界", deathCase < 2 ? DimensionPortalSpec.RENJIAN.dimension() : Level.OVERWORLD, player.level().dimension());
            expectTrue("真实死亡来源符合样本", deathCase == 1 ? event.getSource().getEntity() == attacker
                    : event.getSource().getEntity() == null);
            expect("旧玩家自动标记", deathCase == 0 ? "" : "人间界", Cultivation.get(player, CultivationText.REVIVE));
            deathObserved = true;
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!deathObserved || respawnObserved || !(event.getEntity() instanceof ServerPlayer player)) return;
        expectTrue("死亡而非末地返回", !event.isEndConquered());
        expect("重生所在维度", DimensionPortalSpec.RENJIAN.dimension(), player.level().dimension());
        expect("原生重生复制或消费标记", deathCase == 1 ? "人间界" : "", Cultivation.get(player, CultivationText.REVIVE));
        expect("寿元只减一次", beforeLifetime - 30, Cultivation.get(player, CultivationValue.LIFETIME));
        expect("原版生命上限的两成", player.getMaxHealth() * 0.2, player.getHealth());
        if (deathCase == 2) {
            expect("返回 X", 0, player.getX());
            expect("返回 Y", 32, player.getY());
            expect("返回 Z", 0, player.getZ());
        } else {
            expectTrue("同维保留原生重生点附近", player.blockPosition().distSqr(ARENA) < 9);
        }
        player.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(173);
        respawned = player;
        respawnObserved = true;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPost(ServerTickEvent.Post event) {
        if (!respawnObserved || postTicks >= 61) return;
        int tick = ++postTicks;
        if (tick == 19 || tick == 20) {
            expect("第 " + tick + " 个结束刻属性", tick == 19 ? 173 : PlayerAttributes.value(respawned, Stat.ATTACK),
                    respawned.getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue());
        }
    }

    private boolean absorbed(Player player) {
        return Math.abs(Cultivation.get(player, CultivationValue.EXPERIENCE) - beforeExperience - gained) < 1e-6
                && Cultivation.get(player, CultivationValue.EXPERIENCE_STAND) == 0;
    }

    private boolean inHumanRealm() {
        return minecraft.level != null && minecraft.level.dimension().equals(DimensionPortalSpec.RENJIAN.dimension());
    }

    private boolean markerMatchesClient(String expected) {
        return expected.equals(Cultivation.get(minecraft.player, CultivationText.REVIVE));
    }

    private boolean await(int local, String message) {
        if (local > 300) abort(message);
        return false;
    }

    private void abort(String message) {
        fail(message);
        if (minecraft.getSingleplayerServer() != null) complete();
        else finish();
    }

    private void complete() {
        onServer(player -> {
            NeoForge.EVENT_BUS.unregister(this);
            if (target != null) target.discard();
            if (attacker != null) attacker.discard();
        });
        finish();
    }

    private static Mob controlledMob(ServerLevel level, EntityType<? extends Mob> type, double x, double y, double z) {
        Mob mob = type.create(level);
        if (mob == null) throw new IllegalStateException("未创建目标实体 " + type);
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        mob.moveTo(x, y, z, 180, 0);
        level.addFreshEntity(mob);
        return mob;
    }

    private static void moveToArena(ServerPlayer player, ServerLevel level) {
        buildPlatform(level, ARENA);
        if (player.serverLevel() == level) player.connection.teleport(8.5, 180, 8.5, 0, 0);
        else player.teleportTo(level, 8.5, 180, 8.5, 0, 0);
    }

    private static void buildPlatform(ServerLevel level, BlockPos center) {
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-4, -1, -4), center.offset(4, 4, 4))) {
            boolean solid = pos.getY() == center.getY() - 1 || pos.getY() == center.getY() + 4;
            level.setBlockAndUpdate(pos, solid ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState());
        }
    }
}
