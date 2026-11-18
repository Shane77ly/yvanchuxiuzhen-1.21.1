package net.yvanchuxiuzhen.system.creature.entity;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.content.entity.AnimatedLook;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.particle.api.Particles;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/** 游魂的来源飞行接触攻击、资质初始化、亡灵身份和动画生命周期。 */
public final class Guiguai01Entity extends Monster implements GeoEntity, AnimatedLook {
    private static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(Guiguai01Entity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(Guiguai01Entity.class, EntityDataSerializers.STRING);
    private final EntityModelRow model;
    private final AnimatableInstanceCache animations = GeckoLibUtil.createInstanceCache(this);
    private String procedureAnimation = "empty";
    private boolean swinging;
    private long lastSwing;

    public Guiguai01Entity(EntityType<? extends Guiguai01Entity> type, Level level, EntityModelRow model) {
        super(type, level);
        this.model = model;
        xpReward = 0;
        setNoAi(false);
        moveControl = new FlyingMoveControl(this, 10, true);
        entityData.set(TEXTURE, model.texture());
    }

    public static AttributeSupplier.Builder attributes(EntityModelRow model) {
        return model.attributes().add(Attributes.FLYING_SPEED, model.lifecycle().flyingSpeed().orElseThrow());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TEXTURE, "kong");
        builder.define(ANIMATION, "undefined");
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new FlyingPathNavigation(this, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(1, new ContactAttack());
        goalSelector.addGoal(2, new RandomStrollGoal(this, 0.8, 20) {
            @Override
            protected Vec3 getPosition() {
                RandomSource random = getRandom();
                return new Vec3(getX() + (random.nextFloat() * 2 - 1) * 16,
                        getY() + (random.nextFloat() * 2 - 1) * 16, getZ() + (random.nextFloat() * 2 - 1) * 16);
            }
        });
        goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2, false) {
            @Override
            protected boolean canPerformAttack(LivingEntity target) {
                return isTimeToAttack() && Guiguai01Entity.this.distanceToSqr(target.getX(), target.getY(), target.getZ())
                        <= Guiguai01Entity.this.getBbWidth() * Guiguai01Entity.this.getBbWidth() + target.getBbWidth();
            }
        });
        goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Player.class, false, false));
        targetSelector.addGoal(6, new HurtByTargetGoal(this));
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.GENERIC_HURT; }

    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.GENERIC_DEATH; }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) { return false; }

    @Override
    protected void checkFallDamage(double distance, boolean onGround, BlockState state, BlockPos pos) { }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return !source.is(DamageTypes.FALL) && !source.is(DamageTypes.CACTUS)
                && !source.is(DamageTypes.DROWN) && super.hurt(source, amount);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (level() instanceof ServerLevel server)
            server.sendParticles(ParticleTypes.SMOKE, getX(), getY(), getZ(), 16, 0, 0, 0, 0.1);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, SpawnGroupData data) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data);
        CreatureSourceStats.initialize(this, 3, false, false,
                "勉强", "贫弱", "贫弱", "贫弱", "平庸", "平庸", "贫弱", "勉强", "勉强", "平庸", "强劲", "平庸", "勉强");
        return result;
    }

    @Override
    public void baseTick() {
        super.baseTick();
        CompoundTag saved = saveWithoutId(new CompoundTag());
        if (saved.getBoolean("IsAngry") && level() instanceof ServerLevel server)
            server.sendParticles(Particles.type("cailiao_233particle"), getX(), getY(), getZ(), 2, 0.1, 0.1, 0.1, 0.1);
        refreshDimensions();
        String request = entityData.get(ANIMATION);
        if (!request.equals("undefined")) {
            entityData.set(ANIMATION, "undefined");
            procedureAnimation = request;
        }
    }

    @Override
    public void setNoGravity(boolean ignored) { super.setNoGravity(true); }

    @Override
    public void aiStep() {
        super.aiStep();
        setNoGravity(true);
    }

    /** 来源递增后比较零刻，保留其死亡实体不按普通二十刻清除的条件。 */
    @Override
    protected void tickDeath() {
        if (++deathTime == model.lifecycle().deathTicks()) {
            remove(RemovalReason.KILLED);
            dropExperience(this);
        }
    }

    @Override public String texture() { return entityData.get(TEXTURE); }
    public void setTexture(String texture) { entityData.set(TEXTURE, texture); }
    public String getSyncedAnimation() { return entityData.get(ANIMATION); }
    public void setAnimation(String animation) { entityData.set(ANIMATION, animation); }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        int transitionTicks = model.lifecycle().animationTransitionTicks();
        controllers.add(new AnimationController<Guiguai01Entity>(this, "movement", transitionTicks, state -> {
            if (!procedureAnimation.equals("empty")) return PlayState.STOP;
            boolean moving = state.isMoving() || !(state.getLimbSwingAmount() > -0.15F && state.getLimbSwingAmount() < 0.15F);
            if (moving && onGround()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.guiguai01.walk"));
            if (isDeadOrDying()) return state.setAndContinue(RawAnimation.begin().then("animation.guiguai01.death", Animation.LoopType.PLAY_ONCE));
            if (!onGround()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.guiguai01.walk"));
            return state.setAndContinue(RawAnimation.begin().thenLoop(model.idle()));
        }));
        controllers.add(new AnimationController<Guiguai01Entity>(this, "attacking", transitionTicks, state -> {
            if (getAttackAnim(state.getPartialTick()) > 0 && !swinging) {
                swinging = true;
                lastSwing = level().getGameTime();
            }
            if (swinging && lastSwing + 15 <= level().getGameTime()) swinging = false;
            AnimationController<Guiguai01Entity> controller = state.getController();
            if (swinging && controller.getAnimationState() == AnimationController.State.STOPPED) {
                controller.forceAnimationReset();
                controller.setAnimation(RawAnimation.begin().then("animation.guiguai01.attack", Animation.LoopType.PLAY_ONCE));
            }
            return PlayState.CONTINUE;
        }));
        controllers.add(new AnimationController<Guiguai01Entity>(this, "procedure", transitionTicks, state -> {
            AnimationController<Guiguai01Entity> controller = state.getController();
            if (!procedureAnimation.equals("empty") && controller.getAnimationState() == AnimationController.State.STOPPED) {
                controller.setAnimation(RawAnimation.begin().then(procedureAnimation, Animation.LoopType.PLAY_ONCE));
                if (controller.getAnimationState() == AnimationController.State.STOPPED) {
                    procedureAnimation = "empty";
                    controller.forceAnimationReset();
                }
            }
            return PlayState.CONTINUE;
        }));
    }

    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return animations; }

    private final class ContactAttack extends Goal {
        private ContactAttack() { setFlags(EnumSet.of(Flag.MOVE)); }
        @Override public boolean canUse() { return getTarget() != null && !moveControl.hasWanted(); }
        @Override public boolean canContinueToUse() { return moveControl.hasWanted() && getTarget() != null && getTarget().isAlive(); }
        @Override public void start() {
            Vec3 eye = getTarget().getEyePosition(1);
            moveControl.setWantedPosition(eye.x, eye.y, eye.z, 1);
        }
        @Override public void tick() {
            LivingEntity target = getTarget();
            if (getBoundingBox().intersects(target.getBoundingBox())) doHurtTarget(target);
            else if (distanceToSqr(target) < 16) {
                Vec3 eye = target.getEyePosition(1);
                moveControl.setWantedPosition(eye.x, eye.y, eye.z, 1);
            }
        }
    }
}
