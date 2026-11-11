package net.yvanchuxiuzhen.system.creature.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.EventHooks;
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

/** Organism11：受伤时立即疾跑，随后按来源逐刻维持40刻。 */
public final class Organism11Entity extends TamableAnimal implements GeoEntity, AnimatedLook {
    private static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(Organism11Entity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(Organism11Entity.class, EntityDataSerializers.STRING);
    private final EntityModelRow model;
    private final AnimatableInstanceCache animations = GeckoLibUtil.createInstanceCache(this);
    private String procedureAnimation = "empty";

    public Organism11Entity(EntityType<? extends Organism11Entity> type, Level level, EntityModelRow model) {
        super(type, level);
        this.model = model;
        xpReward = 0;
        entityData.set(TEXTURE, model.texture());
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TEXTURE, "");
        builder.define(ANIMATION, "undefined");
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(1, new PanicGoal(this, 1.5));
        goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 6));
        goalSelector.addGoal(3, new RandomStrollGoal(this, 1));
        goalSelector.addGoal(4, new RandomSwimmingGoal(this, 1, 40));
        goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        goalSelector.addGoal(6, new FloatGoal(this));
    }

    public static AttributeSupplier.Builder attributes(EntityModelRow row) { return row.attributes(); }
    @Override protected SoundEvent getAmbientSound() { return Organisms.ambient11(); }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.GENERIC_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.GENERIC_DEATH; }
    @Override public boolean isFood(ItemStack stack) { return stack.is(Items.COOKED_COD); }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof SpawnEggItem) return super.mobInteract(player, hand);
        if (level().isClientSide()) return isTame() && isOwnedBy(player) || isFood(stack) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        if (isTame()) {
            if (!isOwnedBy(player)) return InteractionResult.CONSUME;
            if (isFood(stack) && getHealth() < getMaxHealth()) {
                FoodProperties food = stack.getFoodProperties(this);
                usePlayerItem(player, hand, stack);
                heal(food == null ? 4 : food.nutrition());
                return InteractionResult.CONSUME;
            }
            return super.mobInteract(player, hand);
        }
        if (isFood(stack)) {
            usePlayerItem(player, hand, stack);
            if (random.nextInt(3) == 0 && !EventHooks.onAnimalTame(this, player)) {
                tame(player);
                level().broadcastEntityEvent(this, (byte) 7);
            } else {
                level().broadcastEntityEvent(this, (byte) 6);
            }
            setPersistenceRequired();
            return InteractionResult.CONSUME;
        }
        InteractionResult result = super.mobInteract(player, hand);
        if (result.consumesAction()) setPersistenceRequired();
        return result;
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        Organism11Entity offspring = (Organism11Entity) getType().create(level);
        offspring.finalizeSpawn(level, level.getCurrentDifficultyAt(offspring.blockPosition()), MobSpawnType.BREEDING, null);
        return offspring;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        setSprinting(true);
        getPersistentData().putBoolean("jipao", true);
        return !source.is(DamageTypes.FALL) && super.hurt(source, amount);
    }

    @Override
    public void baseTick() {
        super.baseTick();
        if (!level().isClientSide()) {
            var data = getPersistentData();
            if (data.getBoolean("jipao")) {
                double tick = data.getDouble("jipaotick") + 1;
                if (tick >= 40) {
                    tick = 0;
                    data.putBoolean("jipao", false);
                    setSprinting(false);
                }
                data.putDouble("jipaotick", tick);
            }
        }
        refreshDimensions();
    }

    @Override public void tick() {
        String request = entityData.get(ANIMATION);
        if (!request.equals("undefined")) { entityData.set(ANIMATION, "undefined"); procedureAnimation = request; }
        super.tick();
    }
    @Override protected void tickDeath() { if (++deathTime == 20) remove(RemovalReason.KILLED); }
    @Override public void aiStep() { super.aiStep(); updateSwingTime(); }
    @Override public String texture() { return entityData.get(TEXTURE); }
    public void setTexture(String texture) { entityData.set(TEXTURE, texture); }
    public void setAnimation(String animation) { entityData.set(ANIMATION, animation); }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<Organism11Entity>(this, "movement", model.lifecycle().animationTransitionTicks(), state -> {
            if (!procedureAnimation.equals("empty")) return PlayState.STOP;
            if (state.isMoving()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.organism11.walk"));
            if (isInWaterOrBubble()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.organism11.swiming"));
            if (isSprinting()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.organism11.spring"));
            return state.setAndContinue(RawAnimation.begin().thenLoop(model.idle()));
        }));
        controllers.add(new AnimationController<Organism11Entity>(this, "procedure", model.lifecycle().animationTransitionTicks(), state -> {
            AnimationController<Organism11Entity> controller = state.getController();
            if (!procedureAnimation.equals("empty") && controller.getAnimationState() == AnimationController.State.STOPPED) {
                controller.setAnimation(RawAnimation.begin().then(procedureAnimation, Animation.LoopType.PLAY_ONCE));
                if (controller.getAnimationState() == AnimationController.State.STOPPED) { procedureAnimation = "empty"; controller.forceAnimationReset(); }
            }
            return PlayState.CONTINUE;
        }));
    }
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return animations; }
}
