package net.yvanchuxiuzhen.smoke;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/** 天魔002的来源初生、自然索敌、冲刺、拒伤边界、真实重进和二十刻死亡检查。 */
final class Moshou002Check extends SystemCheck {
    private static final String MOD = "yvanchuxiuzhen";
    private static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(MOD, "moshou_002");
    private final Screenshots screenshots;
    private int executed = -1;
    private Mob entity;
    private EntityType<?> entityType;
    private UUID entityUuid;
    private int entityId;
    private boolean prepared;
    private boolean naturalReady;
    private boolean resourcesChecked;
    private boolean rendererChecked;
    private boolean screenshotTaken;
    private Vec3 impulseDirection;
    private long impulseStart;
    private double impulseDot;
    private volatile boolean impulseObserved;
    private volatile boolean deathObserved;
    private boolean deathStarted;
    private boolean deathNineteenSeen;
    private boolean deathRemovalMatches;
    private boolean observerRegistered;
    private final Consumer<ServerTickEvent.Post> tickObserver = this::observeServerTick;
    private final Consumer<LivingIncomingDamageEvent> playerProtection = this::protectTargetPlayer;
    private Vec3 originalPosition;
    private Vec3 originalMovement;
    private float originalYaw;
    private float originalPitch;
    private Difficulty originalDifficulty;
    private GameType originalGameMode;
    private boolean originalInvulnerable;
    private boolean originalNoGravity;
    private float savedHealth;
    private String savedLevelName;
    private double savedLevelNumber;
    private double savedFlyingSpeed;

    Moshou002Check(List<String> report, String levelId, Screenshots screenshots) {
        super("天魔", report, levelId);
        this.screenshots = screenshots;
    }

    @Override
    protected boolean step(int step, int local) {
        return switch (step) {
            case 0 -> {
                if (!resourcesChecked) {
                    resourcesChecked = true;
                    checkClientResources();
                }
                yield action(step, local, 2, this::prepare);
            }
            case 1 -> naturalTarget(local);
            case 2 -> controlledFortiethTick(local);
            case 3 -> action(step, local, 2, this::fallAndSerialize);
            case 4 -> beforeSave(step, local);
            case 5 -> {
                onServer(player -> unregisterObserver());
                leaveWorld();
                yield true;
            }
            case 6 -> reopened(local);
            case 7 -> reopenedEntity(local);
            case 8 -> action(step, local, 2, this::startDeath);
            case 9 -> waitForDeath(local);
            case 10 -> {
                onServer(this::cleanup);
                finish();
                yield true;
            }
            default -> throw new IllegalStateException("未知天魔检查步骤 " + step);
        };
    }

    private boolean action(int step, int local, int wait, Consumer<ServerPlayer> run) {
        if (executed != step) {
            executed = step;
            onServer(run);
        }
        return local >= wait;
    }

    private void checkClientResources() {
        expectTrue("实体类型已登记", BuiltInRegistries.ENTITY_TYPE.containsKey(ID));
        expect("来源中文显示名", "天魔", I18n.get("entity.yvanchuxiuzhen.moshou_002"));
        expect("来源刷怪蛋中文显示名", "天魔", I18n.get("item.yvanchuxiuzhen.moshou_002_spawn_egg"));
        ResourceLocation eggId = ResourceLocation.fromNamespaceAndPath(MOD, "moshou_002_spawn_egg");
        expectTrue("来源刷怪蛋工厂指向同一注册实体",
                BuiltInRegistries.ITEM.get(eggId) instanceof SpawnEggItem egg
                        && egg.getType(ItemStack.EMPTY) == BuiltInRegistries.ENTITY_TYPE.get(ID));
        expectTrue("几何模型资源已加载", hasResource("geo/moshou002.geo.json"));
        expectTrue("动作资源已加载", hasResource("animations/moshou002.animation.json"));
        expectTrue("实体纹理资源已加载", hasResource("textures/entities/moshou002.png"));
        expectTrue("刷怪蛋模型资源已加载", hasResource("models/item/moshou_002_spawn_egg.json"));
    }

    private boolean hasResource(String path) {
        ResourceLocation resource = ResourceLocation.fromNamespaceAndPath(MOD, path);
        return minecraft.getResourceManager().getResource(resource).isPresent();
    }

    private void prepare(ServerPlayer player) {
        if (!BuiltInRegistries.ENTITY_TYPE.containsKey(ID)) {
            finish();
            return;
        }
        entityType = BuiltInRegistries.ENTITY_TYPE.get(ID);
        Entity created = entityType.create(player.serverLevel());
        if (!(created instanceof Mob mob)) {
            fail("注册表无法创建活动魔兽实体");
            finish();
            return;
        }

        originalPosition = player.position();
        originalMovement = player.getDeltaMovement();
        originalYaw = player.getYRot();
        originalPitch = player.getXRot();
        originalDifficulty = player.serverLevel().getDifficulty();
        originalGameMode = player.gameMode.getGameModeForPlayer();
        originalInvulnerable = player.isInvulnerable();
        originalNoGravity = player.isNoGravity();

        player.server.setDifficulty(Difficulty.NORMAL, true);
        player.setGameMode(GameType.SURVIVAL);
        player.setInvulnerable(false);
        player.setNoGravity(true);
        player.setDeltaMovement(Vec3.ZERO);

        Vec3 horizontal = new Vec3(player.getLookAngle().x, 0, player.getLookAngle().z);
        if (horizontal.lengthSqr() < 1.0e-6) {
            horizontal = new Vec3(0, 0, 1);
        } else {
            horizontal = horizontal.normalize();
        }
        Vec3 spawn = player.position().add(horizontal.scale(6)).add(0, 0.2, 0);
        mob.moveTo(spawn.x, spawn.y, spawn.z, player.getYRot() + 180, 0);
        mob.finalizeSpawn(player.serverLevel(), player.serverLevel().getCurrentDifficultyAt(mob.blockPosition()),
                MobSpawnType.MOB_SUMMONED, null);
        entity = mob;
        entityUuid = mob.getUUID();
        prepared = player.serverLevel().addFreshEntity(mob);
        expectTrue("注册实体已进入服务端世界", prepared);
        if (!prepared) {
            cleanup(player);
            finish();
            return;
        }
        entityId = mob.getId();
        registerObserver();

        CompoundTag data = mob.getPersistentData();
        expect("来源默认等级", 20, data.getDouble("LEVELnumber"));
        expect("来源初生境界", "四阶圆满", data.getString("LEVELname"));
        expect("来源气血资质", "强劲", data.getString("health_Max_LEVEL"));
        expect("来源气血恢复资质", "平庸", data.getString("health_Rec_LEVEL"));
        expect("来源灵力资质", "平庸", data.getString("power_Max_LEVEL"));
        expect("来源灵力恢复资质", "勉强", data.getString("power_Rec_LEVEL"));
        expect("来源神识资质", "贫弱", data.getString("soul_Max_LEVEL"));
        expect("来源神识恢复资质", "贫弱", data.getString("soul_Rec_LEVEL"));
        expect("来源攻击资质", "勉强", data.getString("attack_Num_LEVEL"));
        expect("来源防御资质", "强劲", data.getString("attack_Def_LEVEL"));
        expect("来源法力攻击资质", "勉强", data.getString("mana_Att_LEVEL"));
        expect("来源法力防御资质", "强劲", data.getString("mana_Def_LEVEL"));
        expect("来源神识攻击资质", "贫弱", data.getString("soul_Att_LEVEL"));
        expect("来源神识防御资质", "平庸", data.getString("soul_Def_LEVEL"));
        expect("来源速度资质", "绝佳", data.getString("speed_LEVEL"));
        expect("来源计算生命上限应用到实体属性", data.getDouble("health_Max"),
                mob.getAttributeValue(Attributes.MAX_HEALTH));
        expect("来源初生生命填满计算上限", data.getDouble("health_Max"), mob.getHealth());
        expect("来源计算攻击力应用到实体属性", data.getDouble("attack_Num"),
                mob.getAttributeValue(Attributes.ATTACK_DAMAGE));
        expect("来源计算速度应用到实体属性", data.getDouble("speed_number"),
                mob.getAttributeValue(Attributes.MOVEMENT_SPEED));
        expect("初生飞行速度等于来源速度资质乘三", data.getDouble("speed_number") * 3,
                mob.getAttributeValue(Attributes.FLYING_SPEED));
    }

    private boolean naturalTarget(int local) {
        if (!prepared) {
            return true;
        }
        if (!naturalReady) {
            onServer(player -> {
                if (entity == null || entity.isRemoved() || entity.getTarget() != player
                        || entity.getPersistentData().getDouble("entitytick") <= 0) {
                    return;
                }
                naturalReady = true;
                player.setInvulnerable(true);
                expectTrue("自然AI取得玩家目标并推进来源刻", true);
                expectTrue("自然生成使用飞行移动控制与飞行寻路",
                        entity.getMoveControl() instanceof FlyingMoveControl
                                && entity.getNavigation() instanceof FlyingPathNavigation);
                expectTrue("实体自然保持无重力", entity.isNoGravity());
                entity.setNoAi(true);
                entity.setYRot(90);
                entity.setXRot(0);
                entity.setDeltaMovement(Vec3.ZERO);
                entity.getPersistentData().putDouble("entitytick", 39);
                impulseDirection = entity.getLookAngle();
                impulseStart = entity.level().getGameTime();
            });
        }
        Entity clientEntity = minecraft.level == null ? null : minecraft.level.getEntity(entityId);
        if (clientEntity != null && !rendererChecked) {
            rendererChecked = true;
            expectTrue("客户端已加载天魔动画实体渲染器",
                    minecraft.getEntityRenderDispatcher().getRenderer(clientEntity).getClass().getSimpleName()
                            .equals("AnimatedEntityRenderer"));
        }
        if (naturalReady && rendererChecked) {
            return true;
        }
        if (local > 120) {
            if (!naturalReady) {
                fail("自然索敌或来源tick未在时限内触发");
                onServer(player -> {
                    if (entity != null && !entity.isRemoved()) {
                        entity.setNoAi(true);
                    }
                });
            }
            if (!rendererChecked) {
                fail("客户端未收到实体实例，无法检查渲染器装配");
            }
            return true;
        }
        return false;
    }

    private boolean controlledFortiethTick(int local) {
        if (!naturalReady) {
            return true;
        }
        if (impulseObserved) {
            expectTrue("受控第40刻将速度设为视线方向", impulseDot > 0.5);
            return true;
        }
        if (local > 160) {
            fail("受控第40刻未清零来源计数或未产生视线方向冲刺");
            return true;
        }
        return false;
    }

    private void observeServerTick(ServerTickEvent.Post event) {
        if (!prepared || entity == null || entity.getServer() != event.getServer()) return;
        if (!impulseObserved && impulseDirection != null && entity.level().getGameTime() > impulseStart
                && entity.getPersistentData().getDouble("entitytick") == 0) {
            impulseDot = entity.getDeltaMovement().dot(impulseDirection);
            impulseObserved = true;
        }
        if (!deathStarted || deathObserved) return;
        if (entity.deathTime == 19 && !deathNineteenSeen) {
            deathNineteenSeen = !entity.isRemoved();
        }
        if (entity.isRemoved()) {
            deathRemovalMatches = entity.deathTime == 20
                    && entity.getRemovalReason() == Entity.RemovalReason.KILLED;
            deathObserved = true;
        }
    }

    private void registerObserver() {
        if (!observerRegistered) {
            NeoForge.EVENT_BUS.addListener(tickObserver);
            NeoForge.EVENT_BUS.addListener(playerProtection);
            observerRegistered = true;
        }
    }

    private void unregisterObserver() {
        if (observerRegistered) {
            NeoForge.EVENT_BUS.unregister(tickObserver);
            NeoForge.EVENT_BUS.unregister(playerProtection);
            observerRegistered = false;
        }
    }

    private void protectTargetPlayer(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer && event.getSource().getEntity() == entity) {
            event.setCanceled(true);
        }
    }

    private void fallAndSerialize(ServerPlayer player) {
        if (entity == null || entity.isRemoved()) {
            fail("跌落免疫检查前实体已消失");
            return;
        }
        CompoundTag data = entity.getPersistentData();
        data.putString("jineng", "controlled_test_skill");
        data.putString("jinengjinxing", "controlled_test_phase");
        data.putDouble("entitytick", 7);
        data.putDouble("entitytick2", 3);
        float health = entity.getHealth();
        boolean hurt = entity.hurt(player.damageSources().fall(), 5);
        expectTrue("FALL伤害被免疫", !hurt);
        expect("FALL受击不扣除生命", health, entity.getHealth());
        expectTrue("FALL受击仍打断技能状态", data.getString("jineng").isEmpty()
                && data.getString("jinengjinxing").isEmpty()
                && data.getDouble("entitytick") == 0 && data.getDouble("entitytick2") == 0);
        expectTrue("自然落地回调入口拒伤且生命不变",
                !entity.causeFallDamage(24, 1, entity.damageSources().fall()) && entity.getHealth() == health);
        entity.invulnerableTime = 0;
        expectTrue("同属IS_FALL标签的石笋伤害仍进入原生扣血",
                entity.hurt(entity.damageSources().stalagmite(), 5) && entity.getHealth() < health);
        entity.setHealth(health);
        entity.setTarget(null);
        entity.setDeltaMovement(Vec3.ZERO);

        savedHealth = entity.getHealth();
        savedLevelName = data.getString("LEVELname");
        savedLevelNumber = data.getDouble("LEVELnumber");
        savedFlyingSpeed = entity.getAttributeValue(Attributes.FLYING_SPEED);
        CompoundTag saved = entity.saveWithoutId(new CompoundTag());
        Entity restored = entityType.create(entity.level());
        if (!(restored instanceof Mob restoredMob)) {
            fail("同一注册类型无法创建序列化读回实例");
            return;
        }
        restoredMob.load(saved.copy());
        CompoundTag restoredData = restoredMob.getPersistentData();
        expectTrue("实体NBT读回保留身份与来源属性", restoredMob.getType() == entity.getType()
                && restoredData.getString("LEVELname").equals(savedLevelName)
                && restoredData.getDouble("LEVELnumber") == savedLevelNumber
                && Math.abs(restoredMob.getAttributeValue(Attributes.FLYING_SPEED) - savedFlyingSpeed) < 1.0e-6);
        expect("实体NBT读回保留生命", savedHealth, restoredMob.getHealth());
        expectTrue("实体NBT读回保留跌落后打断状态", restoredData.getString("jineng").isEmpty()
                && restoredData.getDouble("entitytick") == 0 && restoredData.getDouble("entitytick2") == 0);
        restoredMob.discard();
    }

    private boolean reopenedEntity(int local) {
        if (!inWorld(local)) {
            return false;
        }
        onServer(player -> {
            Entity loaded = player.serverLevel().getEntity(entityUuid);
            entity = loaded instanceof Mob mob ? mob : null;
            expectTrue("真实退出重进恢复同一天魔实体", entity != null && entity.getUUID().equals(entityUuid)
                    && BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).equals(ID));
            if (entity != null) {
                CompoundTag data = entity.getPersistentData();
                expect("真实重进保留来源姓名", savedLevelName, data.getString("LEVELname"));
                expect("真实重进保留来源等级", savedLevelNumber, data.getDouble("LEVELnumber"));
                expect("真实重进保留生命", savedHealth, entity.getHealth());
                expectTrue("真实重进保留技能受击后的清理状态", data.getString("jineng").isEmpty()
                        && data.getDouble("entitytick2") == 0);
            }
        });
        return true;
    }

    private boolean beforeSave(int step, int local) {
        if (executed != step) {
            executed = step;
            onServer(player -> {
                if (entity == null || entity.isRemoved()) return;
                player.connection.teleport(entity.getX(), entity.getY() + 0.2, entity.getZ() + 6, 180, 5);
            });
        }
        if (local < 20) return false;
        if (!screenshotTaken) {
            screenshots.take("r07_moshou002.png");
            screenshotTaken = true;
        }
        return true;
    }

    private void startDeath(ServerPlayer player) {
        if (entity == null || entity.isRemoved()) {
            fail("二十刻死亡检查前实体不可用");
            deathObserved = true;
            return;
        }
        registerObserver();
        entity.setNoAi(true);
        entity.setNoGravity(true);
        entity.setDeltaMovement(Vec3.ZERO);
        entity.invulnerableTime = 0;
        entity.deathTime = 0;
        deathStarted = true;
        expectTrue("原生致死伤害进入死亡生命周期",
                entity.hurt(entity.damageSources().genericKill(), Float.MAX_VALUE) && entity.isDeadOrDying());
    }

    private boolean waitForDeath(int local) {
        if (deathObserved) {
            if (deathStarted) {
                expectTrue("自然死亡第19刻仍保留实体", deathNineteenSeen);
                expectTrue("自然死亡第20刻按KILLED原因移除", deathRemovalMatches);
            }
            return true;
        }
        if (local > 160) {
            fail("实体未在时限内完成二十个自然服务端刻的死亡移除");
            return true;
        }
        return false;
    }

    private void cleanup(ServerPlayer player) {
        unregisterObserver();
        if (entityUuid != null) {
            Entity loaded = player.serverLevel().getEntity(entityUuid);
            if (loaded != null) {
                loaded.discard();
            }
        }
        player.connection.teleport(originalPosition.x, originalPosition.y, originalPosition.z, originalYaw, originalPitch);
        player.setNoGravity(originalNoGravity);
        player.setDeltaMovement(originalMovement);
        player.setInvulnerable(originalInvulnerable);
        player.setGameMode(originalGameMode);
        player.server.setDifficulty(originalDifficulty, true);
    }
}
