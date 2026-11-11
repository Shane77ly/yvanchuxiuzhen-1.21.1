package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.creature.api.SpiritBeastBags;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

/** 006号妖兽：来源编号身份、初始化、逐刻技能、声音、动画及死亡清袋。 */
public final class Yaoshou006Entity extends YaoshouEntity {
    private final Yaoshou037054Animations sourceAnimations = new Yaoshou037054Animations();

    public Yaoshou006Entity(EntityType<? extends YaoshouEntity> type, Level level, EntityModelRow model) {
        super(type, level, model);
        xpReward = 0;
    }

    @Override
    protected void sourceSpawn(ServerLevelAccessor level) {
        YaoshouShared.initialize(this, 1, "贫弱", "贫弱", "勉强", "勉强", "贫弱", "贫弱", "勉强", "勉强", "平庸", "勉强", "勉强", "勉强", "强劲");
    }

    @Override
    protected void sourceTick() {
        Yaoshou006Behavior.tick(level(), this);
    }

    @Override
    protected void sourceHurt(DamageSource source, float amount) {
        Yaoshou006Behavior.hurt(this, source.getEntity());
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        SpiritBeastBags.onCreatureDeath(this);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        updateSwingTime();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.FALL)) {
            sourceHurt(source, amount);
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public SoundEvent getAmbientSound() {
        return Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou006idle");
    }

    @Override
    public SoundEvent getHurtSound(DamageSource ds) {
        return Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou006attacked");
    }

    @Override
    public SoundEvent getDeathSound() {
        return Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou006die");
    }

    @Override
    public void playStepSound(BlockPos pos, BlockState blockIn) {
        this.playSound(Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou006walk"), 0.15f, 1);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        sourceAnimations.register(this, controllers, 0, "animation.yaoshou006.", state -> {
            if (procedureAnimation().equals("empty")) {
                if ((state.isMoving() || !(state.getLimbSwingAmount() > -0.15F && state.getLimbSwingAmount() < 0.15F)) && onGround() && !isVehicle() && !isAggressive()) {
                    return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou006.walk"));
                }
                if (isDeadOrDying()) {
                    return state.setAndContinue(RawAnimation.begin().then("animation.yaoshou006.die", Animation.LoopType.PLAY_ONCE));
                }
                if (isInWaterOrBubble()) {
                    return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou006.swimming"));
                }
                if (isShiftKeyDown()) {
                    return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou006.sneak"));
                }
                if (isSprinting()) {
                    return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou006.sprint"));
                }
                if (!onGround()) {
                    return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou006.fly"));
                }
                if (isVehicle() && state.isMoving()) {
                    return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou006.ride"));
                }
                if (isAggressive() && state.isMoving() && !isVehicle()) {
                    return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou006.attacking"));
                }
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou006.idle"));
            }
            return PlayState.STOP;
        });
    }
}
