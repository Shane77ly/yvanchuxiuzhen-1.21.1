package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.creature.api.SpiritBeastBags;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

/** 027号妖兽的独立编号身份、来源移动、声音、动画与生命周期。 */
public final class Yaoshou027Entity extends YaoshouEntity {
    private final Yaoshou037054Animations sourceAnimations = new Yaoshou037054Animations();

    public Yaoshou027Entity(EntityType<? extends YaoshouEntity> type, Level level, EntityModelRow model) {
        super(type, level, model);
        xpReward = 0;
    }

    @Override
    protected void sourceSpawn(ServerLevelAccessor level) {
        YaoshouShared.initialize(this, 9, "平庸", "勉强", "平庸", "勉强", "勉强", "贫弱", "勉强", "勉强", "贫弱", "勉强", "勉强", "勉强", "绝佳");
    }

    @Override
    protected void sourceTick() {
        Yaoshou027Behavior.tick(level(), this);
    }

    @Override
    protected void sourceHurt(DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        if (!isAlive() || attacker == null || Relations.isFriendly(this, attacker)) return;
        Yaoshou037054Support.hurt(this, source, "animation.yaoshou027.attacked");
        MobEffectInstance current = getEffect(MobEffects.MOVEMENT_SPEED);
        if (current == null) {
            addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 0));
        } else if (current.getAmplifier() <= 3) {
            addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,
                    200 + current.getDuration() * 20, current.getAmplifier() + 1));
        }
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
    public SoundEvent getAmbientSound() {
        return Yaoshou037054Support.sound("entity.horse.ambient");
    }

    @Override
    public SoundEvent getHurtSound(DamageSource ds) {
        return Yaoshou037054Support.sound("entity.horse.hurt");
    }

    @Override
    public SoundEvent getDeathSound() {
        return Yaoshou037054Support.sound("entity.horse.death");
    }

    @Override
    public void playStepSound(BlockPos pos, BlockState blockIn) {
        this.playSound(Yaoshou037054Support.sound("entity.horse.step"), 0.15f, 1);
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float partialTick) {
        return super.getPassengerAttachmentPoint(passenger, dimensions, partialTick).add(0, -0.5, 0);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.FALL)) {
            sourceHurt(source, amount);
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        Yaoshou027Behavior.Yaoshou027ShiTiZhuiLuoShi(level(), this);
        return super.causeFallDamage(distance, multiplier, source);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        sourceAnimations.register(this, controllers, 4, "animation.yaoshou027.", state -> {
            if (procedureAnimation().equals("empty")) {
                if ((state.isMoving() || !(state.getLimbSwingAmount() > -0.15F && state.getLimbSwingAmount() < 0.15F))
                        && !isAggressive()) {
                    return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou027.walk"));
                }
                if (isDeadOrDying()) {
                    return state.setAndContinue(RawAnimation.begin().then("animation.yaoshou027.die", Animation.LoopType.PLAY_ONCE));
                }
                if (isInWaterOrBubble()) {
                    return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou027.swim"));
                }
                if (isShiftKeyDown()) {
                    return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou027.sneak"));
                }
                if (isSprinting()) {
                    return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou027.sprint"));
                }
                if (isAggressive() && state.isMoving()) {
                    return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou027.sprint"));
                }
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou027.idle"));
            }
            return PlayState.STOP;
        });
    }
}
