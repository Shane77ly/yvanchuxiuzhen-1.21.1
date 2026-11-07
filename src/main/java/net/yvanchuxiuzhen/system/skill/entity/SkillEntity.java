package net.yvanchuxiuzhen.system.skill.entity;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.FluidType;
import net.yvanchuxiuzhen.platform.content.entity.AnimatedLook;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * 技能动画实体的公共实现（来源 MCreator 动画实体 {@code SkillXxxEntity} 等）：不被推挤；每种实体的属性、导航、固定无重力、
 * 伤害免疫与死亡移除刻数来自 {@link EntityModelRow}，行为由 {@link EntitySkill} 承担。
 * 来源在 {@code getPersistentData} 里放的施放信息（{@code AttackUUID}、{@code AttackDamage}、{@code Damage_multiplier}、{@code damagetype}、
 * {@code jinengName}）改为字段，其余各技能私有的计数与标记仍用 {@code getPersistentData}。纹理名同步给客户端（默认值是该实体在来源里定义的纹理），
 * 过程动画 {@link #setAnimation} 同步名字给客户端，由 GeckoLib 的 {@code procedure} 控制器播放一次，之后回到默认循环动画。
 */
public final class SkillEntity extends PathfinderMob implements GeoEntity, AnimatedLook {
    private static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(SkillEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(SkillEntity.class, EntityDataSerializers.STRING);

    private final EntityModelRow model;
    private final EntitySkill skill;
    private final AnimatableInstanceCache animations = GeckoLibUtil.createInstanceCache(this);
    private final SkillAnimations animationState;
    private final SkillCast cast = new SkillCast();
    private final boolean fixedNoGravity;
    private float hitboxScale = 1;

    public SkillEntity(EntityType<SkillEntity> type, Level level, EntityModelRow model, EntitySkill skill) {
        super(type, level);
        this.model = model;
        this.skill = skill;
        animationState = new SkillAnimations(model.lifecycle().animationTransitionTicks());
        fixedNoGravity = model.lifecycle().noGravity();
        switch (model.lifecycle().navigation()) {
            case GROUND -> { }
            case WATER -> {
                navigation = new WaterBoundPathNavigation(this, level);
                setPathfindingMalus(PathType.WATER, 0);
                moveControl = new SkillWaterMoveControl(this);
            }
            case FLYING -> {
                navigation = new FlyingPathNavigation(this, level);
                moveControl = new FlyingMoveControl(this, 10, true);
            }
        }
        xpReward = 0;
        setNoAi(model.noAi());
        setPersistenceRequired();
        setTexture(model.texture());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TEXTURE, "");
        builder.define(ANIMATION, SkillAnimations.NO_REQUEST);
    }

    @Override
    public String texture() {
        return entityData.get(TEXTURE);
    }

    public void setTexture(String texture) {
        entityData.set(TEXTURE, texture);
    }

    /**
     * 设置待消费的过程动画请求；每刻消费后同步字段复位，过程播放一次后恢复默认动画。
     */
    public void setAnimation(String animation) {
        entityData.set(ANIMATION, animation);
    }

    @Override
    public void tick() {
        animationState.tick(entityData, ANIMATION);
        super.tick();
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

    /** 记下碰撞箱比例；按来源在每次基础刻的技能过程之后刷新尺寸。 */
    public void setHitboxScale(float scale) {
        hitboxScale = scale;
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
    public boolean canDrownInFluidType(FluidType type) {
        return !(type == NeoForgeMod.WATER_TYPE.value() && model.lifecycle().breathesUnderwater()) && super.canDrownInFluidType(type);
    }

    @Override
    public boolean isPushedByFluid() {
        return model.lifecycle().pushedByFluid() && super.isPushedByFluid();
    }

    @Override
    public void setNoGravity(boolean noGravity) {
        super.setNoGravity(fixedNoGravity || noGravity);
    }

    /** 来源飞行实体在原生移动之后保持无重力，地面与水中导航实体保留技能过程设置的重力。 */
    @Override
    public void aiStep() {
        super.aiStep();
        updateSwingTime();
        if (fixedNoGravity) {
            setNoGravity(true);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return !SkillImmunity.rejectsDamage(model.lifecycle().damageImmunities(), source) && super.hurt(source, amount);
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return !model.lifecycle().ignoresFallDamage() && super.causeFallDamage(distance, multiplier, source);
    }

    @Override
    protected void checkFallDamage(double movement, boolean onGround, BlockState block, BlockPos pos) {
        if (!model.lifecycle().ignoresFallDamage()) {
            super.checkFallDamage(movement, onGround, block, pos);
        }
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
        refreshDimensions();
    }

    @Override
    protected void tickDeath() {
        ++deathTime;
        if (deathTime == model.lifecycle().deathTicks()) {
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
        animationState.register(controllers, this, model);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animations;
    }
}
