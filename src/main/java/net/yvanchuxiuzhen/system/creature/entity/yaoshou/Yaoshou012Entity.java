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

/** 012号妖兽：保留来源等级资质、逐刻战斗和三控制器动画。 */
public final class Yaoshou012Entity extends YaoshouEntity {
    private final Yaoshou037054Animations sourceAnimations = new Yaoshou037054Animations();

    public Yaoshou012Entity(EntityType<? extends YaoshouEntity> type, Level level, EntityModelRow model) {
        super(type, level, model);
        xpReward = 0;
    }

    @Override
    protected void sourceSpawn(ServerLevelAccessor level) {
        YaoshouShared.initialize(this, 15,
                "强劲", "勉强", "平庸", "勉强", "平庸", "勉强", "强劲", "平庸", "强劲", "平庸", "平庸", "平庸", "强劲");
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.FALL)) {
            sourceHurt(source, amount);
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override protected void sourceTick() { Yaoshou012Behavior.tick(level(), this); }
    @Override protected void sourceHurt(DamageSource source, float amount) { Yaoshou037054Support.hurt(this, source, "animation.yaoshou012.attacked"); }

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
    protected void playStepSound(BlockPos position, BlockState state) {
        playSound(Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou012walk"), 0.15F, 1.0F);
    }

    @Override public SoundEvent getAmbientSound() { return Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou012idle"); }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou012attacked"); }
    @Override protected SoundEvent getDeathSound() { return Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou012die"); }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        sourceAnimations.register(this, controllers, 2, "animation.yaoshou012.", state -> {
            if (procedureAnimation().equals("empty")) {
                if ((state.isMoving() || !(state.getLimbSwingAmount() > -0.15F && state.getLimbSwingAmount() < 0.15F)) && onGround() && !isAggressive()) {
                    return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou012.walk"));
                }
                if (isDeadOrDying()) {
                    return state.setAndContinue(RawAnimation.begin().then("animation.yaoshou012.die", Animation.LoopType.PLAY_ONCE));
                }
                if (isInWaterOrBubble()) {
                    return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou012.swim"));
                }
                if (isShiftKeyDown()) {
                    return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou012.sneak"));
                }
                if (isSprinting()) {
                    return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou012.sprint"));
                }
                if (!onGround()) {
                    return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou012.fly"));
                }
                if (isAggressive() && state.isMoving()) {
                    return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou012.sprint"));
                }
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou012.idle"));
            }
            return PlayState.STOP;
        });
    }
}
