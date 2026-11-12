package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.creature.api.SpiritBeastBags;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

/** 009号妖兽：来源编号身份、初始化、逐刻技能、声音、动画及死亡清袋。 */
public final class Yaoshou009Entity extends YaoshouEntity {
    private final Yaoshou037054Animations sourceAnimations = new Yaoshou037054Animations();

    public Yaoshou009Entity(EntityType<? extends YaoshouEntity> type, Level level, EntityModelRow model) {
        super(type, level, model);
        xpReward = 0;
        this.setPathfindingMalus(PathType.WATER, 0);
        this.moveControl = new MoveControl(this) {
            @Override
            public void tick() {
                if (Yaoshou009Entity.this.isInWater())
                    Yaoshou009Entity.this.setDeltaMovement(Yaoshou009Entity.this.getDeltaMovement().add(0, 0.005, 0));
                if (this.operation == MoveControl.Operation.MOVE_TO && !Yaoshou009Entity.this.getNavigation().isDone()) {
                    double dx = this.wantedX - Yaoshou009Entity.this.getX();
                    double dy = this.wantedY - Yaoshou009Entity.this.getY();
                    double dz = this.wantedZ - Yaoshou009Entity.this.getZ();
                    float f = (float) (Mth.atan2(dz, dx) * (double) (180 / Math.PI)) - 90;
                    float f1 = (float) (this.speedModifier * Yaoshou009Entity.this.getAttribute(Attributes.MOVEMENT_SPEED).getValue());
                    Yaoshou009Entity.this.setYRot(this.rotlerp(Yaoshou009Entity.this.getYRot(), f, 10));
                    Yaoshou009Entity.this.yBodyRot = Yaoshou009Entity.this.getYRot();
                    Yaoshou009Entity.this.yHeadRot = Yaoshou009Entity.this.getYRot();
                    if (Yaoshou009Entity.this.isInWater()) {
                        Yaoshou009Entity.this.setSpeed((float) Yaoshou009Entity.this.getAttribute(Attributes.MOVEMENT_SPEED).getValue());
                        float f2 = -(float) (Mth.atan2(dy, (float) Math.sqrt(dx * dx + dz * dz)) * (180 / Math.PI));
                        f2 = Mth.clamp(Mth.wrapDegrees(f2), -85, 85);
                        Yaoshou009Entity.this.setXRot(this.rotlerp(Yaoshou009Entity.this.getXRot(), f2, 5));
                        float f3 = Mth.cos(Yaoshou009Entity.this.getXRot() * (float) (Math.PI / 180.0));
                        Yaoshou009Entity.this.setZza(f3 * f1);
                        Yaoshou009Entity.this.setYya((float) (f1 * dy));
                    } else {
                        Yaoshou009Entity.this.setSpeed(f1 * 0.05F);
                    }
                } else {
                    Yaoshou009Entity.this.setSpeed(0);
                    Yaoshou009Entity.this.setYya(0);
                    Yaoshou009Entity.this.setZza(0);
                }
            }
        };
    }

    @Override
    protected void sourceSpawn(ServerLevelAccessor level) {
        if (YaoshouShared.initialize(this, 8, "平庸", "勉强", "平庸", "贫弱", "贫弱", "贫弱", "勉强", "强劲", "勉强", "强劲", "贫弱", "贫弱", "平庸")) {
            getAttribute(NeoForgeMod.SWIM_SPEED).setBaseValue(getPersistentData().getDouble("speed_number") * 6.5);
        }
    }

    @Override
    protected void sourceTick() {
        Yaoshou009Behavior.tick(level(), this);
    }

    @Override
    protected void sourceHurt(DamageSource source, float amount) {
        Yaoshou009Behavior.hurt(this, source.getEntity());
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
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.DROWN)) {
            sourceHurt(source, amount);
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public SoundEvent getHurtSound(DamageSource ds) {
        return Yaoshou037054Support.sound("entity.puffer_fish.hurt");
    }

    @Override
    public SoundEvent getDeathSound() {
        return Yaoshou037054Support.sound("entity.puffer_fish.death");
    }

    @Override
    public void playStepSound(BlockPos pos, BlockState blockIn) {
        this.playSound(Yaoshou037054Support.sound("entity.fish.swim"), 0.15f, 1);
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public boolean checkSpawnObstruction(LevelReader world) {
        return world.isUnobstructed(this);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WaterBoundPathNavigation(this, level);
    }

    @Override
    protected void registerGoals() {

    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        sourceAnimations.register(this, controllers, 4, "animation.yaoshou009.", state -> {
            if (procedureAnimation().equals("empty")) {
                if ((state.isMoving() || !(state.getLimbSwingAmount() > -0.15F && state.getLimbSwingAmount() < 0.15F))
                        && !isAggressive()) {
                    return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou009.walk"));
                }
                if (isDeadOrDying()) {
                    return state.setAndContinue(RawAnimation.begin().then("animation.yaoshou009.death", Animation.LoopType.PLAY_ONCE));
                }
                if (isInWaterOrBubble()) {
                    return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou009.swimming"));
                }
                if (isSprinting()) {
                    return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou009.sprint"));
                }
                if (isAggressive() && state.isMoving()) {
                    return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou009.attacking"));
                }
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou009.idle"));
            }
            return PlayState.STOP;
        });
    }
}
