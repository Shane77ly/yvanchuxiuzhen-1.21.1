package net.yvanchuxiuzhen.system.creature.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.yvanchuxiuzhen.platform.content.entity.AnimatedLook;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/** 魔兽共享来源同步、动画、受击打断与原生生命周期。 */
public abstract class MoshouEntity extends Monster implements GeoEntity, AnimatedLook {
    private static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(MoshouEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(MoshouEntity.class, EntityDataSerializers.STRING);
    private final AnimatableInstanceCache animations = GeckoLibUtil.createInstanceCache(this);
    private final String animationName;
    private final int deathTicks;
    private boolean swinging;
    private long lastSwing;
    private String procedureAnimation = "empty";

    MoshouEntity(EntityType<? extends MoshouEntity> type, Level level, String name, int deathTicks) {
        super(type, level);
        this.animationName = name;
        this.deathTicks = deathTicks;
        entityData.set(TEXTURE, name);
        xpReward = 0;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TEXTURE, "");
        builder.define(ANIMATION, "undefined");
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(1, new MeleeAttackGoal(this, meleeSpeed(), false) {
            @Override
            protected boolean canPerformAttack(LivingEntity target) {
                return isTimeToAttack() && distanceToSqr(target.getX(), target.getY(), target.getZ())
                        <= getBbWidth() * getBbWidth() + target.getBbWidth();
            }
        });
        targetSelector.addGoal(2, new HurtByTargetGoal(this));
        goalSelector.addGoal(3, new RandomStrollGoal(this, 0.8));
        goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        if (hasFloatGoal()) goalSelector.addGoal(5, new FloatGoal(this));
        targetSelector.addGoal(hasFloatGoal() ? 6 : 5, new NearestAttackableTargetGoal<>(this, Player.class, false, false));
    }

    protected boolean hasFloatGoal() { return true; }

    protected abstract double meleeSpeed();

    @Override protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.GENERIC_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.GENERIC_DEATH; }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isAlive() && !StatusEffects.has(this, "skilleffect_08")) {
            CompoundTag data = getPersistentData();
            if (!data.getString("jineng").isEmpty()) {
                setAnimation("empty");
                data.putDouble("entitytick", 0);
                data.putString("jineng", "");
                data.putString("jinengjinxing", "");
                data.putDouble("entitytick2", 0);
            }
            setAnimation("animation." + animationName + ".attacked");
        }
        return super.hurt(source, amount);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        onSourceDeath();
    }

    protected void onSourceDeath() { }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
            SpawnGroupData data) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data);
        initializeSourceData();
        return result;
    }

    protected abstract void initializeSourceData();

    @Override
    public void baseTick() {
        super.baseTick();
        sourceTick();
        refreshDimensions();
        String request = entityData.get(ANIMATION);
        if (!request.equals("undefined")) {
            entityData.set(ANIMATION, "undefined");
            procedureAnimation = request;
        }
    }

    protected void sourceTick() { }

    @Override
    protected void tickDeath() {
        if (++deathTime == deathTicks) {
            remove(RemovalReason.KILLED);
            dropExperience(this);
        }
    }

    @Override public String texture() { return entityData.get(TEXTURE); }
    public void setAnimation(String animation) { entityData.set(ANIMATION, animation); }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<MoshouEntity>(this, "movement", 4, state -> {
            if (!procedureAnimation.equals("empty")) return PlayState.STOP;
            boolean moving = state.isMoving() || !(state.getLimbSwingAmount() > -0.15F && state.getLimbSwingAmount() < 0.15F);
            if (moving && !isAggressive()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation." + animationName + ".walk"));
            if (isDeadOrDying()) return state.setAndContinue(RawAnimation.begin().then("animation." + animationName + (animationName.equals("moshou003") ? ".death" : ".die"), Animation.LoopType.PLAY_ONCE));
            if (isAggressive() && state.isMoving()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation." + animationName + ".attacking"));
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation." + animationName + ".idle"));
        }));
        controllers.add(new AnimationController<MoshouEntity>(this, "attacking", 4, state -> {
            if (getAttackAnim(state.getPartialTick()) > 0 && !swinging) { swinging = true; lastSwing = level().getGameTime(); }
            if (swinging && lastSwing + 15 <= level().getGameTime()) swinging = false;
            AnimationController<MoshouEntity> controller = state.getController();
            if (swinging && controller.getAnimationState() == AnimationController.State.STOPPED) {
                controller.forceAnimationReset();
                controller.setAnimation(RawAnimation.begin().then("animation." + animationName + ".attack", Animation.LoopType.PLAY_ONCE));
            }
            return PlayState.CONTINUE;
        }));
        controllers.add(new AnimationController<MoshouEntity>(this, "procedure", 4, state -> {
            AnimationController<MoshouEntity> controller = state.getController();
            if (!procedureAnimation.equals("empty") && controller.getAnimationState() == AnimationController.State.STOPPED) {
                controller.setAnimation(RawAnimation.begin().then(procedureAnimation, Animation.LoopType.PLAY_ONCE));
                if (controller.getAnimationState() == AnimationController.State.STOPPED) { procedureAnimation = "empty"; controller.forceAnimationReset(); }
            }
            return PlayState.CONTINUE;
        }));
    }

    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return animations; }
    public static AttributeSupplier.Builder attributes(double speed) {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, speed).add(Attributes.MAX_HEALTH, 100)
                .add(Attributes.ARMOR, 0).add(Attributes.ATTACK_DAMAGE, 3).add(Attributes.FOLLOW_RANGE, 16);
    }
}
