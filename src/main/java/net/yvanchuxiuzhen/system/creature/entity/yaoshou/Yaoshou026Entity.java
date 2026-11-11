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

/** 026号妖兽：保留来源资质、元素技能和实体生命周期。 */
public final class Yaoshou026Entity extends YaoshouEntity {
    private final Yaoshou037054Animations sourceAnimations = new Yaoshou037054Animations();

    public Yaoshou026Entity(EntityType<? extends YaoshouEntity> type, Level level, EntityModelRow model) {
        super(type, level, model);
        xpReward = 0;
    }

    @Override
    protected void sourceSpawn(ServerLevelAccessor level) {
        YaoshouShared.initialize(this, 15,
                "绝佳", "勉强", "绝佳", "平庸", "勉强", "贫弱", "平庸", "平庸", "强劲", "平庸", "平庸", "勉强", "平庸");
    }

    @Override
    protected void sourceTick() {
        Yaoshou026Behavior.tick(this);
    }

    @Override
    protected void sourceHurt(DamageSource source, float amount) {
        Yaoshou037054Support.hurt(this, source, "animation.yaoshou026.attacked");
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        SpiritBeastBags.onCreatureDeath(this);
    }

    @Override
    protected void playStepSound(BlockPos position, BlockState state) {
        playSound(sound("yaoshou012walk"), 0.15F, 1.0F);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return sound("yaoshou012idle");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return sound("yaoshou012attacked");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return sound("yaoshou012die");
    }

    @Override
    public void aiStep() {
        super.aiStep();
        updateSwingTime();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        sourceAnimations.register(this, controllers, 4, "animation.yaoshou026.", state -> {
            if (procedureAnimation().equals("empty")) {
                if ((state.isMoving() || !(state.getLimbSwingAmount() > -0.15F && state.getLimbSwingAmount() < 0.15F))
                        && !isAggressive()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou026.walk"));
                if (isDeadOrDying()) return state.setAndContinue(RawAnimation.begin().then("animation.yaoshou026.die", Animation.LoopType.PLAY_ONCE));
                if (isInWaterOrBubble()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou026.swim"));
                if (isShiftKeyDown()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou026.sneak"));
                if (isSprinting() || isAggressive() && state.isMoving()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou026.sprint"));
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou026.idle"));
            }
            return PlayState.STOP;
        });
    }

    private static SoundEvent sound(String path) {
        return BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath("yvanchuxiuzhen", path));
    }
}
