package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
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

/** 035号妖兽的来源身份、初始化、生命周期与逐刻行为。 */
public final class Yaoshou035Entity extends YaoshouEntity {
    private final Yaoshou037054Animations sourceAnimations = new Yaoshou037054Animations();

    public Yaoshou035Entity(EntityType<? extends YaoshouEntity> type, Level level, EntityModelRow model) {
        super(type, level, model);
        xpReward = 0;
    }

    @Override protected void sourceSpawn(ServerLevelAccessor level) {
        YaoshouShared.initialize(this, 19, "勉强", "勉强", "平庸", "勉强", "平庸", "勉强", "平庸", "勉强", "平庸", "勉强", "强劲", "强劲", "平庸");
    }

    @Override protected void sourceTick() { Yaoshou035Behavior.tick(this); }
    @Override protected void sourceHurt(DamageSource source, float amount) { Yaoshou037054Support.hurt(this, source, "animation.yaoshou035.attacked"); }

    @Override
    public void aiStep() {
        super.aiStep();
        updateSwingTime();
    }

    @Override public void die(DamageSource source) { super.die(source); SpiritBeastBags.onCreatureDeath(this); }

    @Override protected void playStepSound(BlockPos pos, BlockState state) { playSound(sound("yaoshou035walk"), 0.15F, 1); }
    @Override protected SoundEvent getAmbientSound() { return sound("yaoshou035idle"); }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return sound("yaoshou035attacked"); }
    @Override protected SoundEvent getDeathSound() { return sound("yaoshou035die"); }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        sourceAnimations.register(this, controllers, 4, "animation.yaoshou035.", state -> {
            if (!procedureAnimation().equals("empty")) return PlayState.STOP;
            if ((state.isMoving() || !(state.getLimbSwingAmount() > -0.15F && state.getLimbSwingAmount() < 0.15F)) && !isAggressive()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou035.walk"));
            }
            if (isDeadOrDying()) return state.setAndContinue(RawAnimation.begin().then("animation.yaoshou035.die", Animation.LoopType.PLAY_ONCE));
            if (isInWaterOrBubble()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou035.swim"));
            if (isSprinting() || isAggressive() && state.isMoving()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou035.sprint"));
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou035.idle"));
        });
    }

    private static SoundEvent sound(String id) {
        return BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", id));
    }
}
