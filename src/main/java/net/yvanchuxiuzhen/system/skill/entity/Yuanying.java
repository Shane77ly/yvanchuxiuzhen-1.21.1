package net.yvanchuxiuzhen.system.skill.entity;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.skill.SkillSetup;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * 元婴出窍实体（来源 {@code YvanyingchuqiaoEntity}）：记录本体 UUID，受伤时把本体生命同步到受击前的元婴生命，
 * 被杀时结束出窍并杀死本体。出窍条件、神识消耗、返回位置和游戏模式由技能入口维护。
 */
public final class Yuanying extends PathfinderMob implements GeoEntity, SkillLook {
    public static final String ID = "yvanyingchuqiao";
    private static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(Yuanying.class, EntityDataSerializers.STRING);
    private final EntityModelRow model;
    private final AnimatableInstanceCache animations = GeckoLibUtil.createInstanceCache(this);
    private UUID ownerUuid;

    public Yuanying(EntityType<Yuanying> type, Level level, EntityModelRow model) {
        super(type, level);
        this.model = model;
        xpReward = 0;
        setPersistenceRequired();
        moveControl = new FlyingMoveControl(this, 10, true);
    }

    public static AttributeSupplier.Builder attributes(EntityModelRow model) {
        return model.attributes().add(Attributes.FLYING_SPEED, 0.3);
    }

    public UUID ownerId() {
        return ownerUuid;
    }

    /** 在本体位置生成元婴，保存本体生命与 UUID，并把附近正在攻击本体的生物改为攻击元婴。 */
    static void spawn(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Yuanying body = SkillSetup.<Yuanying>entityType(ID).get().create(level);
        body.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), 0);
        body.ownerUuid = player.getUUID();
        Relations.copyAllegiance(player, body);
        double healthMax = PlayerAttributes.value(player, Stat.HEALTH_MAX);
        body.getAttribute(Attributes.MAX_HEALTH).setBaseValue(healthMax);
        body.setHealth((float) Math.min(player.getHealth(), healthMax));
        body.getPersistentData().putDouble("health_Max", healthMax);
        body.getPersistentData().putDouble("attack_Def", PlayerAttributes.value(player, Stat.ATTACK_DEF));
        body.getPersistentData().putDouble("mana_Def", PlayerAttributes.value(player, Stat.MANA_DEF));
        body.getPersistentData().putDouble("soul_Def", PlayerAttributes.value(player, Stat.SOUL_DEF));
        body.finalizeSpawn(level, level.getCurrentDifficultyAt(body.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
        level.addFreshEntity(body);
        String playerId = player.getStringUUID();
        String bodyId = body.getStringUUID();
        for (Entity other : level.getEntitiesOfClass(Entity.class, new AABB(player.blockPosition()).inflate(16))) {
            if (other instanceof Mob mob) {
                if (mob.getTarget() != null) {
                    if (mob.getTarget() == player) {
                        mob.setTarget(body);
                    }
                } else if (playerId.equals(mob.getPersistentData().getString("gongjizhe"))) {
                    mob.getPersistentData().putString("linshigongjizhe", bodyId);
                    mob.getPersistentData().putString("gongjizhe", bodyId);
                }
            }
        }
    }

    /** 技能按钮：未出窍时开始，已出窍时返回本体。 */
    public static void toggle(ServerPlayer player) {
        if (Cultivation.thunderOpen(player)) {
            player.displayClientMessage(Component.literal("渡劫时无法出窍！"), false);
            return;
        }
        if (Cultivation.get(player, CultivationValue.LEVEL_NUMBER) + Cultivation.tizhiDouble(player, "LevelNumberCorrect") < 31) {
            player.displayClientMessage(Component.literal("必须达到元婴期才可施展元婴出窍！"), false);
            return;
        }
        if (player.level().dimension().location().equals(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "fabao_009weidu"))) {
            player.displayClientMessage(Component.literal("受到强大的界面压制，无法元婴出窍！"), false);
            return;
        }
        if (Cultivation.tizhiBoolean(player, "yvanyingchuqiao")) {
            returnToBody(player);
            return;
        }
        if (player.gameMode.getGameModeForPlayer() == GameType.SPECTATOR) {
            return;
        }
        double cost = Cultivation.get(player, CultivationValue.LEVEL_NUMBER) * 2;
        if (!player.isCreative() && Cultivation.get(player, CultivationValue.SOUL) < cost) {
            player.displayClientMessage(Component.literal("精力不足，无法元婴出窍！"), false);
            return;
        }
        if (!player.isCreative()) {
            Cultivation.add(player, CultivationValue.SOUL, -cost);
        }
        Cultivation.setTizhiDouble(player, "chuqiaoX", player.getX());
        Cultivation.setTizhiDouble(player, "chuqiaoY", player.getY());
        Cultivation.setTizhiDouble(player, "chuqiaoZ", player.getZ());
        Cultivation.setTizhiString(player, "chuqiaoqianzhuangtai", mode(player.gameMode.getGameModeForPlayer()));
        spawn(player);
        player.setGameMode(GameType.SPECTATOR);
        Cultivation.setTizhiBoolean(player, "yvanyingchuqiao", true);
        player.closeContainer();
    }

    /** 每刻扣除出窍移动与环境消耗；神识不足时按来源立即归窍。 */
    public static void tick(ServerPlayer player) {
        if (!Cultivation.tizhiBoolean(player, "yvanyingchuqiao")) {
            return;
        }
        double distance = Math.sqrt(Math.pow(player.getX() - Cultivation.tizhiDouble(player, "chuqiaoX"), 2)
                + Math.pow(player.getY() - Cultivation.tizhiDouble(player, "chuqiaoY"), 2)
                + Math.pow(player.getZ() - Cultivation.tizhiDouble(player, "chuqiaoZ"), 2));
        double obstruction = player.level().isEmptyBlock(BlockPos.containing(player.position())) ? 0 : Cultivation.get(player, CultivationValue.LEVEL_NUMBER) * 3;
        double cost = Cultivation.get(player, CultivationValue.LEVEL_NUMBER) + obstruction + distance;
        if (!player.isCreative()) {
            if (Cultivation.get(player, CultivationValue.SOUL) < cost) {
                returnToBody(player);
            } else {
                player.setPortalCooldown(40);
                Cultivation.add(player, CultivationValue.SOUL, -cost);
                if (Cultivation.get(player, CultivationValue.SOUL) < cost) {
                    returnToBody(player);
                }
            }
        }
        player.displayClientMessage(Component.literal(Cultivation.get(player, CultivationValue.SOUL) + "/"
                + PlayerAttributes.value(player, Stat.SOUL_MAX)), true);
    }

    static void returnToBody(ServerPlayer player) {
        player.connection.teleport(Cultivation.tizhiDouble(player, "chuqiaoX"), Cultivation.tizhiDouble(player, "chuqiaoY"),
                Cultivation.tizhiDouble(player, "chuqiaoZ"), player.getYRot(), player.getXRot());
        for (Yuanying body : player.serverLevel().getEntitiesOfClass(Yuanying.class, new AABB(player.blockPosition()).inflate(2))) {
            if (player.getUUID().equals(body.ownerUuid)) {
                body.discard();
                break;
            }
        }
        Cultivation.setTizhiBoolean(player, "yvanyingchuqiao", false);
        player.setGameMode(mode(Cultivation.tizhiString(player, "chuqiaoqianzhuangtai")));
    }

    private ServerPlayer owner() {
        return ownerUuid == null || !(level() instanceof ServerLevel level) ? null : level.getServer().getPlayerList().getPlayer(ownerUuid);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        ServerPlayer player = owner();
        if (player != null) {
            player.setHealth(getHealth());
            player.displayClientMessage(Component.literal("您感觉到本体正在遭受攻击！"), true);
        }
        return super.hurt(source, amount);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        ServerPlayer player = owner();
        if (player != null) {
            Cultivation.setTizhiBoolean(player, "yvanyingchuqiao", false);
            player.setGameMode(mode(Cultivation.tizhiString(player, "chuqiaoqianzhuangtai")));
            player.displayClientMessage(Component.literal("您的本体已死亡！"), true);
            player.kill();
        }
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new FlyingPathNavigation(this, level);
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double movement, boolean onGround, BlockState block, BlockPos pos) {
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ANIMATION, SkillAnimations.NONE);
    }

    public void setAnimation(String animation) {
        entityData.set(ANIMATION, animation);
    }

    @Override
    public String texture() {
        return model.texture();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        SkillAnimations.register(controllers, this, model, () -> entityData.get(ANIMATION));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animations;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("texture", texture());
        if (ownerUuid != null) {
            tag.putUUID("owner", ownerUuid);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("owner")) {
            ownerUuid = tag.getUUID("owner");
        }
    }

    private static String mode(GameType gameType) {
        return gameType.getName();
    }

    private static GameType mode(String name) {
        return switch (name) {
            case "creative" -> GameType.CREATIVE;
            case "adventure" -> GameType.ADVENTURE;
            case "spectator" -> GameType.SPECTATOR;
            default -> GameType.SURVIVAL;
        };
    }
}
