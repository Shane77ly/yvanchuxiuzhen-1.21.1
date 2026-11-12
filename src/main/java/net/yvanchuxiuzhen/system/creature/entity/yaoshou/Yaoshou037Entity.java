package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
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

/** 037号妖兽的来源初始化、声音、动画与生命周期。 */
public final class Yaoshou037Entity extends YaoshouEntity {
    private final Yaoshou037054Animations sourceAnimations = new Yaoshou037054Animations();

    public Yaoshou037Entity(EntityType<? extends YaoshouEntity> type, Level level, EntityModelRow model) {
        super(type, level, model);
        xpReward = 0;
    }

    public static AttributeSupplier.Builder attributes(EntityModelRow row) { return YaoshouEntity.attributes(row); }
    @Override protected void sourceSpawn(ServerLevelAccessor level) { YaoshouShared.initialize(this, 25, "强劲", "勉强", "平庸", "勉强", "勉强", "贫弱", "绝佳", "平庸", "平庸", "勉强", "勉强", "勉强", "平庸"); }
    @Override protected void sourceTick() { Yaoshou037Behavior.tick(level(), this); }
    @Override protected void sourceHurt(DamageSource source, float amount) { Yaoshou037054Support.hurt(this, source, "animation.yaoshou037.attacked"); }
    @Override public void die(DamageSource source) { super.die(source); SpiritBeastBags.onCreatureDeath(this); }
    @Override public void aiStep() { super.aiStep(); updateSwingTime(); }
    @Override
    public SoundEvent getAmbientSound() {
        return Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou037idle");
    }
    @Override
    public SoundEvent getHurtSound(DamageSource ds) {
        return Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou012attacked");
    }
    @Override
    public SoundEvent getDeathSound() {
        return Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou037die");
    }
    @Override
    public void playStepSound(BlockPos pos, BlockState blockIn) {
        this.playSound(Yaoshou037054Support.sound("yvanchuxiuzhen:yaoshou012walk"), 0.15f, 1);
    }

    @Override public boolean hurt(DamageSource source, float amount) { if (source.is(DamageTypes.LIGHTNING_BOLT)) { sourceHurt(source, amount); return false; } return super.hurt(source, amount); }
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        sourceAnimations.register(this, controllers, 4, "animation.yaoshou037.", state -> {

        if (procedureAnimation().equals("empty")) {
            if ((state.isMoving() || !(state.getLimbSwingAmount() > -0.15F && state.getLimbSwingAmount() < 0.15F))

                    && !isAggressive()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou037.walk"));
            }
            if (isDeadOrDying()) {
                return state.setAndContinue(RawAnimation.begin().then("animation.yaoshou037.die", Animation.LoopType.PLAY_ONCE));
            }
            if (isInWaterOrBubble()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou037.swim"));
            }
            if (isShiftKeyDown()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou037.sneak"));
            }
            if (isSprinting()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou037.sprint"));
            }
            if (isAggressive() && state.isMoving()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou037.sprint"));
            }
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou037.idle"));
        }
        return PlayState.STOP;

        });
    }
}
