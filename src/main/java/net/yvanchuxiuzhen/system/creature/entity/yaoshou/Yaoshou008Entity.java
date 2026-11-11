package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
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

/** 008号妖兽：来源编号身份、初始化、逐刻技能、声音、动画及死亡清袋。 */
public final class Yaoshou008Entity extends YaoshouEntity {
    private final Yaoshou037054Animations sourceAnimations = new Yaoshou037054Animations();

    public Yaoshou008Entity(EntityType<? extends YaoshouEntity> type, Level level, EntityModelRow model) {
        super(type, level, model);
        xpReward = 0;
    }

    @Override
    protected void sourceSpawn(ServerLevelAccessor level) {
        YaoshouShared.initialize(this, 14, "平庸", "勉强", "勉强", "贫弱", "勉强", "贫弱", "强劲", "平庸", "平庸", "平庸", "勉强", "平庸", "强劲");
    }

    @Override
    protected void sourceTick() {
        Yaoshou008Behavior.tick(level(), this);
    }

    @Override
    protected void sourceHurt(DamageSource source, float amount) {
        Yaoshou008Behavior.hurt(this, source.getEntity());
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
    public SoundEvent getAmbientSound() {
        return Yaoshou037054Support.sound("entity.wolf.ambient");
    }

    @Override
    public SoundEvent getHurtSound(DamageSource ds) {
        return Yaoshou037054Support.sound("entity.wolf.hurt");
    }

    @Override
    public SoundEvent getDeathSound() {
        return Yaoshou037054Support.sound("entity.wolf.death");
    }

    @Override
    public void playStepSound(BlockPos pos, BlockState blockIn) {
        this.playSound(Yaoshou037054Support.sound("entity.wolf.step"), 0.15f, 1);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        sourceAnimations.register(this, controllers, 4, "animation.yaoshou008.", state -> {
            if (procedureAnimation().equals("empty")) {
                if ((state.isMoving() || !(state.getLimbSwingAmount() > -0.15F && state.getLimbSwingAmount() < 0.15F)) && !isVehicle() && !isAggressive()) {
                    return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou008.walk"));
                }
                if (isDeadOrDying()) {
                    return state.setAndContinue(RawAnimation.begin().then("animation.yaoshou008.dle", Animation.LoopType.PLAY_ONCE));
                }
                if (isInWaterOrBubble()) {
                    return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou008.swimming"));
                }
                if (isShiftKeyDown()) {
                    return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou008.sneak"));
                }
                if (isSprinting()) {
                    return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou008.sprint"));
                }
                if (isVehicle() && state.isMoving()) {
                    return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou008.ride"));
                }
                if (isAggressive() && state.isMoving() && !isVehicle()) {
                    return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou008.attacking"));
                }
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou008.idle"));
            }
            return PlayState.STOP;
        });
    }
}
