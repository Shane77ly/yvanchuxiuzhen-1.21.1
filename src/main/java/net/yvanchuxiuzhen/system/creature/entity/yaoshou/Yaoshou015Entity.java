package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.creature.api.SpiritBeastBags;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

/** 015号妖兽的来源初始化、声音、动画与生命周期。 */
public final class Yaoshou015Entity extends YaoshouEntity {
    private final Yaoshou037054Animations sourceAnimations = new Yaoshou037054Animations();

    public Yaoshou015Entity(EntityType<? extends YaoshouEntity> type, Level level, EntityModelRow model) {
        super(type, level, model);
        xpReward = 0;
    }

    public static AttributeSupplier.Builder attributes(EntityModelRow row) { return YaoshouEntity.attributes(row); }
    @Override protected void sourceSpawn(ServerLevelAccessor level) { YaoshouShared.initialize(this, 3, "贫弱", "贫弱", "勉强", "贫弱", "贫弱", "贫弱", "强劲", "平庸", "贫弱", "平庸", "贫弱", "贫弱", "勉强"); }
    @Override protected void sourceTick() { Yaoshou015Behavior.tick(level(), this); }
    @Override protected void sourceHurt(DamageSource source, float amount) { Yaoshou037054Support.hurt(this, source, "animation.yaoshou015.attacked"); }
    @Override public void die(DamageSource source) { super.die(source); SpiritBeastBags.onCreatureDeath(this); }
    @Override public void aiStep() { super.aiStep(); updateSwingTime(); }
    @Override
    public SoundEvent getAmbientSound() {
        return Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou015idle");
    }
    @Override
    public SoundEvent getHurtSound(DamageSource ds) {
        return Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou015attacked");
    }
    @Override
    public SoundEvent getDeathSound() {
        return Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou015die");
    }
    @Override
    public void playStepSound(BlockPos pos, BlockState blockIn) {
        this.playSound(Yaoshou037054Support.sound("entity.spider.step"), 0.15f, 1);
    }

    @Override protected EntityDimensions getDefaultDimensions(Pose pose) { return super.getDefaultDimensions(pose).withEyeHeight(1.4F); }
    @Override public boolean hurt(DamageSource source, float amount) { if (source.is(DamageTypes.FALL)) { sourceHurt(source, amount); return false; } return super.hurt(source, amount); }
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        sourceAnimations.register(this, controllers, 2, "animation.yaoshou015.", state -> {

        if (procedureAnimation().equals("empty")) {
            if ((state.isMoving() || !(state.getLimbSwingAmount() > -0.15F && state.getLimbSwingAmount() < 0.15F)) && !isVehicle() && !isAggressive()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou015.walk"));
            }
            if (isDeadOrDying()) {
                return state.setAndContinue(RawAnimation.begin().then("animation.yaoshou015.die", Animation.LoopType.PLAY_ONCE));
            }
            if (isShiftKeyDown()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou015.sneak"));
            }
            if (isSprinting()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou015.sprint"));
            }
            if (isVehicle() && state.isMoving()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou015.ride"));
            }
            if (isAggressive() && state.isMoving() && !isVehicle()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou015.attacking"));
            }
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou015.idle"));
        }
        return PlayState.STOP;

        });
    }
}
