package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
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

/** 051号妖兽的独立来源实体生命周期、声音与动画。 */
public final class Yaoshou051Entity extends YaoshouEntity {
    private final Yaoshou037054Animations sourceAnimations = new Yaoshou037054Animations();

    public Yaoshou051Entity(EntityType<? extends YaoshouEntity> type, Level level, EntityModelRow model) {
        super(type, level, model);
        xpReward = 0;
    }

    public static AttributeSupplier.Builder attributes(EntityModelRow row) {
        return YaoshouEntity.attributes(row);
    }

    @Override protected void sourceSpawn(ServerLevelAccessor level) {
        YaoshouShared.initialize(this, 25, "绝佳", "平庸", "强劲", "平庸", "勉强", "贫弱", "平庸", "强劲", "强劲", "平庸", "勉强", "勉强", "平庸");
    }
    @Override protected void sourceTick() { Yaoshou051Behavior.tick(level(), this); }
    @Override protected void sourceHurt(DamageSource source, float amount) { Yaoshou037054Support.hurt(this, source, "animation.yaoshou051.attacked"); }
    @Override public void die(DamageSource source) { super.die(source); SpiritBeastBags.onCreatureDeath(this); }
    @Override public void aiStep() { super.aiStep(); updateSwingTime();  }
    @Override protected SoundEvent getAmbientSound() { return Yaoshou037054Support.sound("minecraft:entity.polar_bear.ambient"); }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return Yaoshou037054Support.sound("minecraft:entity.polar_bear.hurt"); }
    @Override protected SoundEvent getDeathSound() { return Yaoshou037054Support.sound("minecraft:entity.polar_bear.death"); }
    @Override protected void playStepSound(BlockPos pos, BlockState state) { playSound(Yaoshou037054Support.sound("minecraft:entity.polar_bear.step"), 0.15F, 1F); }

    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        sourceAnimations.register(this, controllers, 4, "animation.yaoshou051.", state -> {
            if (procedureAnimation().equals("empty")) {
                if ((state.isMoving() || !(state.getLimbSwingAmount() > -0.15F && state.getLimbSwingAmount() < 0.15F))
                        && !isVehicle() && !isAggressive())
                    return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou051.walk"));
                if (isDeadOrDying()) return state.setAndContinue(RawAnimation.begin().then("animation.yaoshou051.die", Animation.LoopType.PLAY_ONCE));
                if (isShiftKeyDown()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou051.sneak"));
                if (isSprinting()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou051.sprint"));
                if (isVehicle() && state.isMoving()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou051.ride"));
                if (isAggressive() && state.isMoving() && !isVehicle()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou051.attacking"));
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou051.idle"));
            }
            return PlayState.STOP;
        });
    }
}
