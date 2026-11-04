package net.yvanchuxiuzhen.system.skill.entity;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * 技能动画实体（来源 MCreator 动画实体 {@code SkillXxxEntity}，35 种共用）：无敌的占位生物，不被推挤，不接受药水云、药水弹、跌落、仙人掌、
 * 溺水的伤害，死亡 20 刻后移除。每种实体的生命、移速等属性与模型来自 {@link EntityModelRow}，行为由 {@link EntitySkill} 承担。
 * 来源在 {@code getPersistentData} 里放的施放信息（{@code AttackUUID}、{@code AttackDamage}、{@code Damage_multiplier}、{@code damagetype}、
 * {@code jinengName}）改为字段，其余各技能私有的计数与标记仍用 {@code getPersistentData}。纹理名同步给客户端（默认透明的 {@code kong}），
 * 过程动画 {@link #setAnimation} 同步名字给客户端，由 GeckoLib 的 {@code procedure} 控制器播放一次，之后回到默认循环动画。
 */
public final class SkillEntity extends PathfinderMob implements GeoEntity {
    public static final String DEFAULT_TEXTURE = "kong";
    private static final String NO_ANIMATION = "empty";
    private static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(SkillEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(SkillEntity.class, EntityDataSerializers.STRING);

    private final EntityModelRow model;
    private final EntitySkill skill;
    private final AnimatableInstanceCache animations = GeckoLibUtil.createInstanceCache(this);
    private UUID attacker;
    private double attackDamage;
    private double multiplier;
    private String damageType = "";
    private String skillName = "";

    public SkillEntity(EntityType<SkillEntity> type, Level level, EntityModelRow model, EntitySkill skill) {
        super(type, level);
        this.model = model;
        this.skill = skill;
        xpReward = 0;
        setNoAi(model.noAi());
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes(EntityModelRow model) {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, model.speed()).add(Attributes.MAX_HEALTH, model.health())
                .add(Attributes.ARMOR, model.armor()).add(Attributes.ATTACK_DAMAGE, model.attack()).add(Attributes.FOLLOW_RANGE, model.follow());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TEXTURE, DEFAULT_TEXTURE);
        builder.define(ANIMATION, NO_ANIMATION);
    }

    public String texture() {
        return entityData.get(TEXTURE);
    }

    public void setTexture(String texture) {
        entityData.set(TEXTURE, texture);
    }

    /**
     * 设置过程动画（来源 {@code setAnimation}）：同步给客户端播放一次，随后回到默认循环动画。放入世界之前设置也有效；
     * 与当前值相同的名字不会再次播放（来源如此）。
     */
    public void setAnimation(String animation) {
        entityData.set(ANIMATION, animation);
    }

    /** 记下施放信息：攻击者、攻击量、伤害倍率、伤害类别标签与技能名（来源各 {@code …shifang} 写入的几个键）。 */
    public void configure(Entity attacker, double attackDamage, double multiplier, String damageType, String skillName) {
        this.attacker = attacker.getUUID();
        this.attackDamage = attackDamage;
        this.multiplier = multiplier;
        this.damageType = damageType;
        this.skillName = skillName;
    }

    /** 攻击者；已离线或不在本维度时为空。 */
    public Entity attackerEntity() {
        return attacker == null || !(level() instanceof ServerLevel server) ? null : server.getEntity(attacker);
    }

    public UUID attacker() {
        return attacker;
    }

    public double attackDamage() {
        return attackDamage;
    }

    public double multiplier() {
        return multiplier;
    }

    public String damageType() {
        return damageType;
    }

    public String skillName() {
        return skillName;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entity) {
    }

    @Override
    protected void pushEntities() {
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getDirectEntity() instanceof ThrownPotion || source.getDirectEntity() instanceof AreaEffectCloud || source.is(DamageTypes.FALL)
                || source.is(DamageTypes.CACTUS) || source.is(DamageTypes.DROWN)) {
            return false;
        }
        boolean hurt = super.hurt(source, amount);
        if (hurt && !level().isClientSide()) {
            skill.hurt(this, source, amount);
        }
        return hurt;
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!level().isClientSide()) {
            skill.died(this, source);
        }
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, SpawnGroupData data) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, spawnType, data);
        skill.spawned(this);
        return result;
    }

    @Override
    public void baseTick() {
        super.baseTick();
        if (!level().isClientSide()) {
            skill.tick(this);
        }
    }

    @Override
    protected void tickDeath() {
        ++deathTime;
        if (deathTime == 20) {
            remove(RemovalReason.KILLED);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("texture", texture());
        tag.putDouble("attack_damage", attackDamage);
        tag.putDouble("multiplier", multiplier);
        tag.putString("damage_type", damageType);
        tag.putString("skill_name", skillName);
        if (attacker != null) {
            tag.putUUID("attacker", attacker);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setTexture(tag.getString("texture"));
        attackDamage = tag.getDouble("attack_damage");
        multiplier = tag.getDouble("multiplier");
        damageType = tag.getString("damage_type");
        skillName = tag.getString("skill_name");
        attacker = tag.hasUUID("attacker") ? tag.getUUID("attacker") : null;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<SkillEntity>(this, "main", 0, state -> {
            String loop = model.air().isPresent() && !onGround() ? model.air().get() : model.idle();
            return state.setAndContinue(RawAnimation.begin().thenLoop(loop));
        }));
        controllers.add(new AnimationController<SkillEntity>(this, "procedure", 0, state -> {
            String requested = entityData.get(ANIMATION);
            return requested.equals(NO_ANIMATION) ? PlayState.STOP : state.setAndContinue(RawAnimation.begin().thenPlay(requested));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animations;
    }
}
