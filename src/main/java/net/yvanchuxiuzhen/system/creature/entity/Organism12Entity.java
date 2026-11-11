package net.yvanchuxiuzhen.system.creature.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.yvanchuxiuzhen.platform.content.entity.AnimatedLook;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/** Organism12：水中游动、生成时提高游速；右键令自身进入潜行姿态。 */
public final class Organism12Entity extends PathfinderMob implements GeoEntity, AnimatedLook {
    private static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(Organism12Entity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(Organism12Entity.class, EntityDataSerializers.STRING);
    private final EntityModelRow model;
    private final AnimatableInstanceCache animations = GeckoLibUtil.createInstanceCache(this);
    private String procedureAnimation = "empty";

    public Organism12Entity(EntityType<? extends Organism12Entity> type, Level level, EntityModelRow model) {
        super(type, level);
        this.model = model;
        xpReward = 0;
        setPathfindingMalus(PathType.WATER, 0);
        moveControl = new MoveControl(this) {
            @Override
            public void tick() {
                if (Organism12Entity.this.isInWater()) setDeltaMovement(getDeltaMovement().add(0, 0.005, 0));
                if (operation == Operation.MOVE_TO && !getNavigation().isDone()) {
                    double dx = wantedX - getX(), dy = wantedY - getY(), dz = wantedZ - getZ();
                    float yaw = (float) (Mth.atan2(dz, dx) * (180 / Math.PI)) - 90;
                    float speed = (float) (speedModifier * getAttributeValue(Attributes.MOVEMENT_SPEED));
                    setYRot(rotlerp(getYRot(), yaw, 10));
                    yBodyRot = getYRot();
                    yHeadRot = getYRot();
                    if (isInWater()) {
                        setSpeed((float) getAttributeValue(Attributes.MOVEMENT_SPEED));
                        float pitch = -(float) (Mth.atan2(dy, (float) Math.sqrt(dx * dx + dz * dz)) * (180 / Math.PI));
                        pitch = Mth.clamp(Mth.wrapDegrees(pitch), -85, 85);
                        setXRot(rotlerp(getXRot(), pitch, 5));
                        float horizontal = Mth.cos(getXRot() * (float) (Math.PI / 180));
                        setZza(horizontal * speed);
                        setYya((float) (speed * dy));
                    } else {
                        setSpeed(speed * 0.05F);
                    }
                } else {
                    setSpeed(0);
                    setYya(0);
                    setZza(0);
                }
            }
        };
        entityData.set(TEXTURE, model.texture());
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TEXTURE, "");
        builder.define(ANIMATION, "undefined");
    }
    @Override protected PathNavigation createNavigation(Level level) { return new WaterBoundPathNavigation(this, level); }
    @Override protected void registerGoals() { super.registerGoals(); goalSelector.addGoal(1, new RandomSwimmingGoal(this, 1, 40)); }
    public static AttributeSupplier.Builder attributes(EntityModelRow row) {
        return row.attributes().add(NeoForgeMod.SWIM_SPEED, row.lifecycle().swimSpeed().orElseThrow());
    }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.GENERIC_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.GENERIC_DEATH; }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return !source.is(DamageTypes.CACTUS) && !source.is(DamageTypes.DROWN) && super.hurt(source, amount);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
            MobSpawnType reason, SpawnGroupData data) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data);
        getAttribute(NeoForgeMod.SWIM_SPEED).setBaseValue(2);
        return result;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        super.mobInteract(player, hand);
        setShiftKeyDown(true);
        return level().isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
    }

    @Override public void baseTick() { super.baseTick(); refreshDimensions(); }
    @Override public boolean checkSpawnObstruction(LevelReader level) { return level.isUnobstructed(this); }
    @Override public boolean isPushedByFluid() { return false; }
    @Override public void aiStep() { super.aiStep(); updateSwingTime(); }
    @Override protected void tickDeath() { if (++deathTime == 20) remove(RemovalReason.KILLED); }
    @Override public String texture() { return entityData.get(TEXTURE); }
    public void setTexture(String texture) { entityData.set(TEXTURE, texture); }
    public void setAnimation(String animation) { entityData.set(ANIMATION, animation); }

    @Override
    public void tick() {
        String request = entityData.get(ANIMATION);
        if (!request.equals("undefined")) { entityData.set(ANIMATION, "undefined"); procedureAnimation = request; }
        super.tick();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<Organism12Entity>(this, "movement", 4, state -> {
            if (!procedureAnimation.equals("empty")) return PlayState.STOP;
            if (state.isMoving()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.organism12.walk"));
            if (isShiftKeyDown()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.organism12.sneak"));
            return state.setAndContinue(RawAnimation.begin().thenLoop(model.idle()));
        }));
        controllers.add(new AnimationController<Organism12Entity>(this, "procedure", 4, state -> {
            AnimationController<Organism12Entity> controller = state.getController();
            if (!procedureAnimation.equals("empty") && controller.getAnimationState() == AnimationController.State.STOPPED) {
                controller.setAnimation(RawAnimation.begin().then(procedureAnimation, Animation.LoopType.PLAY_ONCE));
                if (controller.getAnimationState() == AnimationController.State.STOPPED) { procedureAnimation = "empty"; controller.forceAnimationReset(); }
            }
            return PlayState.CONTINUE;
        }));
    }
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return animations; }
}
