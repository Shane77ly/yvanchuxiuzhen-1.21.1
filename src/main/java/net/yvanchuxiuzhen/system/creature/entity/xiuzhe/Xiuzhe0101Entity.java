package net.yvanchuxiuzhen.system.creature.entity.xiuzhe;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.yvanchuxiuzhen.platform.content.entity.AnimatedLook;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.YaoshouAttributes;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/** 0101号修者的独立来源实体、属性、动态尺寸及动画生命周期。 */
public final class Xiuzhe0101Entity extends Monster implements GeoEntity, AnimatedLook {
    private static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(Xiuzhe0101Entity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(Xiuzhe0101Entity.class, EntityDataSerializers.STRING);
    private final AnimatableInstanceCache animations = GeckoLibUtil.createInstanceCache(this);
    private String procedureAnimation = "empty";
    private boolean swinging;
    private long lastSwing;

    public Xiuzhe0101Entity(EntityType<? extends Xiuzhe0101Entity> type, Level level) {
        super(type, level);
        xpReward = 0;
        setNoAi(false);
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes().add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.MAX_HEALTH, 20)
                .add(Attributes.ARMOR, 0).add(Attributes.ATTACK_DAMAGE, 3).add(Attributes.FOLLOW_RANGE, 16)
                .add(YaoshouAttributes.SIZE).add(YaoshouAttributes.SKIN);
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(ANIMATION, "undefined");
        builder.define(TEXTURE, "xiuzhe0101");
    }

    public void setAnimation(String animation) { entityData.set(ANIMATION, animation); }
    public void setTexture(String texture) { entityData.set(TEXTURE, texture); }
    @Override public String texture() { return entityData.get(TEXTURE); }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.GENERIC_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.GENERIC_DEATH; }

    @Override public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, SpawnGroupData data) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data);
        Xiuzhe0101Stats.initialize(level, this);
        return result;
    }

    @Override public boolean hurt(DamageSource source, float amount) {
        Xiuzhe0101Behavior.hurt(this, source.getEntity());
        return super.hurt(source, amount);
    }

    @Override public void baseTick() {
        super.baseTick();
        Xiuzhe0101Behavior.tick(level(), this);
        restoreSkin();
        refreshDimensions();
        String request = entityData.get(ANIMATION);
        if (!request.equals("undefined")) {
            entityData.set(ANIMATION, "undefined");
            procedureAnimation = request;
        }
    }

    void restoreSkin() {
        double variant = getAttribute(YaoshouAttributes.SKIN).getBaseValue();
        String path = variant == 5 ? "xiuzhe0105" : variant == 4 ? "xiuzhe0104" : variant == 3 ? "xiuzhe0103"
                : variant == 2 ? "xiuzhe0102" : "xiuzhe0101";
        if (!path.equals(texture())) setTexture(path);
    }

    @Override protected EntityDimensions getDefaultDimensions(Pose pose) {
        return super.getDefaultDimensions(pose).scale((float) getAttributeValue(YaoshouAttributes.SIZE));
    }

    @Override protected void tickDeath() {
        if (++deathTime == 600) {
            remove(RemovalReason.KILLED);
            dropExperience(this);
        }
    }

    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<Xiuzhe0101Entity>(this, "movement", 4, state -> {
            if (!procedureAnimation.equals("empty")) return PlayState.STOP;
            if ((state.isMoving() || !(state.getLimbSwingAmount() > -0.15F && state.getLimbSwingAmount() < 0.15F))
                    && onGround() && !isVehicle() && !isAggressive()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.xiuzhe0101.walk01"));
            if (isDeadOrDying()) return state.setAndContinue(RawAnimation.begin().then("animation.xiuzhe0101.die02", Animation.LoopType.PLAY_ONCE));
            if (isInWaterOrBubble()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.xiuzhe0101.swim01"));
            if (isShiftKeyDown()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.xiuzhe0101.sneak01"));
            if (isSprinting()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.xiuzhe0101.sprint01"));
            if (!onGround()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.xiuzhe0101.fly01"));
            if (isVehicle() && state.isMoving()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.xiuzhe0101.ride01"));
            if (isAggressive() && state.isMoving() && !isVehicle()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.xiuzhe0101.sprint01"));
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.xiuzhe0101.idle02"));
        }));
        controllers.add(new AnimationController<Xiuzhe0101Entity>(this, "attacking", 4, state -> {
            if (getAttackAnim(state.getPartialTick()) > 0 && !swinging) {
                swinging = true;
                lastSwing = level().getGameTime();
            }
            if (swinging && lastSwing + 15 <= level().getGameTime()) swinging = false;
            AnimationController<Xiuzhe0101Entity> controller = state.getController();
            if (swinging && controller.getAnimationState() == AnimationController.State.STOPPED) {
                controller.forceAnimationReset();
                controller.setAnimation(RawAnimation.begin().then("animation.xiuzhe0101.attack01", Animation.LoopType.PLAY_ONCE));
            }
            return PlayState.CONTINUE;
        }));
        controllers.add(new AnimationController<Xiuzhe0101Entity>(this, "procedure", 4, state -> {
            AnimationController<Xiuzhe0101Entity> controller = state.getController();
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
}
