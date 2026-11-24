package net.yvanchuxiuzhen.system.creature.entity.boss;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
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

/** 04号首领的实体生命周期与来源动画控制。 */
public final class Boss04Entity extends PathfinderMob implements GeoEntity, AnimatedLook {
    private static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(Boss04Entity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(Boss04Entity.class, EntityDataSerializers.STRING);
    private final AnimatableInstanceCache animations = GeckoLibUtil.createInstanceCache(this);
    private String procedureAnimation = "empty";
    private CompoundTag aiState = new CompoundTag();

    public Boss04Entity(EntityType<? extends Boss04Entity> type, Level level, EntityModelRow model) {
        super(type, level);
        entityData.set(TEXTURE, model.texture());
        xpReward = 0;
        setNoAi(false);
        setPersistenceRequired();
    }

    public CompoundTag aiState() {
        return aiState;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("AiState", aiState.copy());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        aiState = tag.contains("AiState") ? tag.getCompound("AiState").copy() : new CompoundTag();
    }

    public static AttributeSupplier.Builder attributes(EntityModelRow model) {
        return model.attributes();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TEXTURE, "ycxzboss_04");
        builder.define(ANIMATION, "undefined");
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public Vec3 getPassengerRidingPosition(Entity passenger) {
        return super.getPassengerRidingPosition(passenger).add(0, -1, 0);
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
        Boss04Behavior.hurt(this, source.getEntity());
        return !source.is(DamageTypes.FALL) && !source.is(DamageTypes.CACTUS) && super.hurt(source, amount);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, SpawnGroupData data) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data);
        Boss04Stats.initialize(this);
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
        Boss04Behavior.tick(this);
        refreshDimensions();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        updateSwingTime();
    }

    @Override
    protected void tickDeath() {
        if (++deathTime == 100) {
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
        controllers.add(new AnimationController<Boss04Entity>(this, "movement", 0, state -> {
            if (!procedureAnimation.equals("empty")) return PlayState.STOP;
            boolean moving = state.isMoving() || !(state.getLimbSwingAmount() > -0.15F && state.getLimbSwingAmount() < 0.15F);
            if (moving) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.ycxzboss_04.walk"));
            if (isDeadOrDying()) return state.setAndContinue(RawAnimation.begin().then("animation.ycxzboss_04.die", Animation.LoopType.PLAY_ONCE));
            if (isInWaterOrBubble()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.ycxzboss_04.swim"));
            if (isSprinting()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.ycxzboss_04.sprint"));
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.ycxzboss_04.idle"));
        }));
        controllers.add(new AnimationController<Boss04Entity>(this, "procedure", 0, state -> {
            AnimationController<Boss04Entity> controller = state.getController();
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
