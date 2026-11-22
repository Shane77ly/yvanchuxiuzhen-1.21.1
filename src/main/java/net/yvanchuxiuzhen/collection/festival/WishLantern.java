package net.yvanchuxiuzhen.collection.festival;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
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

/** 祈愿灯实体：保持无重力，每刻转向一度并按视线方向缓慢上升。 */
public final class WishLantern extends PathfinderMob implements GeoEntity, AnimatedLook {
    private static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(WishLantern.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(WishLantern.class, EntityDataSerializers.STRING);
    private final EntityModelRow model;
    private final AnimatableInstanceCache animations = GeckoLibUtil.createInstanceCache(this);
    private String procedureAnimation = "empty";

    public WishLantern(EntityType<? extends WishLantern> type, Level level, EntityModelRow model) {
        super(type, level);
        this.model = model;
        xpReward = 0;
        setNoAi(model.noAi());
        moveControl = new FlyingMoveControl(this, 10, true);
        entityData.set(TEXTURE, model.texture());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TEXTURE, "");
        builder.define(ANIMATION, "undefined");
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new FlyingPathNavigation(this, level);
    }

    @Override
    public void baseTick() {
        super.baseTick();
        setYRot(getYRot() + 1);
        setXRot(0);
        setYBodyRot(getYRot());
        setYHeadRot(getYRot());
        yRotO = getYRot();
        xRotO = getXRot();
        yBodyRotO = getYRot();
        yHeadRotO = getYRot();
        Vec3 look = getLookAngle();
        setDeltaMovement(look.x * 0.1, 0.1, look.z * 0.1);
        refreshDimensions();
        String request = entityData.get(ANIMATION);
        if (!request.equals("undefined")) {
            entityData.set(ANIMATION, "undefined");
            procedureAnimation = request;
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        updateSwingTime();
        setNoGravity(true);
    }

    @Override
    public void setNoGravity(boolean ignored) {
        super.setNoGravity(true);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return !source.is(DamageTypes.FALL) && !source.is(DamageTypes.CACTUS) && super.hurt(source, amount);
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double distance, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return null;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return null;
    }

    @Override
    protected void tickDeath() {
        if (++deathTime == model.lifecycle().deathTicks()) {
            remove(RemovalReason.KILLED);
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

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<WishLantern>(this, "movement", model.lifecycle().animationTransitionTicks(), state ->
                procedureAnimation.equals("empty") ? state.setAndContinue(RawAnimation.begin().thenLoop(model.idle())) : PlayState.STOP));
        controllers.add(new AnimationController<WishLantern>(this, "procedure", model.lifecycle().animationTransitionTicks(), state -> {
            AnimationController<WishLantern> controller = state.getController();
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
