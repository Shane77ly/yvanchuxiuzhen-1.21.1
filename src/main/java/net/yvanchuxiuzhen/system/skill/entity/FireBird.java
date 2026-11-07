package net.yvanchuxiuzhen.system.skill.entity;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.skill.SkillReleases;
import net.yvanchuxiuzhen.system.skill.SkillSetup;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;
import net.yvanchuxiuzhen.system.skill.projectile.ProjectileSupport;

/**
 * 火鸟术放出的火鸟（来源 {@code SkillFire06Entity}、{@code SkillFire06shifang}、{@code SkillFire06DangShiTiGengXinKeShi}、
 * {@code SkillFire06DangShiTiBeiShaSiShi}）：无重力的飞行实体，跟随主人，主人攻击或被攻击的目标成为它的目标，追上目标后冲撞；
 * 身边 1.5 格内出现非友方实体后 3 刻爆发，对 2.5 格内的非友方实体造成火系伤害（攻击量与倍率取整）并点燃 5 秒，然后消失；
 * 寿命 1200 刻。免疫药水云、药水弹与仙人掌，不受跌落伤害，不会被喂食驯服（见 {@link TamedSkillEntity}）。
 * 来源的爆发粒子 {@code SMALLEXPLOSIONRED} 随 particle 迁移。
 */
public final class FireBird extends TamedSkillEntity {
    public static final String ID = "skill_fire_06";

    private static final String FIRE = "火";
    private static final double FLYING_SPEED = 0.4;
    private static final int LIFETIME = 1200;
    private static final int FLY_ANIMATION_INTERVAL = 40;
    private static final int BURST_DELAY = 3;
    private static final String FLY_ANIMATION = "animation.skillFire06.fly";

    public FireBird(EntityType<FireBird> type, Level level, EntityModelRow model) {
        super(type, level, model);
        moveControl = new FlyingMoveControl(this, 10, true);
    }

    public static AttributeSupplier.Builder attributes(EntityModelRow model) {
        return model.attributes().add(Attributes.FLYING_SPEED, FLYING_SPEED);
    }

    /** 火鸟术：在施放者上方 1.5 格生成，朝向随机后转向施放者的朝向，以施放者为主人。 */
    static void release(ServerPlayer caster, SkillBook book) {
        ServerLevel level = (ServerLevel) caster.level();
        FireBird bird = SkillSetup.<FireBird>entityType(ID).get().create(level);
        bird.cast().configure(caster.getUUID(), book.manaAttack(), book.damageMultiplier(), FIRE, book.name());
        Relations.copyAllegiance(caster, bird);
        bird.moveTo(caster.getX(), caster.getY() + 1.5, caster.getZ(), level.getRandom().nextFloat() * 360F, 0);
        SkillEntities.turn(bird, caster.getYRot(), 0);
        bird.tame(caster);
        level.addFreshEntity(bird);
    }

    static void register() {
        SkillReleases.register(FIRE, FireBird::release, "火鸟术");
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new FlyingPathNavigation(this, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        targetSelector.addGoal(1, new OwnerHurtTargetGoal(this));
        goalSelector.addGoal(2, new OwnerHurtByTargetGoal(this));
        goalSelector.addGoal(3, new FollowOwnerGoal(this, 3, 10, 2));
        goalSelector.addGoal(4, new Charge());
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
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    public void setNoGravity(boolean ignored) {
        super.setNoGravity(true);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        setNoGravity(true);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getDirectEntity() instanceof ThrownPotion || source.getDirectEntity() instanceof AreaEffectCloud || source.is(DamageTypes.CACTUS)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!level().isClientSide()) {
            burstParticles();
            discard();
        }
    }

    @Override
    public void baseTick() {
        super.baseTick();
        if (!level().isClientSide()) {
            tickSkill();
        }
    }

    private void burstParticles() {
        ((ServerLevel) level()).sendParticles(ParticleTypes.FLAME, getX(), getY(), getZ(), 20, 0, 0, 0, 0.5);
    }

    /** 每刻过程：寿命、飞行动画、有目标时沿朝向前冲、近身探测与爆发（来源按此次序，寿命到期后同一刻的其余部分照常执行）。 */
    private void tickSkill() {
        ServerLevel level = (ServerLevel) level();
        CompoundTag scratch = getPersistentData();
        scratch.putDouble("tick", scratch.getDouble("tick") + 1);
        if (scratch.getDouble("tick") >= LIFETIME) {
            scratch.putDouble("tick", 0);
            discard();
        }
        scratch.putDouble("tick2", scratch.getDouble("tick2") + 1);
        if (scratch.getDouble("tick2") >= FLY_ANIMATION_INTERVAL) {
            setAnimation(FLY_ANIMATION);
            scratch.putDouble("tick2", 0);
        }
        if (getTarget() != null) {
            double yaw = Math.toRadians(getYRot());
            double pitch = Math.toRadians(getXRot());
            setDeltaMovement(new Vec3(-0.2 * Math.sin(yaw), -0.2 * Math.sin(pitch), 0.2 * Math.cos(yaw)));
        }
        for (Entity other : ProjectileSupport.around(level, position(), 1.5)) {
            if (!ProjectileSupport.friendly(this, other)) {
                scratch.putBoolean("kaishi", true);
                break;
            }
        }
        if (scratch.getBoolean("kaishi")) {
            scratch.putDouble("tick3", scratch.getDouble("tick3") + 1);
            if (scratch.getDouble("tick3") >= BURST_DELAY) {
                for (Entity other : ProjectileSupport.around(level, position(), 2.5)) {
                    if (!ProjectileSupport.friendly(this, other)) {
                        ProjectileSupport.burst(other, "smallexplosionred", other.getBbHeight() / 2);
                        cast().strikeRounded(level, other);
                        ProjectileSupport.ignite(other, 5);
                    }
                }
                burstParticles();
                discard();
            }
        }
    }

    /** 有目标且没有在移动时飞向目标的眼睛，接触时冲撞，4 格内持续修正落点（来源的自定义目标）。 */
    private final class Charge extends Goal {
        Charge() {
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return getTarget() != null && !getMoveControl().hasWanted();
        }

        @Override
        public boolean canContinueToUse() {
            LivingEntity target = getTarget();
            return getMoveControl().hasWanted() && target != null && target.isAlive();
        }

        @Override
        public void start() {
            aim(getTarget());
        }

        @Override
        public void tick() {
            LivingEntity target = getTarget();
            if (getBoundingBox().intersects(target.getBoundingBox())) {
                doHurtTarget(target);
            } else if (distanceToSqr(target) < 16) {
                aim(target);
            }
        }

        private void aim(LivingEntity target) {
            Vec3 eye = target.getEyePosition(1);
            moveControl.setWantedPosition(eye.x, eye.y, eye.z, 2);
        }
    }
}
