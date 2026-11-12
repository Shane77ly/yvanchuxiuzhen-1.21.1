package net.yvanchuxiuzhen.system.skill.entity;

import java.util.Comparator;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.entity.AnimatedLook;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.combat.api.FightingState;
import net.yvanchuxiuzhen.system.particle.api.Particles;
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
public final class Yuanying extends PathfinderMob implements GeoEntity, AnimatedLook {
    public static final String ID = "yvanyingchuqiao";
    private static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(Yuanying.class, EntityDataSerializers.STRING);
    private final EntityModelRow model;
    private final AnimatableInstanceCache animations = GeckoLibUtil.createInstanceCache(this);
    private final SkillAnimations animationState;
    private UUID ownerUuid;

    public Yuanying(EntityType<Yuanying> type, Level level, EntityModelRow model) {
        super(type, level);
        this.model = model;
        animationState = new SkillAnimations(model.lifecycle().animationTransitionTicks());
        xpReward = 0;
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder attributes(EntityModelRow model) {
        return model.attributes();
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
        body.setYBodyRot(body.getYRot());
        body.setYHeadRot(body.getYRot());
        body.yBodyRotO = body.getYRot();
        body.yHeadRotO = body.getYRot();
        String playerId = player.getStringUUID();
        String bodyId = body.getStringUUID();
        Vec3 center = player.position();
        for (Entity other : level.getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(16)).stream()
                .sorted(Comparator.comparingDouble(entity -> entity.distanceToSqr(center))).toList()) {
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
        body.finalizeSpawn(level, level.getCurrentDifficultyAt(body.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
        level.addFreshEntity(body);
    }

    /** 技能按钮：未出窍时开始，已出窍时返回本体。 */
    public static void toggle(ServerPlayer player) {
        if (Cultivation.thunderOpen(player)) {
            player.displayClientMessage(Component.literal("渡劫时无法出窍！").withStyle(ChatFormatting.DARK_RED), false);
            return;
        }
        if (Cultivation.get(player, CultivationValue.LEVEL_NUMBER) + Cultivation.tizhiDouble(player, "LevelNumberCorrect") < 31) {
            player.displayClientMessage(Component.literal("必须达到元婴期才可施展元婴出窍！").withStyle(ChatFormatting.DARK_RED), false);
            return;
        }
        if (player.level().dimension().location().equals(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "fabao_009weidu"))) {
            player.displayClientMessage(Component.literal("受到强大的界面压制，无法元婴出窍！").withStyle(ChatFormatting.DARK_RED), false);
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
            player.displayClientMessage(Component.literal("精力不足，无法元婴出窍！").withStyle(ChatFormatting.DARK_RED), false);
            return;
        }
        Cultivation.add(player, CultivationValue.SOUL, -cost);
        Cultivation.setTizhiDouble(player, "chuqiaoX", player.getX());
        Cultivation.setTizhiDouble(player, "chuqiaoY", player.getY());
        Cultivation.setTizhiDouble(player, "chuqiaoZ", player.getZ());
        Cultivation.setTizhiString(player, "chuqiaoqianzhuangtai", player.gameMode.getGameModeForPlayer().getName());
        player.serverLevel().sendParticles(Particles.type("starblue"), player.getX(), player.getY() + player.getBbHeight(), player.getZ(), 20, 0, 0, 0, 0.5);
        player.serverLevel().sendParticles(Particles.type("starskyblue"), player.getX(), player.getY() + player.getBbHeight(), player.getZ(), 10, 0, 0, 0, 0.5);
        spawn(player);
        player.setGameMode(GameType.SPECTATOR);
        Cultivation.setTizhiBoolean(player, "yvanyingchuqiao", true);
        player.closeContainer();
    }

    /** 出窍期间每刻在玩家脚下发出来源的两枚紫烟粒子。 */
    public static void playerTick(ServerPlayer player) {
        if (Cultivation.tizhiBoolean(player, "yvanyingchuqiao")) {
            player.serverLevel().sendParticles(Particles.type("smokedeeppurple"), player.getX(), player.getY(), player.getZ(), 2, 0.2, 0.2, 0.2, 0);
        }
    }

    /** 玩家在出窍状态下被有来源实体的伤害杀死时先归窍，再额外扣除一年寿元。元婴本体死亡已先清除出窍标记。 */
    public static void playerDied(ServerPlayer player) {
        if (Cultivation.tizhiBoolean(player, "yvanyingchuqiao")) {
            returnToBody(player);
            Cultivation.add(player, CultivationValue.LIFETIME, -365);
            player.displayClientMessage(Component.literal("因处于出窍状态下暴毙，所以额外扣除一年寿元！").withStyle(ChatFormatting.DARK_RED), false);
        }
    }

    /** 在修炼每二十刻事件中扣除出窍距离与环境消耗；神识不足时清战斗状态并归窍。 */
    public static void tick(ServerPlayer player) {
        if (!Cultivation.tizhiBoolean(player, "yvanyingchuqiao")) {
            return;
        }
        double distance = Math.sqrt(Math.pow(player.getX() - Cultivation.tizhiDouble(player, "chuqiaoX"), 2)
                + Math.pow(player.getY() - Cultivation.tizhiDouble(player, "chuqiaoY"), 2)
                + Math.pow(player.getZ() - Cultivation.tizhiDouble(player, "chuqiaoZ"), 2));
        double obstruction = player.level().isEmptyBlock(BlockPos.containing(player.position())) ? 0 : Cultivation.get(player, CultivationValue.LEVEL_NUMBER) * 3;
        double cost = Cultivation.get(player, CultivationValue.LEVEL_NUMBER) + obstruction + distance;
        FightingState.mark(player);
        if (!player.isCreative()) {
            if (Cultivation.get(player, CultivationValue.SOUL) < cost) {
                FightingState.clear(player);
                returnToBody(player);
            } else {
                player.setPortalCooldown(40);
                Cultivation.add(player, CultivationValue.SOUL, -cost);
                if (Cultivation.get(player, CultivationValue.SOUL) < cost) {
                    FightingState.clear(player);
                    returnToBody(player);
                }
            }
        }
        player.displayClientMessage(Component.literal(Cultivation.get(player, CultivationValue.SOUL) + "/"
                + PlayerAttributes.value(player, Stat.SOUL_MAX)), true);
    }

    static void returnToBody(ServerPlayer player) {
        double x = Cultivation.tizhiDouble(player, "chuqiaoX");
        double y = Cultivation.tizhiDouble(player, "chuqiaoY");
        double z = Cultivation.tizhiDouble(player, "chuqiaoZ");
        player.teleportTo(player.serverLevel(), x, y, z, player.getYRot(), player.getXRot());
        player.connection.teleport(x, y, z, player.getYRot(), player.getXRot());
        Vec3 center = player.position();
        for (Yuanying body : player.serverLevel().getEntitiesOfClass(Yuanying.class, new AABB(center, center).inflate(2)).stream()
                .sorted(Comparator.comparingDouble(entity -> entity.distanceToSqr(center))).toList()) {
            if (player.getUUID().equals(body.ownerUuid)) {
                body.discard();
                break;
            }
        }
        Cultivation.setTizhiBoolean(player, "yvanyingchuqiao", false);
        restoreMode(player);
    }

    private ServerPlayer owner() {
        if (level() instanceof ServerLevel level) {
            for (ServerPlayer player : level.players()) {
                if (player.getUUID().equals(ownerUuid)) {
                    return player;
                }
            }
        }
        return null;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        ServerPlayer player = owner();
        if (player != null) {
            player.setHealth(getHealth());
            player.displayClientMessage(Component.literal("您感觉到本体正在遭受攻击！").withStyle(ChatFormatting.DARK_RED), true);
        }
        return super.hurt(source, amount);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (level() instanceof ServerLevel level) {
            level.sendParticles(Particles.type("brokendarkblue"), getX(), getY(), getZ(), 20, 0, 0, 0, 0.5);
        }
        ServerPlayer player = owner();
        if (player != null) {
            Cultivation.setTizhiBoolean(player, "yvanyingchuqiao", false);
            restoreMode(player);
            player.displayClientMessage(Component.literal("您的本体已死亡！").withStyle(ChatFormatting.DARK_RED), true);
            player.kill();
        }
    }

    @Override
    public void baseTick() {
        super.baseTick();
        refreshDimensions();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        updateSwingTime();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.GENERIC_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GENERIC_DEATH;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ANIMATION, SkillAnimations.NO_REQUEST);
    }

    public void setAnimation(String animation) {
        entityData.set(ANIMATION, animation);
    }

    @Override
    public void tick() {
        animationState.tick(entityData, ANIMATION);
        super.tick();
    }

    @Override
    public String texture() {
        return model.texture();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        animationState.register(controllers, this, model);
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

    private static void restoreMode(ServerPlayer player) {
        switch (Cultivation.tizhiString(player, "chuqiaoqianzhuangtai")) {
            case "creative" -> player.setGameMode(GameType.CREATIVE);
            case "adventure" -> player.setGameMode(GameType.ADVENTURE);
            case "survival" -> player.setGameMode(GameType.SURVIVAL);
        }
    }
}
