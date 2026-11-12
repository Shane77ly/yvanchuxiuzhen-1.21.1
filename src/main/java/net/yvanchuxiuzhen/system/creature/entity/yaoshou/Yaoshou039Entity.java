package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.creature.api.SpiritBeastBags;
import net.yvanchuxiuzhen.system.creature.entity.SourceFollowOwnerGoal;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

/** 039号妖兽的来源实体身份、基础属性、伤害与动画。 */
public final class Yaoshou039Entity extends YaoshouEntity {
    private final Yaoshou037054Animations sourceAnimations = new Yaoshou037054Animations();

    public Yaoshou039Entity(EntityType<? extends YaoshouEntity> type, Level level, EntityModelRow model) {
        super(type, level, model);
        xpReward = 0;
    }

    public static AttributeSupplier.Builder attributes(EntityModelRow row) {
        return YaoshouEntity.attributes(row).add(Attributes.FOLLOW_RANGE, 16);
    }

    @Override protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(1, new SourceFollowOwnerGoal(this, 1, 10, 2, false));
        goalSelector.addGoal(2, new FloatGoal(this));
    }

    @Override protected void sourceSpawn(ServerLevelAccessor level) {
        YaoshouShared.initialize(this, 15, "勉强", "勉强", "强劲", "强劲", "勉强", "贫弱", "勉强", "贫弱", "强劲", "勉强", "贫弱", "贫弱", "强劲");
    }

    @Override protected void sourceTick() { Yaoshou039Behavior.tick(level(), this); }

    @Override protected void sourceHurt(DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        if (!isAlive() || attacker == null || Relations.isFriendly(this, attacker)) return;
        getPersistentData().putString("linshigongjizhe", attacker.getStringUUID());
        if (getPersistentData().getBoolean("beidaduan")) {
            setAnimation("empty");
            getPersistentData().putBoolean("beidaduan", false);
        }
        if (getPersistentData().getString("jineng").isEmpty() && !level().isClientSide()) setAnimation("animation.yaoshou039.attacked");
    }

    @Override public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.FALL)) {
            sourceHurt(source, amount);
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override public void die(DamageSource source) {
        super.die(source);
        SpiritBeastBags.onCreatureDeath(this);
    }

    @Override public void aiStep() { super.aiStep(); updateSwingTime(); }
    @Override protected EntityDimensions getDefaultDimensions(Pose pose) {
        return super.getDefaultDimensions(pose).withEyeHeight(1.7F);
    }
    @Override public void playStepSound(BlockPos pos, BlockState state) {
        playSound(sound("entity.wolf.step"), 0.15F, 1.0F);
    }
    @Override protected SoundEvent getAmbientSound() { return sound("entity.fox.ambient"); }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return sound("entity.fox.hurt"); }
    @Override protected SoundEvent getDeathSound() { return sound("entity.fox.death"); }

    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        sourceAnimations.register(this, controllers, 4, "animation.yaoshou039.", state -> {
            if (!procedureAnimation().equals("empty")) return PlayState.STOP;
            boolean moving = state.isMoving() || !(state.getLimbSwingAmount() > -0.15F && state.getLimbSwingAmount() < 0.15F);
            if (moving && !isAggressive()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou039.walk"));
            if (isDeadOrDying()) return state.setAndContinue(RawAnimation.begin().then("animation.yaoshou039.die", Animation.LoopType.PLAY_ONCE));
            if (isInWaterOrBubble()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou039.swim"));
            if (isShiftKeyDown()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou039.sneak"));
            if (isSprinting() || isAggressive() && state.isMoving()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou039.sprint"));
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou039.idle"));
        });
    }

    private static SoundEvent sound(String id) { return BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse(id)); }
}
