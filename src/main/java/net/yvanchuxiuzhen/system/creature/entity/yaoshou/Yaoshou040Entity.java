package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.creature.api.SpiritBeastBags;
import net.yvanchuxiuzhen.system.creature.entity.SourceFollowOwnerGoal;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

/** 040号妖兽的来源飞行、基础属性、技能及死亡生命周期。 */
public final class Yaoshou040Entity extends YaoshouEntity {
    public Yaoshou040Entity(EntityType<? extends YaoshouEntity> type, Level level, EntityModelRow model) {
        super(type, level, model);
        xpReward = 0;
        moveControl = new FlyingMoveControl(this, 10, true);
    }

    public static AttributeSupplier.Builder attributes(EntityModelRow row) {
        return YaoshouEntity.attributes(row).add(Attributes.FOLLOW_RANGE, 32);
    }

    @Override protected PathNavigation createNavigation(Level level) { return new FlyingPathNavigation(this, level); }
    @Override protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(1, new SourceFollowOwnerGoal(this, 1, 10, 2, false));
    }
    @Override protected void sourceSpawn(ServerLevelAccessor level) {
        if (YaoshouShared.initialize(this, 25, "强劲", "勉强", "绝佳", "绝佳", "勉强", "贫弱", "贫弱", "贫弱", "绝佳", "平庸", "勉强", "强劲", "贫弱")) {
            getAttribute(Attributes.FLYING_SPEED).setBaseValue(getPersistentData().getDouble("speed_number") * 0.7);
        }
    }

    @Override protected void sourceTick() {
        Yaoshou040Behavior.tick(level(), this);
    }

    @Override protected void sourceHurt(DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        if (!isAlive() || attacker == null || Relations.isFriendly(this, attacker)) return;
        getPersistentData().putString("linshigongjizhe", attacker.getStringUUID());
        if (getPersistentData().getBoolean("beidaduan")) {
            getPersistentData().putBoolean("beidaduan", false);
            if (isShiftKeyDown()) setShiftKeyDown(false);
        }
    }

    @Override public void setNoGravity(boolean ignored) { super.setNoGravity(true); }
    @Override public boolean causeFallDamage(float distance, float multiplier, DamageSource source) { return false; }
    @Override protected void checkFallDamage(double distance, boolean onGround, BlockState state, BlockPos pos) { }
    @Override public void die(DamageSource source) { super.die(source); SpiritBeastBags.onCreatureDeath(this); }
    @Override public void aiStep() { super.aiStep(); updateSwingTime(); setNoGravity(true); }
    @Override public void playStepSound(BlockPos pos, BlockState state) {
        playSound(sound("yvanchuxiuzhen:yaoshou040idle"), 0.15F, 1.0F);
    }
    @Override protected SoundEvent getAmbientSound() { return sound("yvanchuxiuzhen:yaoshou040idle"); }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return sound("entity.generic.hurt"); }
    @Override protected SoundEvent getDeathSound() { return sound("entity.generic.death"); }

    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<Yaoshou040Entity>(this, "movement", 10, state -> {
            if (!procedureAnimation().equals("empty")) return PlayState.STOP;
            if (isDeadOrDying()) return state.setAndContinue(RawAnimation.begin().then("animation.yaoshou040.die", Animation.LoopType.PLAY_ONCE));
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou040.idle"));
        }));
        controllers.add(new AnimationController<Yaoshou040Entity>(this, "procedure", 10, state -> {
            AnimationController<Yaoshou040Entity> controller = state.getController();
            if (!procedureAnimation().equals("empty") && controller.getAnimationState() == AnimationController.State.STOPPED) {
                controller.setAnimation(RawAnimation.begin().then(procedureAnimation(), Animation.LoopType.PLAY_ONCE));
                if (controller.getAnimationState() == AnimationController.State.STOPPED) {
                    clearProcedureAnimation();
                    controller.forceAnimationReset();
                }
            }
            return PlayState.CONTINUE;
        }));
    }

    private static SoundEvent sound(String id) { return BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse(id)); }
}
