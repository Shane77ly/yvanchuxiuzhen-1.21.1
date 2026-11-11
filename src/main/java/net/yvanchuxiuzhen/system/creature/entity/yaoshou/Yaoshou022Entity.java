package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.creature.api.SpiritBeastBags;
import net.yvanchuxiuzhen.system.creature.entity.SourceFollowOwnerGoal;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

/** 022号妖兽的独立编号身份、来源移动、声音、动画与生命周期。 */
public final class Yaoshou022Entity extends YaoshouEntity {

    public Yaoshou022Entity(EntityType<? extends YaoshouEntity> type, Level level, EntityModelRow model) {
        super(type, level, model);
        xpReward = 0;
        this.moveControl = new FlyingMoveControl(this, 10, true);
    }

    @Override
    protected void sourceSpawn(ServerLevelAccessor level) {
        if (YaoshouShared.initialize(this, 7, "强劲", "平庸", "平庸", "勉强", "贫弱", "贫弱", "勉强", "强劲", "勉强", "强劲", "贫弱", "平庸", "贫弱")) {
            getAttribute(Attributes.FLYING_SPEED).setBaseValue(getPersistentData().getDouble("speed_number") * 0.7);
        }
    }

    @Override
    protected void sourceTick() {
        Yaoshou022Behavior.tick(level(), this);
    }

    @Override
    protected void sourceHurt(DamageSource source, float amount) {
        Yaoshou037054Support.hurt(this, source, "animation.yaoshou022.attacked");
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        SpiritBeastBags.onCreatureDeath(this);
        setNoGravity(false);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        updateSwingTime();
        setNoGravity(true);
    }

    @Override
    public SoundEvent getHurtSound(DamageSource ds) {
        return Yaoshou037054Support.sound("entity.generic.hurt");
    }

    @Override
    public SoundEvent getDeathSound() {
        return Yaoshou037054Support.sound("entity.generic.death");
    }

    @Override
    protected void checkFallDamage(double y, boolean onGroundIn, BlockState state, BlockPos pos) {
    }

    @Override
    public void setNoGravity(boolean ignored) {
        super.setNoGravity(true);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new FlyingPathNavigation(this, level);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new SourceFollowOwnerGoal(this, 1, 10, 2, false));
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<YaoshouEntity>(this, "movement", 4, state -> {
            if (procedureAnimation().equals("empty")) {
                if (isShiftKeyDown()) {
                    return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou022.defence"));
                }
                if (isSprinting()) {
                    return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou022.fastmove"));
                }
                if (!onGround()) {
                    return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou022.move"));
                }
                if (isVehicle() && state.isMoving()) {
                    return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou022.sleep"));
                }
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou022.wait"));
            }
            return PlayState.STOP;
        }));
        controllers.add(new AnimationController<YaoshouEntity>(this, "procedure", 4, state -> {
            AnimationController<YaoshouEntity> controller = state.getController();
            if (!procedureAnimation().equals("empty") && controller.getAnimationState() == AnimationController.State.STOPPED) {
                controller.setAnimation(RawAnimation.begin().then(procedureAnimation(), Animation.LoopType.PLAY_ONCE));
                if (controller.getAnimationState() == AnimationController.State.STOPPED) {
                    clearProcedureAnimation();
                    controller.forceAnimationReset();
                }
            }
            return PlayState.CONTINUE;
        }));
    }
}
