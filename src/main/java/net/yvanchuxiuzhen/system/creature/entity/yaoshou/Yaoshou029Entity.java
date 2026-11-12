package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
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

/** 029号妖兽的独立编号身份、来源移动、声音、动画与生命周期。 */
public final class Yaoshou029Entity extends YaoshouEntity {
    private final Yaoshou037054Animations sourceAnimations = new Yaoshou037054Animations();

    public Yaoshou029Entity(EntityType<? extends YaoshouEntity> type, Level level, EntityModelRow model) {
        super(type, level, model);
        xpReward = 0;
        this.setPathfindingMalus(PathType.WATER, 0);
        this.moveControl = new MoveControl(this) {
            @Override
            public void tick() {
                if (Yaoshou029Entity.this.isInWater())
                    Yaoshou029Entity.this.setDeltaMovement(Yaoshou029Entity.this.getDeltaMovement().add(0, 0.005, 0));
                if (this.operation == MoveControl.Operation.MOVE_TO && !Yaoshou029Entity.this.getNavigation().isDone()) {
                    double dx = this.wantedX - Yaoshou029Entity.this.getX();
                    double dy = this.wantedY - Yaoshou029Entity.this.getY();
                    double dz = this.wantedZ - Yaoshou029Entity.this.getZ();
                    float f = (float) (Mth.atan2(dz, dx) * (double) (180 / Math.PI)) - 90;
                    float f1 = (float) (this.speedModifier * Yaoshou029Entity.this.getAttribute(Attributes.MOVEMENT_SPEED).getValue());
                    Yaoshou029Entity.this.setYRot(this.rotlerp(Yaoshou029Entity.this.getYRot(), f, 10));
                    Yaoshou029Entity.this.yBodyRot = Yaoshou029Entity.this.getYRot();
                    Yaoshou029Entity.this.yHeadRot = Yaoshou029Entity.this.getYRot();
                    if (Yaoshou029Entity.this.isInWater()) {
                        Yaoshou029Entity.this.setSpeed((float) Yaoshou029Entity.this.getAttribute(Attributes.MOVEMENT_SPEED).getValue());
                        float f2 = -(float) (Mth.atan2(dy, (float) Math.sqrt(dx * dx + dz * dz)) * (180 / Math.PI));
                        f2 = Mth.clamp(Mth.wrapDegrees(f2), -85, 85);
                        Yaoshou029Entity.this.setXRot(this.rotlerp(Yaoshou029Entity.this.getXRot(), f2, 5));
                        float f3 = Mth.cos(Yaoshou029Entity.this.getXRot() * (float) (Math.PI / 180.0));
                        Yaoshou029Entity.this.setZza(f3 * f1);
                        Yaoshou029Entity.this.setYya((float) (f1 * dy));
                    } else {
                        Yaoshou029Entity.this.setSpeed(f1 * 0.05F);
                    }
                } else {
                    Yaoshou029Entity.this.setSpeed(0);
                    Yaoshou029Entity.this.setYya(0);
                    Yaoshou029Entity.this.setZza(0);
                }
            }
        };
    }

    @Override
    protected void sourceSpawn(ServerLevelAccessor level) {
        if (YaoshouShared.initialize(this, 14, "勉强", "勉强", "平庸", "勉强", "勉强", "贫弱", "勉强", "勉强", "强劲", "勉强", "勉强", "勉强", "平庸")) {
            getAttribute(NeoForgeMod.SWIM_SPEED).setBaseValue(getPersistentData().getDouble("speed_number") * 6.5);
        }
    }

    @Override
    protected void sourceTick() {
        Yaoshou029Behavior.tick(level(), this);
    }

    @Override
    protected void sourceHurt(DamageSource source, float amount) {
        Yaoshou037054Support.hurt(this, source, "animation.yaoshou029.attacked");
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
        return Yaoshou037054Support.sound("entity.puffer_fish.ambient");
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
        sourceAnimations.register(this, controllers, 4, "animation.yaoshou029.", state -> {
            if (procedureAnimation().equals("empty")) {
                if ((state.isMoving() || !(state.getLimbSwingAmount() > -0.15F && state.getLimbSwingAmount() < 0.15F))
                ) {
                    return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou029.swim"));
                }
                if (isDeadOrDying()) {
                    return state.setAndContinue(RawAnimation.begin().then("animation.yaoshou029.die", Animation.LoopType.PLAY_ONCE));
                }
                if (isInWaterOrBubble()) {
                    return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou029.swim"));
                }
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou029.idle"));
            }
            return PlayState.STOP;
        });
    }
}
