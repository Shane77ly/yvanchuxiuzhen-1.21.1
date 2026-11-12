package net.yvanchuxiuzhen.smoke;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.gui.components.Button;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.yvanchuxiuzhen.smoke.ActionCheck.Action;
import net.yvanchuxiuzhen.system.attribute.api.Contribution;
import net.yvanchuxiuzhen.system.attribute.api.Layer;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.skill.data.SkillCooldowns;
import net.yvanchuxiuzhen.system.skill.data.SkillData;
import net.yvanchuxiuzhen.system.skill.entity.Yuanying;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/** 元婴出窍的门槛、消耗、受伤同步、本体致死及出窍状态重登后经真实按钮归窍。 */
final class YuanyingCheck extends SystemCheck {
    private static final String CONTRIBUTION = "smoke:yuanying";
    private static final CultivationValue[] VALUES = {CultivationValue.LEVEL_NUMBER, CultivationValue.SOUL, CultivationValue.POWER,
            CultivationValue.LIFETIME, CultivationValue.EXPERIENCE_STAND};
    private final double[] originalValues = new double[VALUES.length];
    private final List<ItemStack> inventory = new ArrayList<>();
    private ActionCheck actions;
    private SkillCooldowns cooldowns;
    private double originalCorrection;
    private double originalTimer;
    private double soulAfterCast;
    private UUID savedBodyId;
    private UUID savedPlayerId;
    private Vec3 returnPosition;
    private int originalExperienceLevel;
    private int originalTotalExperience;
    private float originalExperienceProgress;
    private boolean originalOut;
    private GameType originalMode;
    private float originalHealth;
    private double originalX;
    private double originalY;
    private double originalZ;
    private ResourceKey<Level> originalDimension;
    private ResourceKey<Level> crossDimensionOrigin;
    private UUID crossDimensionBodyId;

    YuanyingCheck(List<String> report, String levelId) {
        super("元婴出窍", report, levelId);
        script(report, levelId,
                new Action(8, p -> {
                    for (int i = 0; i < VALUES.length; i++) originalValues[i] = Cultivation.get(p, VALUES[i]);
                    originalCorrection = Cultivation.tizhiDouble(p, "LevelNumberCorrect");
                    originalTimer = Cultivation.tizhiDouble(p, "jishiqi");
                    originalOut = Cultivation.tizhiBoolean(p, "yvanyingchuqiao");
                    originalMode = p.gameMode.getGameModeForPlayer();
                    originalHealth = p.getHealth();
                    originalX = p.getX();
                    originalY = p.getY();
                    originalZ = p.getZ();
                    originalDimension = p.level().dimension();
                    for (int slot = 0; slot < p.getInventory().getContainerSize(); slot++) inventory.add(p.getInventory().getItem(slot).copy());
                    cooldowns = p.getData(SkillData.COOLDOWNS);
                    originalExperienceLevel = p.experienceLevel;
                    originalTotalExperience = p.totalExperience;
                    originalExperienceProgress = p.experienceProgress;
                    p.setGameMode(GameType.SURVIVAL);
                    Arena.level(p);
                    Cultivation.set(p, CultivationValue.LEVEL_NUMBER, 30);
                    Cultivation.setTizhiDouble(p, "LevelNumberCorrect", 0);
                    Cultivation.set(p, CultivationValue.SOUL, 10000);
                    Cultivation.setTizhiBoolean(p, "yvanyingchuqiao", false);
                    Skills.toggleYuanying(p);
                    expect("境界不足时不能出窍", 0, bodies(p).size());
                }),
                new Action(24, p -> {
                    Cultivation.set(p, CultivationValue.LEVEL_NUMBER, 31);
                    Cultivation.set(p, CultivationValue.SOUL, 10000);
                    Cultivation.setTizhiDouble(p, "jishiqi", p.level().getDayTime());
                    double before = Cultivation.get(p, CultivationValue.SOUL);
                    Skills.toggleYuanying(p);
                    soulAfterCast = Cultivation.get(p, CultivationValue.SOUL);
                    expectTrue("达到元婴期后扣神识并生成本体元婴",
                            Cultivation.tizhiBoolean(p, "yvanyingchuqiao") && p.gameMode.getGameModeForPlayer() == GameType.SPECTATOR
                                    && bodies(p).size() == 1 && Cultivation.get(p, CultivationValue.SOUL) < before - 61);
                }),
                new Action(8, p -> {
                    Yuanying body = bodies(p).get(0);
                    float bodyHealth = body.getHealth();
                    body.hurt(p.damageSources().generic(), 1);
                    expectTrue("出窍移动持续消耗神识，元婴受伤同步本体生命",
                            Cultivation.get(p, CultivationValue.SOUL) < soulAfterCast && p.getHealth() == bodyHealth && body.getHealth() < bodyHealth);
                    Cultivation.set(p, CultivationValue.SOUL, 10000);
                    Skills.openMenu(p);
                }),
                new Action(8, p -> pressReturn()),
                new Action(8, p -> {
                    expectTrue("旁观状态点击归窍按钮后恢复原位置与生存模式",
                            !Cultivation.tizhiBoolean(p, "yvanyingchuqiao") && p.gameMode.getGameModeForPlayer() == GameType.SURVIVAL
                                    && bodies(p).isEmpty() && p.position().distanceToSqr(originalX, originalY, originalZ) < 0.01);
                }),
                new Action(8, p -> {
                    Cultivation.set(p, CultivationValue.SOUL, 0);
                    Skills.toggleYuanying(p);
                    expectTrue("神识不足时不能再次出窍", !Cultivation.tizhiBoolean(p, "yvanyingchuqiao")
                            && p.gameMode.getGameModeForPlayer() == GameType.SURVIVAL);
                })
        );
    }

    /** 原有五项检查结束后继续死亡与重登场景，全部结束时统一还原玩家。 */
    private void script(List<String> report, String levelId, Action... steps) {
        actions = new ActionCheck("元婴出窍", report, levelId) {
            {
                script(steps);
            }

            @Override
            protected void cleanup(ServerPlayer player) {
            }
        };
    }

    @Override
    protected boolean step(int step, int local) {
        return switch (step) {
            case 0 -> actions.tick(local);
            case 1 -> {
                if (local < 8) yield false;
                onServer(p -> {
                    p.getInventory().clearContent();
                    p.experienceLevel = 0;
                    p.totalExperience = 0;
                    p.experienceProgress = 0;
                    Cultivation.set(p, CultivationValue.SOUL, 10000);
                    Cultivation.set(p, CultivationValue.LIFETIME, 10000);
                    Skills.toggleYuanying(p);
                    Yuanying body = bodies(p).getFirst();
                    double before = Cultivation.get(p, CultivationValue.LIFETIME);
                    boolean hurt = body.hurt(body.damageSources().generic(), body.getMaxHealth() * 2);
                    expectTrue("元婴本体真实伤害致死后清出窍并杀死主人，重生前不额外扣一年寿元", hurt && !body.isAlive() && !p.isAlive()
                            && !Cultivation.tizhiBoolean(p, "yvanyingchuqiao") && p.gameMode.getGameModeForPlayer() == GameType.SURVIVAL
                            && Cultivation.get(p, CultivationValue.LIFETIME) == before);
                });
                yield true;
            }
            case 2 -> respawned(local);
            case 3 -> {
                if (local < 100) yield false;
                onServer(p -> {
                    Arena.level(p);
                    Cultivation.set(p, CultivationValue.LIFETIME, 10000);
                    Cultivation.set(p, CultivationValue.SOUL, 10000);
                    Cultivation.setTizhiDouble(p, "jishiqi", p.level().getDayTime());
                    returnPosition = p.position();
                    Skills.toggleYuanying(p);
                    p.setGameMode(GameType.SURVIVAL);
                    p.setHealth(p.getMaxHealth());
                    double before = Cultivation.get(p, CultivationValue.LIFETIME);
                    LivingDeathEvent event = new LivingDeathEvent(p, p.damageSources().mobAttack(new Zombie(EntityType.ZOMBIE, p.level())));
                    NeoForge.EVENT_BUS.post(event);
                    expectTrue("死亡事件先归窍并扣除365寿元，恢复生存模式与保存坐标",
                            !Cultivation.tizhiBoolean(p, "yvanyingchuqiao") && p.gameMode.getGameModeForPlayer() == GameType.SURVIVAL
                                    && Cultivation.get(p, CultivationValue.LIFETIME) == before - 365
                                    && p.position().distanceToSqr(returnPosition) < 0.01 && bodies(p).isEmpty());
                    p.kill();
                });
                yield true;
            }
            case 4 -> respawned(local);
            case 5 -> {
                if (!inWorld(local)) yield false;
                onServer(p -> {
                    Arena.level(p);
                    PlayerAttributes.setContribution(p, CONTRIBUTION, new Contribution(Layer.BASE, Map.of(Stat.SOUL_MAX, 1_000_000.0)));
                    Cultivation.set(p, CultivationValue.SOUL, 1_000_000);
                    Cultivation.setTizhiDouble(p, "jishiqi", p.level().getDayTime());
                    returnPosition = p.position();
                    Skills.toggleYuanying(p);
                    savedBodyId = bodies(p).getFirst().getUUID();
                    p.connection.teleport(p.getX() + 4, p.getY(), p.getZ(), p.getYRot(), p.getXRot());
                });
                yield true;
            }
            case 6 -> {
                if (local < 50) yield false;
                onServer(p -> {
                    expectTrue("断开前服务端出窍标记仍有效", Cultivation.tizhiBoolean(p, "yvanyingchuqiao"));
                    expectTrue("断开前服务端仍为旁观模式", p.isSpectator());
                    expectTrue("断开前离开本体且原UUID元婴身份有效", p.position().distanceToSqr(returnPosition) > 9
                            && p.serverLevel().getEntity(savedBodyId) instanceof Yuanying body && p.getUUID().equals(body.ownerId()));
                    savedPlayerId = p.getUUID();
                });
                leaveWorld();
                yield true;
            }
            case 7 -> reopened(local);
            case 8 -> inWorld(local);
            case 9 -> {
                onServer(p -> {
                    expect("同存档重登玩家UUID", savedPlayerId, p.getUUID());
                    expect("重登后修炼境界仍为元婴期", 31, Cultivation.get(p, CultivationValue.LEVEL_NUMBER));
                    expectTrue("重登后服务端仍为旁观模式", p.isSpectator());
                    expectTrue("重登后客户端同步旁观模式", minecraft.player.isSpectator());
                    expectTrue("重登后服务端出窍标记持久化", Cultivation.tizhiBoolean(p, "yvanyingchuqiao"));
                    expectTrue("重登后客户端同步出窍标记", Cultivation.tizhiBoolean(minecraft.player, "yvanyingchuqiao"));
                    expectTrue("重登后原UUID元婴实体存在", p.serverLevel().getEntity(savedBodyId) instanceof Yuanying);
                    if (p.serverLevel().getEntity(savedBodyId) instanceof Yuanying body) {
                        expect("重登后元婴主人UUID", p.getUUID(), body.ownerId());
                    } else {
                        expectTrue("重登后元婴主人UUID", false);
                    }
                    Skills.openMenu(p);
                });
                yield true;
            }
            case 10 -> {
                if (local < 10) yield false;
                onServer(p -> expectTrue("点击归窍按钮前仍保持重登后的出窍状态", Cultivation.tizhiBoolean(p, "yvanyingchuqiao")
                        && p.isSpectator() && p.serverLevel().getEntity(savedBodyId) instanceof Yuanying body
                        && p.getUUID().equals(body.ownerId())));
                pressReturn();
                yield true;
            }
            case 11 -> {
                if (local < 10) yield false;
                onServer(p -> expectTrue("重登后出窍状态经真实归窍按钮恢复保存坐标及生存模式，并移除原本体", !Cultivation.tizhiBoolean(p, "yvanyingchuqiao")
                        && !Cultivation.tizhiBoolean(minecraft.player, "yvanyingchuqiao") && p.gameMode.getGameModeForPlayer() == GameType.SURVIVAL
                        && !minecraft.player.isSpectator() && p.position().distanceToSqr(returnPosition) < .01
                        && minecraft.player.position().distanceToSqr(returnPosition) < .01 && p.serverLevel().getEntity(savedBodyId) == null));
                yield true;
            }
            case 12 -> {
                if (local < 40) yield false;
                onServer(p -> {
                    Arena.level(p);
                    Cultivation.setTizhiDouble(p, "jishiqi", p.level().getDayTime());
                    crossDimensionOrigin = p.level().dimension();
                    returnPosition = p.position();
                    Skills.toggleYuanying(p);
                    crossDimensionBodyId = bodies(p).getFirst().getUUID();
                    ServerLevel destination = p.server.getLevel(Level.NETHER);
                    p.teleportTo(destination, returnPosition.x, returnPosition.y, returnPosition.z, p.getYRot(), p.getXRot());
                    Arena.level(p);
                    Skills.openMenu(p);
                });
                yield true;
            }
            case 13 -> {
                if (local < 10) yield false;
                pressReturn();
                yield true;
            }
            case 14 -> {
                if (local < 10) yield false;
                onServer(p -> {
                    expect("跨维度归窍后仍在目标维度", Level.NETHER, p.level().dimension());
                    expectTrue("跨维度归窍使用原保存坐标并清除出窍标记", !Cultivation.tizhiBoolean(p, "yvanyingchuqiao")
                            && p.gameMode.getGameModeForPlayer() == GameType.SURVIVAL && p.position().distanceToSqr(returnPosition) < 0.01);
                    expectTrue("跨维度归窍不在目标维度删除元婴", bodies(p).isEmpty());
                    ServerLevel origin = p.server.getLevel(crossDimensionOrigin);
                    expectTrue("跨维度归窍后原维度元婴仍保留", origin.getEntity(crossDimensionBodyId) instanceof Yuanying body
                            && p.getUUID().equals(body.ownerId()));
                });
                yield true;
            }
            default -> {
                onServer(this::cleanup);
                finish();
                yield true;
            }
        };
    }

    private void pressReturn() {
        minecraft.execute(() -> minecraft.screen.children().stream()
                .filter(child -> child instanceof Button button && button.getMessage().getString().equals("元婴出窍"))
                .map(child -> (Button) child).findFirst().orElseThrow().onPress());
    }

    private static List<Yuanying> bodies(ServerPlayer player) {
        return player.serverLevel().getEntitiesOfClass(Yuanying.class, new AABB(player.blockPosition()).inflate(3)).stream()
                .filter(body -> player.getUUID().equals(body.ownerId())).toList();
    }

    private void cleanup(ServerPlayer p) {
        PlayerAttributes.clearContribution(p, CONTRIBUTION);
        discardBody(p.serverLevel(), savedBodyId);
        if (crossDimensionOrigin != null) discardBody(p.server.getLevel(crossDimensionOrigin), crossDimensionBodyId);
        if (Cultivation.tizhiBoolean(p, "yvanyingchuqiao")) {
            Skills.toggleYuanying(p);
        }
        ServerLevel original = p.server.getLevel(originalDimension);
        if (!p.level().dimension().equals(originalDimension)) {
            p.teleportTo(original, originalX, originalY, originalZ, p.getYRot(), p.getXRot());
        }
        for (Yuanying body : bodies(p)) {
            body.discard();
        }
        p.setGameMode(originalMode);
        p.connection.teleport(originalX, originalY, originalZ, p.getYRot(), p.getXRot());
        p.setHealth(originalHealth);
        p.getInventory().clearContent();
        for (int slot = 0; slot < inventory.size(); slot++) p.getInventory().setItem(slot, inventory.get(slot));
        p.setData(SkillData.COOLDOWNS, cooldowns);
        p.experienceLevel = originalExperienceLevel;
        p.totalExperience = originalTotalExperience;
        p.experienceProgress = originalExperienceProgress;
        for (int i = 0; i < VALUES.length; i++) Cultivation.set(p, VALUES[i], originalValues[i]);
        Cultivation.setTizhiDouble(p, "LevelNumberCorrect", originalCorrection);
        Cultivation.setTizhiDouble(p, "jishiqi", originalTimer);
        Cultivation.setTizhiBoolean(p, "yvanyingchuqiao", originalOut);
        p.inventoryMenu.broadcastChanges();
    }

    private static void discardBody(ServerLevel level, UUID bodyId) {
        if (bodyId != null && level.getEntity(bodyId) instanceof Yuanying body) body.discard();
    }
}
