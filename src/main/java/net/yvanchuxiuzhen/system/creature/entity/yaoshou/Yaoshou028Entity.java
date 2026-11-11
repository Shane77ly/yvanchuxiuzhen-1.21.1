package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.creature.api.SpiritBeastBags;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

/** 028号妖兽：保留来源水生属性、溺水免疫和水枪技能。 */
public final class Yaoshou028Entity extends YaoshouEntity {
    private final Yaoshou037054Animations sourceAnimations = new Yaoshou037054Animations();

    public Yaoshou028Entity(EntityType<? extends YaoshouEntity> type, Level level, EntityModelRow model) {
        super(type, level, model);
        xpReward = 0;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.removeAllGoals(goal -> goal instanceof FloatGoal);
    }

    @Override
    protected void sourceSpawn(ServerLevelAccessor level) {
        if (YaoshouShared.initialize(this, 8,
                "平庸", "平庸", "平庸", "勉强", "贫弱", "贫弱", "勉强", "绝佳", "贫弱", "强劲", "贫弱", "贫弱", "贫弱")) {
            getAttribute(NeoForgeMod.SWIM_SPEED).setBaseValue(getPersistentData().getDouble("speed_number") * 6.5);
        }
    }

    @Override
    protected void sourceTick() {
        Yaoshou028Behavior.tick(this);
    }

    @Override
    protected EntityDimensions getDefaultDimensions(Pose pose) {
        return super.getDefaultDimensions(pose).withEyeHeight(0.3F);
    }

    @Override
    protected void sourceHurt(DamageSource source, float amount) {
        Yaoshou037054Support.hurt(this, source, "animation.yaoshou028.attacked");
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.DROWN)) {
            sourceHurt(source, amount);
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        SpiritBeastBags.onCreatureDeath(this);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return sound("entity.turtle.ambient_land");
    }

    @Override
    protected void playStepSound(BlockPos position, BlockState state) {
        playSound(sound("entity.turtle.swim"), 0.15F, 1.0F);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return sound("entity.turtle.hurt");
    }

    @Override
    protected SoundEvent getDeathSound() {
        return sound("entity.turtle.death");
    }

    @Override
    public void aiStep() {
        super.aiStep();
        updateSwingTime();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        sourceAnimations.register(this, controllers, 4, "animation.yaoshou028.", state -> {
            if (procedureAnimation().equals("empty")) {
                if ((state.isMoving() || !(state.getLimbSwingAmount() > -0.15F && state.getLimbSwingAmount() < 0.15F))
                        && !isAggressive()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou028.walk"));
                if (isDeadOrDying()) return state.setAndContinue(RawAnimation.begin().then("animation.yaoshou028.die", Animation.LoopType.PLAY_ONCE));
                if (isInWaterOrBubble()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou028.swim"));
                if (isShiftKeyDown()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou028.sneak"));
                if (isSprinting() || isAggressive() && state.isMoving()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou028.sprint"));
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou028.idle"));
            }
            return PlayState.STOP;
        });
    }

    private static SoundEvent sound(String path) {
        return BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse(path));
    }
}
