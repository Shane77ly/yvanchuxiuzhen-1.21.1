package net.yvanchuxiuzhen.system.npc.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.platform.content.ContentFiles;
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

/** 08号飞梭：原生单乘客交互、固定世界方向运动及首次载人阶段由实体自身维护。 */
public final class NpcFlyingShuttle extends PathfinderMob implements GeoEntity, AnimatedLook {
    private static final EntityModelRow MODEL = ContentFiles.read("content/npc/shuttle_model.json", EntityModelRow.CODEC).getFirst();
    private static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(NpcFlyingShuttle.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(NpcFlyingShuttle.class, EntityDataSerializers.STRING);
    private final AnimatableInstanceCache animations = GeckoLibUtil.createInstanceCache(this);
    private String procedure = "empty";
    private boolean procedureStarted;

    public NpcFlyingShuttle(EntityType<? extends NpcFlyingShuttle> type, Level level) {
        super(type, level);
        xpReward = 0;
        setPersistenceRequired();
        moveControl = new FlyingMoveControl(this, 10, true);
    }

    public static EntityModelRow model() { return MODEL; }

    public static AttributeSupplier.Builder attributes() {
        return MODEL.attributes().add(Attributes.FLYING_SPEED, MODEL.lifecycle().flyingSpeed().orElseThrow());
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TEXTURE, "feisuo");
        builder.define(ANIMATION, "undefined");
    }

    @Override protected PathNavigation createNavigation(Level level) { return new FlyingPathNavigation(this, level); }

    @Override protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 10));
    }

    @Override public boolean removeWhenFarAway(double distance) { return false; }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return null; }
    @Override protected SoundEvent getDeathSound() { return null; }
    @Override public boolean causeFallDamage(float distance, float multiplier, DamageSource source) { return false; }
    @Override protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) { }
    @Override public void setNoGravity(boolean ignored) { super.setNoGravity(true); }

    @Override public boolean hurt(DamageSource source, float amount) {
        if (source.getDirectEntity() instanceof AbstractArrow || source.getDirectEntity() instanceof Player
                || source.getDirectEntity() instanceof ThrownPotion || source.getDirectEntity() instanceof AreaEffectCloud
                || source.is(DamageTypes.FALL) || source.is(DamageTypes.CACTUS) || source.is(DamageTypes.DROWN)
                || source.is(DamageTypes.LIGHTNING_BOLT) || source.is(DamageTypeTags.IS_EXPLOSION)
                || source.getMsgId().equals("trident") || source.is(DamageTypes.FALLING_ANVIL) || source.is(DamageTypes.DRAGON_BREATH)
                || source.is(DamageTypes.WITHER) || source.getMsgId().equals("witherSkull")) return false;
        return super.hurt(source, amount);
    }

    @Override public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, SpawnGroupData data) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data);
        addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 1200, 0, false, false));
        return result;
    }

    @Override public InteractionResult mobInteract(Player player, InteractionHand hand) {
        InteractionResult result = InteractionResult.sidedSuccess(level().isClientSide());
        super.mobInteract(player, hand);
        player.startRiding(this);
        return result;
    }

    @Override public void tick() {
        String request = getSyncedAnimation();
        if (!request.equals("undefined")) {
            setAnimation("undefined");
            procedure = request;
            procedureStarted = false;
        }
        super.tick();
    }

    @Override public void baseTick() {
        super.baseTick();
        if (isVehicle()) {
            setDeltaMovement(new Vec3(0, .1, -.1));
            if (!getPersistentData().getBoolean("jieduan")) {
                removeEffect(MobEffects.SLOW_FALLING);
                getPersistentData().putBoolean("jieduan", true);
            }
        }
        refreshDimensions();
    }

    @Override public void aiStep() {
        super.aiStep();
        updateSwingTime();
        setNoGravity(true);
    }

    @Override protected void tickDeath() {
        if (++deathTime == 20) {
            remove(RemovalReason.KILLED);
            dropExperience(this);
        }
    }

    @Override public String texture() { return entityData.get(TEXTURE); }
    public void setTexture(String texture) { entityData.set(TEXTURE, texture); }
    public String getSyncedAnimation() { return entityData.get(ANIMATION); }
    public void setAnimation(String animation) { entityData.set(ANIMATION, animation); }

    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<NpcFlyingShuttle>(this, "movement", 4, state -> procedure.equals("empty")
                ? state.setAndContinue(RawAnimation.begin().thenLoop(MODEL.idle())) : PlayState.STOP));
        controllers.add(new AnimationController<NpcFlyingShuttle>(this, "procedure", 4, state -> {
            if (procedure.equals("empty")) return PlayState.STOP;
            if (!procedureStarted) {
                state.getController().forceAnimationReset();
                state.getController().setAnimation(RawAnimation.begin().then(procedure, Animation.LoopType.PLAY_ONCE));
                procedureStarted = true;
            } else if (state.getController().getAnimationState() == AnimationController.State.STOPPED) {
                procedure = "empty";
                procedureStarted = false;
                return PlayState.STOP;
            }
            return PlayState.CONTINUE;
        }));
    }

    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return animations; }
}
