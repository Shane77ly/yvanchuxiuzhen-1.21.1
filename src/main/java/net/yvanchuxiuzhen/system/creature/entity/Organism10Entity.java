package net.yvanchuxiuzhen.system.creature.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
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

/** Organism10：按来源持久状态机休息、起飞、飞行30刻、下降30刻并冷却60刻。 */
public final class Organism10Entity extends PathfinderMob implements GeoEntity, AnimatedLook {
    private static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(Organism10Entity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(Organism10Entity.class, EntityDataSerializers.STRING);
    private final EntityModelRow model;
    private final AnimatableInstanceCache animations = GeckoLibUtil.createInstanceCache(this);
    private String procedureAnimation = "empty";
    private String moodState = "";
    private double stateTimer;

    public Organism10Entity(EntityType<? extends Organism10Entity> type, Level level, EntityModelRow model) {
        super(type, level);
        this.model = model;
        xpReward = 0;
        entityData.set(TEXTURE, model.texture());
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
        goalSelector.addGoal(1, new RandomStrollGoal(this, 0.8, 20) {
            @Override protected Vec3 getPosition() {
                RandomSource random = Organism10Entity.this.getRandom();
                return new Vec3(getX() + (random.nextFloat() * 2 - 1) * 16,
                        getY() + (random.nextFloat() * 2 - 1) * 16, getZ() + (random.nextFloat() * 2 - 1) * 16);
            }
        });
        goalSelector.addGoal(2, new RandomLookAroundGoal(this));
        goalSelector.addGoal(3, new FloatGoal(this));
        goalSelector.addGoal(4, new LeapAtTargetGoal(this, 0.5F));
    }

    public static AttributeSupplier.Builder attributes(EntityModelRow row) { return row.attributes(); }

    @Override protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.GENERIC_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.GENERIC_DEATH; }
    @Override public boolean hurt(DamageSource source, float amount) { return !source.is(DamageTypes.FALL) && super.hurt(source, amount); }

    @Override
    public void baseTick() {
        super.baseTick();
        if (level().isClientSide()) return;
        AABB area = new AABB(position(), position()).inflate(4);
        if (level().getEntitiesOfClass(Player.class, area).stream().sorted((a, b) -> Double.compare(a.distanceToSqr(position()), b.distanceToSqr(position()))).findFirst().isPresent()) {
            moodState = "飞行";
        }
        if (moodState.equals("飞行") || moodState.equals("降落")) setAnimation("animation.organism10.fly");
        if (moodState.equals("休息1") || moodState.isEmpty()) {
            double tick = stateTimer + 1;
            if (tick >= 46) {
                if (Math.random() < 0.1) moodState = "飞行";
                else if (Math.random() < 0.4) moodState = "开始休息2";
                else moodState = "开始休息3";
                tick = 0;
            }
            stateTimer = tick;
        } else if (moodState.equals("开始休息2")) {
            setAnimation("animation.organism10.idle2");
            moodState = "休息冷却";
        } else if (moodState.equals("开始休息3")) {
            setAnimation("animation.organism10.idle3");
            moodState = "休息冷却";
        } else if (moodState.equals("休息冷却")) {
            double tick = stateTimer + 1;
            if (tick >= 60) {
                moodState = "休息1";
                tick = 0;
            }
            stateTimer = tick;
        } else if (moodState.equals("飞行")) {
            double yaw = getYRot() * (3.14 / 180);
            setDeltaMovement(new Vec3(-0.5 * Math.sin(yaw), 0.2, 0.5 * Math.cos(yaw)));
            double tick = stateTimer + 1;
            if (tick >= 30) { moodState = "降落"; tick = 0; }
            stateTimer = tick;
        } else if (moodState.equals("降落")) {
            double yaw = getYRot() * (3.14 / 180);
            setDeltaMovement(new Vec3(-0.5 * Math.sin(yaw), -0.2, 0.5 * Math.cos(yaw)));
            double tick = stateTimer + 1;
            if (tick >= 30) {
                setAnimation("empty");
                moodState = "休息1";
                tick = 0;
            }
            stateTimer = tick;
        }
        refreshDimensions();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("xiangfa", moodState);
        tag.putDouble("jishitick", stateTimer);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        moodState = tag.getString("xiangfa");
        stateTimer = tag.getDouble("jishitick");
    }

    @Override
    public void tick() {
        String request = entityData.get(ANIMATION);
        if (!request.equals("undefined")) { entityData.set(ANIMATION, "undefined"); procedureAnimation = request; }
        super.tick();
    }

    @Override
    protected void tickDeath() {
        if (++deathTime == 20) {
            remove(RemovalReason.KILLED);
            dropExperience(lastHurtByPlayer);
        }
    }
    @Override public void aiStep() { super.aiStep(); updateSwingTime(); }
    @Override public String texture() { return entityData.get(TEXTURE); }
    public void setTexture(String texture) { entityData.set(TEXTURE, texture); }
    public void setAnimation(String animation) { entityData.set(ANIMATION, animation); }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<Organism10Entity>(this, "movement", 4, state -> {
            if (!procedureAnimation.equals("empty")) return PlayState.STOP;
            boolean moving = state.isMoving() || !(state.getLimbSwingAmount() > -.15F && state.getLimbSwingAmount() < .15F);
            if (onGround() && moving) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.organism10.walk"));
            return state.setAndContinue(RawAnimation.begin().thenLoop(onGround() ? model.idle() : model.air().orElseThrow()));
        }));
        controllers.add(new AnimationController<Organism10Entity>(this, "procedure", 4, state -> {
            AnimationController<Organism10Entity> controller = state.getController();
            if (!procedureAnimation.equals("empty") && controller.getAnimationState() == AnimationController.State.STOPPED) {
                controller.setAnimation(RawAnimation.begin().then(procedureAnimation, Animation.LoopType.PLAY_ONCE));
                if (controller.getAnimationState() == AnimationController.State.STOPPED) { procedureAnimation = "empty"; controller.forceAnimationReset(); }
            }
            return PlayState.CONTINUE;
        }));
    }
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return animations; }
}
