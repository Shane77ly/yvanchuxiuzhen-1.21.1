package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.creature.api.SpiritBeastBags;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

/** 018号妖兽：来源编号身份、初始化、逐刻技能、声音、动画及死亡清袋。 */
public final class Yaoshou018Entity extends YaoshouEntity {
    private final Yaoshou037054Animations sourceAnimations = new Yaoshou037054Animations();

    public Yaoshou018Entity(EntityType<? extends YaoshouEntity> type, Level level, EntityModelRow model) {
        super(type, level, model);
        xpReward = 0;
    }

    @Override
    protected void sourceSpawn(ServerLevelAccessor level) {
        YaoshouShared.initializeWithoutExperience(this, 2, "贫弱", "贫弱", "强劲", "强劲", "贫弱", "贫弱", "平庸", "强劲", "勉强", "平庸", "贫弱", "贫弱", "贫弱");
    }

    @Override
    protected void sourceTick() {
        Yaoshou018Behavior.tick(level(), this);
    }

    @Override
    protected void sourceHurt(DamageSource source, float amount) {
        Yaoshou018Behavior.hurt(this, source.getEntity());
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
    public SoundEvent getHurtSound(DamageSource ds) {
        return Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou018attacked");
    }

    @Override
    public SoundEvent getDeathSound() {
        return Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou018die");
    }

    @Override
    public void playStepSound(BlockPos pos, BlockState blockIn) {
        this.playSound(Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou050idle"), 0.15f, 1);
    }

    @Override
    protected EntityDimensions getDefaultDimensions(Pose pose) {
        return super.getDefaultDimensions(pose).withEyeHeight(0.3F);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        sourceAnimations.register(this, controllers, 4, "animation.yaoshou018.", state -> {
            if (procedureAnimation().equals("empty")) {
                if ((state.isMoving() || !(state.getLimbSwingAmount() > -0.15F && state.getLimbSwingAmount() < 0.15F))
                ) {
                    return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou018.walk"));
                }
                if (isDeadOrDying()) {
                    return state.setAndContinue(RawAnimation.begin().then("animation.yaoshou018.die", Animation.LoopType.PLAY_ONCE));
                }
                if (isShiftKeyDown()) {
                    return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou018.sneak"));
                }
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou018.idle"));
            }
            return PlayState.STOP;
        });
    }
}
