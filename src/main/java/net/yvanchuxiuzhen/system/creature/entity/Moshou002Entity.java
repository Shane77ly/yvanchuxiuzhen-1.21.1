package net.yvanchuxiuzhen.system.creature.entity;

import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.creature.data.CreatureData;
import net.yvanchuxiuzhen.system.creature.data.CreatureState;

/** 002号魔兽的飞行追击、四十刻冲刺、来源资质和无重力生命周期。 */
public final class Moshou002Entity extends MoshouEntity {
    public Moshou002Entity(EntityType<? extends MoshouEntity> type, Level level) {
        super(type, level, "moshou002", 20);
        moveControl = new FlyingMoveControl(this, 10, true);
    }

    @Override
    protected double meleeSpeed() { return 1.5; }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new FlyingPathNavigation(this, level);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new Goal() {
            {
                setFlags(EnumSet.of(Flag.MOVE));
            }

            @Override
            public boolean canUse() {
                return getTarget() != null && !getMoveControl().hasWanted();
            }

            @Override
            public boolean canContinueToUse() {
                return getMoveControl().hasWanted() && getTarget() != null && getTarget().isAlive();
            }

            @Override
            public void start() {
                Vec3 eye = getTarget().getEyePosition(1);
                moveControl.setWantedPosition(eye.x, eye.y, eye.z, 1.5);
            }

            @Override
            public void tick() {
                LivingEntity target = getTarget();
                if (getBoundingBox().intersects(target.getBoundingBox())) {
                    doHurtTarget(target);
                } else if (distanceToSqr(target) < 16) {
                    Vec3 eye = target.getEyePosition(1);
                    moveControl.setWantedPosition(eye.x, eye.y, eye.z, 1.5);
                }
            }
        });
        goalSelector.addGoal(2, new RandomStrollGoal(this, .8, 20) {
            @Override
            protected Vec3 getPosition() {
                RandomSource random = getRandom();
                return new Vec3(getX() + (random.nextFloat() * 2 - 1) * 16,
                        getY() + (random.nextFloat() * 2 - 1) * 16,
                        getZ() + (random.nextFloat() * 2 - 1) * 16);
            }
        });
        goalSelector.addGoal(3, new RandomLookAroundGoal(this));
        targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Player.class, false, false));
        targetSelector.addGoal(5, new HurtByTargetGoal(this));
    }

    @Override
    protected void initializeSourceData() {
        if (EntityAttributes.realmName(this).isEmpty()) {
            CreatureSourceStats.initialize(this, 20, false,
                    "强劲", "平庸", "平庸", "勉强", "贫弱", "贫弱", "勉强", "强劲", "勉强", "强劲", "贫弱", "平庸", "绝佳");
            getAttribute(Attributes.FLYING_SPEED).setBaseValue(EntityAttributes.stat(this, Stat.MOVE_SPEED) * 3);
        }
    }

    @Override
    protected void sourceTick() {
        if (isAlive() && getTarget() != null) {
            CreatureState state = CreatureData.state(this);
            double tick = state.tick() + 1;
            if (tick >= 40) {
                setDeltaMovement(getLookAngle());
                tick = 0;
            }
            state.setTick(tick);
        }
    }

    @Override
    protected boolean immuneTo(DamageSource source) { return source.is(DamageTypes.FALL); }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) { return false; }

    @Override
    protected void checkFallDamage(double distance, boolean onGround, BlockState state, BlockPos pos) { }

    @Override
    public void setNoGravity(boolean ignored) { super.setNoGravity(true); }

    @Override
    public void aiStep() {
        super.aiStep();
        setNoGravity(true);
    }

    @Override
    public SoundEvent getAmbientSound() { return SoundEvents.PHANTOM_AMBIENT; }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) { playSound(SoundEvents.PHANTOM_FLAP, .15F, 1); }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.PHANTOM_HURT; }

    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.PHANTOM_DEATH; }

    public static AttributeSupplier.Builder attributes() {
        return MoshouEntity.attributes(.3).add(Attributes.FLYING_SPEED, .3);
    }
}
