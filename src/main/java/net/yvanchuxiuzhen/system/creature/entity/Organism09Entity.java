package net.yvanchuxiuzhen.system.creature.entity;

import java.util.EnumSet;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
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

/** Organism09：无重力飞行、玩家目标与主人受击反击；来源食物列表为空。 */
public final class Organism09Entity extends TamableAnimal implements GeoEntity, AnimatedLook {
    private static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(Organism09Entity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(Organism09Entity.class, EntityDataSerializers.STRING);
    private final EntityModelRow model;
    private final AnimatableInstanceCache animations = GeckoLibUtil.createInstanceCache(this);
    private String procedureAnimation = "empty";

    public Organism09Entity(EntityType<? extends Organism09Entity> type, Level level, EntityModelRow model) {
        super(type, level);
        this.model = model;
        xpReward = 0;
        moveControl = new FlyingMoveControl(this, 10, true);
        entityData.set(TEXTURE, model.texture());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TEXTURE, "");
        builder.define(ANIMATION, "undefined");
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new FlyingPathNavigation(this, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(1, new ContactAttack());
        goalSelector.addGoal(2, new RandomStrollGoal(this, 0.8, 20) {
            @Override
            protected Vec3 getPosition() {
                RandomSource random = getRandom();
                return new Vec3(getX() + (random.nextFloat() * 2 - 1) * 16,
                        getY() + (random.nextFloat() * 2 - 1) * 16, getZ() + (random.nextFloat() * 2 - 1) * 16);
            }
        });
        goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.2, false) {
            @Override
            protected boolean canPerformAttack(LivingEntity target) {
                return isTimeToAttack() && Organism09Entity.this.distanceToSqr(target.getX(), target.getY(), target.getZ())
                        <= Organism09Entity.this.getBbWidth() * Organism09Entity.this.getBbWidth() + target.getBbWidth();
            }
        });
        goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Player.class, false, false));
        targetSelector.addGoal(6, new HurtByTargetGoal(this));
        targetSelector.addGoal(7, new OwnerHurtTargetGoal(this));
        goalSelector.addGoal(8, new OwnerHurtByTargetGoal(this));
        goalSelector.addGoal(9, new SourceFollowOwnerGoal(this, 1, 10, 2, false));
    }

    public static AttributeSupplier.Builder attributes(EntityModelRow row) {
        return row.attributes().add(Attributes.FLYING_SPEED, row.lifecycle().flyingSpeed().orElseThrow());
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return List.of().contains(stack.getItem());
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof SpawnEggItem) return super.mobInteract(player, hand);
        if (level().isClientSide()) return isTame() && isOwnedBy(player) || isFood(stack) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        if (isTame()) return isOwnedBy(player) ? super.mobInteract(player, hand) : InteractionResult.CONSUME;
        InteractionResult result = super.mobInteract(player, hand);
        if (result.consumesAction()) setPersistenceRequired();
        return result;
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        Organism09Entity offspring = (Organism09Entity) getType().create(level);
        offspring.finalizeSpawn(level, level.getCurrentDifficultyAt(offspring.blockPosition()), MobSpawnType.BREEDING, null);
        return offspring;
    }

    @Override
    public void tick() {
        String request = entityData.get(ANIMATION);
        if (!request.equals("undefined")) {
            entityData.set(ANIMATION, "undefined");
            procedureAnimation = request;
        }
        super.tick();
        refreshDimensions();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        updateSwingTime();
        setNoGravity(true);
    }

    @Override
    public void setNoGravity(boolean ignored) {
        super.setNoGravity(true);
    }

    @Override
    protected void checkFallDamage(double distance, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return !source.is(DamageTypes.FALL) && super.hurt(source, amount);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.GENERIC_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GENERIC_DEATH;
    }

    @Override
    protected void tickDeath() {
        if (++deathTime == 20) remove(RemovalReason.KILLED);
    }

    @Override public String texture() { return entityData.get(TEXTURE); }
    public void setTexture(String texture) { entityData.set(TEXTURE, texture); }
    public void setAnimation(String animation) { entityData.set(ANIMATION, animation); }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<Organism09Entity>(this, "movement", 4,
                state -> procedureAnimation.equals("empty") ? state.setAndContinue(RawAnimation.begin().thenLoop(model.idle())) : PlayState.STOP));
        controllers.add(new AnimationController<Organism09Entity>(this, "procedure", 4, state -> {
            AnimationController<Organism09Entity> controller = state.getController();
            if (!procedureAnimation.equals("empty") && controller.getAnimationState() == AnimationController.State.STOPPED) {
                controller.setAnimation(RawAnimation.begin().then(procedureAnimation, Animation.LoopType.PLAY_ONCE));
                if (controller.getAnimationState() == AnimationController.State.STOPPED) {
                    procedureAnimation = "empty";
                    controller.forceAnimationReset();
                }
            }
            return PlayState.CONTINUE;
        }));
    }

    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return animations; }

    private final class ContactAttack extends Goal {
        private ContactAttack() { setFlags(EnumSet.of(Flag.MOVE)); }
        @Override public boolean canUse() { return getTarget() != null && !moveControl.hasWanted(); }
        @Override public boolean canContinueToUse() { return moveControl.hasWanted() && getTarget() != null && getTarget().isAlive(); }
        @Override public void start() {
            Vec3 eye = getTarget().getEyePosition(1);
            moveControl.setWantedPosition(eye.x, eye.y, eye.z, 1);
        }
        @Override public void tick() {
            LivingEntity target = getTarget();
            if (getBoundingBox().intersects(target.getBoundingBox())) doHurtTarget(target);
            else if (distanceToSqr(target) < 16) {
                Vec3 eye = target.getEyePosition(1);
                moveControl.setWantedPosition(eye.x, eye.y, eye.z, 1);
            }
        }
    }
}
