package net.yvanchuxiuzhen.system.guchong.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.MobSpawnType;
import net.yvanchuxiuzhen.platform.content.entity.AnimatedLook;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/** 蛊虫实体公共生命周期；具体编号只提供来源属性、目标类型和动画分支。 */
public abstract class GuchongEntity extends TamableAnimal implements GeoEntity, AnimatedLook {
    private static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(GuchongEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(GuchongEntity.class, EntityDataSerializers.STRING);
    private final AnimatableInstanceCache animations = GeckoLibUtil.createInstanceCache(this);
    private final EntityModelRow model;
    private String procedureAnimation = "empty";

    protected GuchongEntity(EntityType<? extends GuchongEntity> type, Level level, EntityModelRow model) {
        super(type, level);
        this.model = model;
        entityData.set(TEXTURE, model.texture());
        xpReward = 0;
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder attributes() {
        return TamableAnimal.createMobAttributes().add(Attributes.MOVEMENT_SPEED, .3).add(Attributes.MAX_HEALTH, 10)
                .add(Attributes.ARMOR, 0).add(Attributes.ATTACK_DAMAGE, 3).add(Attributes.FOLLOW_RANGE, 16);
    }

    protected final EntityModelRow model() { return model; }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TEXTURE, "");
        builder.define(ANIMATION, "undefined");
    }

    @Override protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(1, new GuchongFollowOwnerGoal(this, 1, 10, 2, false));
        targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        goalSelector.addGoal(3, new OwnerHurtByTargetGoal(this));
        targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Player.class, true, true));
        goalSelector.addGoal(6, new MeleeAttackGoal(this, 1.2, false) {
            @Override protected boolean canPerformAttack(LivingEntity target) {
                return isTimeToAttack() && distanceToSqr(target) <= getBbWidth() * getBbWidth() + target.getBbWidth();
            }
        });
        targetSelector.addGoal(7, new HurtByTargetGoal(this).setAlertOthers());
        goalSelector.addGoal(9, new RandomStrollGoal(this, 1));
        goalSelector.addGoal(10, new RandomLookAroundGoal(this));
        goalSelector.addGoal(11, new FloatGoal(this));
    }

    @Override public boolean removeWhenFarAway(double distance) { return false; }
    @Override public boolean isFood(ItemStack stack) { return false; }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.GENERIC_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.GENERIC_DEATH; }

    @Override public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof SpawnEggItem) return super.mobInteract(player, hand);
        if (level().isClientSide()) return isTame() && isOwnedBy(player) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        if (isTame()) return isOwnedBy(player) ? super.mobInteract(player, hand) : InteractionResult.CONSUME;
        InteractionResult result = super.mobInteract(player, hand);
        if (result.consumesAction()) setPersistenceRequired();
        return result;
    }

    @Override public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.FALL) && ignoresFallDamage()) return false;
        return super.hurt(source, amount);
    }

    protected boolean ignoresFallDamage() { return false; }

    @Override public void die(DamageSource source) { super.die(source); GuchongBehavior.died(this); }

    @Override public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
            SpawnGroupData data) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data);
        initializeSourceData();
        return result;
    }

    protected abstract void initializeSourceData();
    protected abstract void sourceTick();

    @Override public void baseTick() {
        super.baseTick();
        sourceTick();
        refreshDimensions();
        String request = entityData.get(ANIMATION);
        if (!request.equals("undefined")) {
            entityData.set(ANIMATION, "undefined");
            procedureAnimation = request;
        }
    }

    @Override public String texture() { return entityData.get(TEXTURE); }
    public void setAnimation(String animation) { entityData.set(ANIMATION, animation); }

    @Override protected void tickDeath() {
        if (++deathTime == 20) { remove(RemovalReason.KILLED); dropExperience(this); }
    }

    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<GuchongEntity>(this, "movement", 2, state -> {
            if (!procedureAnimation.equals("empty")) return PlayState.STOP;
            boolean moving = state.isMoving() || !(state.getLimbSwingAmount() > -.15F && state.getLimbSwingAmount() < .15F);
            if (moving && (onGround() || model.air().isEmpty()))
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation." + model.animation() + ".walk"));
            if (!onGround() && model.air().isPresent())
                return state.setAndContinue(RawAnimation.begin().thenLoop(model.air().get()));
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation." + model.animation() + ".idle"));
        }));
        controllers.add(new AnimationController<GuchongEntity>(this, "procedure", 2, state -> {
            if (procedureAnimation.equals("empty")) return PlayState.STOP;
            AnimationController<GuchongEntity> controller = state.getController();
            if (controller.getAnimationState() == AnimationController.State.STOPPED) {
                controller.setAnimation(RawAnimation.begin().then(procedureAnimation, Animation.LoopType.PLAY_ONCE));
                procedureAnimation = "empty";
            }
            return PlayState.CONTINUE;
        }));
    }

    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return animations; }

    @Override public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        GuchongEntity child = (GuchongEntity) getType().create(level);
        child.finalizeSpawn(level, level.getCurrentDifficultyAt(child.blockPosition()), MobSpawnType.BREEDING, null);
        return child;
    }
}
