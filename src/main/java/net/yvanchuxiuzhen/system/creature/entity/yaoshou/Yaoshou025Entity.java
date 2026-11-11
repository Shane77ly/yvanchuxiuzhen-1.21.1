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
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.creature.api.SpiritBeastBags;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

/** 025号妖兽：按来源资质初始化，以受击者UUID驱动逐刻技能流程。 */
public final class Yaoshou025Entity extends YaoshouEntity {
    private final Yaoshou037054Animations sourceAnimations = new Yaoshou037054Animations();
    public Yaoshou025Entity(EntityType<? extends YaoshouEntity> type, Level level, EntityModelRow model) {
        super(type, level, model);
        xpReward = 0;
    }

    @Override
    protected void playStepSound(BlockPos position, BlockState state) {
        playSound(sound("yaoshou050walk"), 0.15F, 1.0F);
    }

    @Override
    public SoundEvent getAmbientSound() {
        return sound("yaoshou025idle");
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return sound("yaoshou025attacked");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return sound("yaoshou025die");
    }

    @Override
    protected void sourceSpawn(ServerLevelAccessor level) {
        YaoshouShared.initialize(this, 6,
                "贫弱", "贫弱", "贫弱", "贫弱", "贫弱", "贫弱", "平庸", "贫弱", "贫弱", "贫弱", "贫弱", "贫弱", "强劲");
    }

    @Override
    protected void sourceTick() {
        Yaoshou025Behavior.tick(level(), this);
    }

    @Override
    protected void sourceHurt(DamageSource source, float amount) {
        Yaoshou025Behavior.hurt(this, source.getEntity());
    }

    @Override
    public void aiStep() {
        super.aiStep();
        updateSwingTime();
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        SpiritBeastBags.onCreatureDeath(this);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        sourceAnimations.register(this, controllers, 2, "animation.yaoshou025.", state -> {
            if (!procedureAnimation().equals("empty")) return PlayState.STOP;
            if (state.isMoving() || !(state.getLimbSwingAmount() > -0.15F && state.getLimbSwingAmount() < 0.15F)) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou025.walk"));
            }
            if (isDeadOrDying()) {
                return state.setAndContinue(RawAnimation.begin().then("animation.yaoshou025.death", Animation.LoopType.PLAY_ONCE));
            }
            if (isInWaterOrBubble()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou025.swim"));
            if (isSprinting()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou025.sprint"));
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou025.idle"));
        });
    }

    private static SoundEvent sound(String path) {
        return BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, path));
    }
}
