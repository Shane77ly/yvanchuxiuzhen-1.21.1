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
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * 技能动画实体（来源 MCreator 动画实体 {@code SkillXxxEntity}，35 种共用）：无敌的占位生物，不被推挤，不接受药水云、药水弹、跌落、仙人掌、
 * 溺水的伤害，死亡 20 刻后移除。每种实体的生命、移速等属性与模型来自 {@link EntityModelRow}，行为由 {@link EntitySkill} 承担。
 * 来源在 {@code getPersistentData} 里放的施放信息（{@code AttackUUID}、{@code AttackDamage}、{@code Damage_multiplier}、{@code damagetype}、
 * {@code jinengName}）改为字段，其余各技能私有的计数与标记仍用 {@code getPersistentData}。纹理名同步给客户端（默认值是该实体在来源里定义的纹理），
 * 过程动画 {@link #setAnimation} 同步名字给客户端，由 GeckoLib 的 {@code procedure} 控制器播放一次，之后回到默认循环动画。
 */
public final class SkillEntity extends PathfinderMob implements GeoEntity, SkillLook {
    private static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(SkillEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(SkillEntity.class, EntityDataSerializers.STRING);

    private final EntityModelRow model;
    private final EntitySkill skill;
    private final AnimatableInstanceCache animations = GeckoLibUtil.createInstanceCache(this);
    private final SkillCast cast = new SkillCast();
    private float hitboxScale = 1;

    public SkillEntity(EntityType<SkillEntity> type, Level level, EntityModelRow model, EntitySkill skill) {
        super(type, level);
        this.model = model;
        this.skill = skill;
        xpReward = 0;
        setNoAi(model.noAi());
        setPersistenceRequired();
        setTexture(model.texture());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TEXTURE, "");
        builder.define(ANIMATION, SkillAnimations.NONE);
    }

    @Override
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
        configure(attacker.getUUID(), attackDamage, multiplier, damageType, skillName);
    }

    public void configure(UUID attacker, double attackDamage, double multiplier, String damageType, String skillName) {
        cast.configure(attacker, attackDamage, multiplier, damageType, skillName);
    }

    public SkillCast cast() {
        return cast;
    }

    /** 按倍数放大碰撞箱（来源重写 {@code getDimensions} 乘以实体里记下的比例）；放入世界之前设置，进入世界时按此尺寸生成。 */
    public void setHitboxScale(float scale) {
        hitboxScale = scale;
        refreshDimensions();
    }

    @Override
    protected EntityDimensions getDefaultDimensions(Pose pose) {
        return super.getDefaultDimensions(pose).scale(hitboxScale);
    }

    /** 攻击者；已离线或不在本维度时为空。 */
    public Entity attackerEntity() {
        return level() instanceof ServerLevel server ? cast.attackerEntity(server) : null;
    }

    public UUID attacker() {
        return cast.attacker();
    }

    public double attackDamage() {
        return cast.attackDamage();
    }

    public double multiplier() {
        return cast.multiplier();
    }

    public String damageType() {
        return cast.damageType();
    }

    public String skillName() {
        return cast.skillName();
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
        cast.save(tag);
        tag.putFloat("hitbox_scale", hitboxScale);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setTexture(tag.getString("texture"));
        cast.load(tag);
        setHitboxScale(tag.contains("hitbox_scale") ? tag.getFloat("hitbox_scale") : 1);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        SkillAnimations.register(controllers, this, model, () -> entityData.get(ANIMATION));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animations;
    }
}
