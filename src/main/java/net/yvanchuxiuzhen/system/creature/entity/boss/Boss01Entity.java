package net.yvanchuxiuzhen.system.creature.entity.boss;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.yvanchuxiuzhen.platform.content.entity.AnimatedLook;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/** 首领01的原生近战目标、持久生命周期与来源三动画控制器。 */
public final class Boss01Entity extends Monster implements GeoEntity, AnimatedLook {
    private static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(Boss01Entity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(Boss01Entity.class, EntityDataSerializers.STRING);
    private final EntityModelRow model;
    private final AnimatableInstanceCache animations = GeckoLibUtil.createInstanceCache(this);
    private String procedureAnimation = "empty";
    private boolean swinging;
    private long lastSwing;

    public Boss01Entity(EntityType<? extends Boss01Entity> type, Level level, EntityModelRow model) {
        super(type, level);
        this.model = model;
        entityData.set(TEXTURE, model.texture());
        xpReward = 0;
        setNoAi(false);
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder attributes(EntityModelRow model) {
        return model.attributes();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TEXTURE, "ycxzboss_01");
        builder.define(ANIMATION, "undefined");
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2, false) {
            @Override
            protected boolean canPerformAttack(LivingEntity target) {
                double reach = mob.getBbWidth() * mob.getBbWidth() + target.getBbWidth();
                return isTimeToAttack() && mob.distanceToSqr(target.getX(), target.getBoundingBox().minY, target.getZ()) <= reach;
            }
        });
        goalSelector.addGoal(2, new RandomStrollGoal(this, 1));
        targetSelector.addGoal(3, new HurtByTargetGoal(this));
        goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        goalSelector.addGoal(5, new FloatGoal(this));
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
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
    public boolean hurt(DamageSource source, float amount) {
        Boss01Behavior.hurt(this);
        return super.hurt(source, amount);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
            SpawnGroupData data) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data);
        Boss01Stats.initialize(this);
        return result;
    }

    @Override
    public void baseTick() {
        String request = entityData.get(ANIMATION);
        if (!request.equals("undefined")) {
            entityData.set(ANIMATION, "undefined");
            procedureAnimation = request;
        }
        super.baseTick();
        Boss01Behavior.update(this);
        refreshDimensions();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        updateSwingTime();
    }

    @Override
    protected void tickDeath() {
        if (++deathTime == 80) {
            remove(RemovalReason.KILLED);
            dropExperience(this);
        }
    }

    @Override
    public String texture() {
        return entityData.get(TEXTURE);
    }

    public void setTexture(String texture) {
        entityData.set(TEXTURE, texture);
    }

    public void setAnimation(String animation) {
        entityData.set(ANIMATION, animation);
    }

    public String getSyncedAnimation() {
        return entityData.get(ANIMATION);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        int transitionTicks = model.lifecycle().animationTransitionTicks();
        controllers.add(new AnimationController<Boss01Entity>(this, "movement", transitionTicks, state -> {
            if (!procedureAnimation.equals("empty")) return PlayState.STOP;
            boolean moving = state.isMoving() || !(state.getLimbSwingAmount() > -0.15F && state.getLimbSwingAmount() < 0.15F);
            if (moving && !isVehicle() && !isAggressive()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.ycxzboss_01.walk"));
            if (isDeadOrDying()) return state.setAndContinue(RawAnimation.begin().then("animation.ycxzboss_01.death", Animation.LoopType.PLAY_ONCE));
            if (isShiftKeyDown()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.ycxzboss_01.sneak"));
            if (isSprinting()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.ycxzboss_01.attacking"));
            if (isVehicle() && state.isMoving()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.ycxzboss_01.sneak"));
            if (isAggressive() && state.isMoving() && !isVehicle()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.ycxzboss_01.attacking"));
            return state.setAndContinue(RawAnimation.begin().thenLoop(model.idle()));
        }));
        controllers.add(new AnimationController<Boss01Entity>(this, "attacking", transitionTicks, state -> {
            if (getAttackAnim(state.getPartialTick()) > 0 && !swinging) {
                swinging = true;
                lastSwing = level().getGameTime();
            }
            if (swinging && lastSwing + 15 <= level().getGameTime()) swinging = false;
            AnimationController<Boss01Entity> controller = state.getController();
            if (swinging && controller.getAnimationState() == AnimationController.State.STOPPED) {
                controller.forceAnimationReset();
                controller.setAnimation(RawAnimation.begin().then("animation.ycxzboss_01.attack", Animation.LoopType.PLAY_ONCE));
            }
            return PlayState.CONTINUE;
        }));
        controllers.add(new AnimationController<Boss01Entity>(this, "procedure", transitionTicks, state -> {
            AnimationController<Boss01Entity> controller = state.getController();
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

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return animations;
    }
}
