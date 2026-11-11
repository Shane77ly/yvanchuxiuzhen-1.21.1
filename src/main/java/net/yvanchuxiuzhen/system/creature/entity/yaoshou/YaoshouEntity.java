package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

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
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.yvanchuxiuzhen.platform.content.entity.AnimatedLook;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.creature.entity.SourceFollowOwnerGoal;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/** 编号妖兽共享来源实体身份、动画同步与原生生命周期。 */
public abstract class YaoshouEntity extends TamableAnimal implements GeoEntity, AnimatedLook {
    private static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(YaoshouEntity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(YaoshouEntity.class, EntityDataSerializers.STRING);
    private final EntityModelRow model;
    private final AnimatableInstanceCache animations = GeckoLibUtil.createInstanceCache(this);
    private String procedureAnimation = "empty";
    private boolean swinging;
    private long lastSwing;

    protected YaoshouEntity(EntityType<? extends YaoshouEntity> type, Level level, EntityModelRow model) {
        super(type, level);
        this.model = model;
        xpReward = 5;
        entityData.set(TEXTURE, model.texture());
    }

    public static AttributeSupplier.Builder attributes(EntityModelRow row) {
        AttributeSupplier.Builder attributes = row.attributes().add(YaoshouAttributes.SIZE);
        if (row.id().equals("yaoshou_001") || row.id().equals("yaoshou_002")) attributes.add(YaoshouAttributes.SKIN);
        row.lifecycle().flyingSpeed().ifPresent(value -> attributes.add(Attributes.FLYING_SPEED, value));
        row.lifecycle().swimSpeed().ifPresent(value -> attributes.add(NeoForgeMod.SWIM_SPEED, value));
        return attributes;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TEXTURE, "");
        builder.define(ANIMATION, "undefined");
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        goalSelector.addGoal(1, new SourceFollowOwnerGoal(this, 1, 10, 2, false));
        goalSelector.addGoal(2, new FloatGoal(this));
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
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
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
            SpawnGroupData data) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, data);
        sourceSpawn(level);
        refreshDimensions();
        return result;
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        YaoshouEntity offspring = (YaoshouEntity) getType().create(level);
        offspring.finalizeSpawn(level, level.getCurrentDifficultyAt(offspring.blockPosition()), MobSpawnType.BREEDING, null);
        return offspring;
    }

    @Override
    public void baseTick() {
        super.baseTick();
        sourceTick();
        refreshDimensions();
        restoreVariantTexture();
        String request = entityData.get(ANIMATION);
        if (!request.equals("undefined")) {
            entityData.set(ANIMATION, "undefined");
            procedureAnimation = request;
        }
    }

    @Override
    protected EntityDimensions getDefaultDimensions(Pose pose) {
        return super.getDefaultDimensions(pose).scale((float) getAttributeValue(YaoshouAttributes.SIZE));
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        sourceHurt(source, amount);
        return (!model.lifecycle().ignoresFallDamage() || !source.is(DamageTypes.FALL)) && super.hurt(source, amount);
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
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return model.lifecycle().ignoresFallDamage() ? false : super.causeFallDamage(distance, multiplier, source);
    }

    @Override
    protected void tickDeath() {
        if (++deathTime == model.lifecycle().deathTicks()) {
            remove(RemovalReason.KILLED);
            dropExperience(this);
        }
    }

    @Override public String texture() { return entityData.get(TEXTURE); }
    public void setTexture(String texture) { entityData.set(TEXTURE, texture); }
    public void setAnimation(String animation) { entityData.set(ANIMATION, animation); }
    public String getSyncedAnimation() { return entityData.get(ANIMATION); }
    protected String procedureAnimation() { return procedureAnimation; }
    protected void clearProcedureAnimation() { procedureAnimation = "empty"; }

    private void restoreVariantTexture() {
        if (!model.id().equals("yaoshou_001") && !model.id().equals("yaoshou_002")) return;
        double variant = getAttributeValue(YaoshouAttributes.SKIN);
        if (variant < 1) return;
        String path = switch (model.id()) {
            case "yaoshou_001" -> variant == 3 ? "yaoshou00103" : variant == 2 ? "yaoshou00102" : "yaoshou001";
            case "yaoshou_002" -> "yaoshou0020" + (int) variant;
            default -> model.texture();
        };
        if (!path.equals(texture())) setTexture(path);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<YaoshouEntity>(this, "movement", 0, state -> {
            if (!procedureAnimation.equals("empty")) return PlayState.STOP;
            boolean moving = state.isMoving() || !(state.getLimbSwingAmount() > -0.15F && state.getLimbSwingAmount() < 0.15F);
            String animation = movementAnimation(model, moving);
            return state.setAndContinue(isDeadOrDying() && model.lifecycle().deathAnimation().isPresent()
                    ? RawAnimation.begin().then(animation, Animation.LoopType.PLAY_ONCE)
                    : RawAnimation.begin().thenLoop(animation));
        }));
        controllers.add(new AnimationController<YaoshouEntity>(this, "attacking", 0, state -> {
            if (getAttackAnim(state.getPartialTick()) > 0 && !swinging) {
                swinging = true;
                lastSwing = level().getGameTime();
            }
            if (swinging && lastSwing + 15 <= level().getGameTime()) swinging = false;
            AnimationController<YaoshouEntity> controller = state.getController();
            if (swinging && controller.getAnimationState() == AnimationController.State.STOPPED) {
                controller.forceAnimationReset();
                controller.setAnimation(RawAnimation.begin().then("animation." + model.animation() + ".attack", Animation.LoopType.PLAY_ONCE));
            }
            return PlayState.CONTINUE;
        }));
        controllers.add(new AnimationController<YaoshouEntity>(this, "procedure", 0, state -> {
            AnimationController<YaoshouEntity> controller = state.getController();
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

    protected abstract void sourceSpawn(ServerLevelAccessor level);
    protected abstract void sourceTick();
    protected abstract void sourceHurt(DamageSource source, float amount);

    private String movementAnimation(EntityModelRow row, boolean moving) {
        if (moving && (row.id().equals("yaoshou_001") ? onGround() : true) && !isVehicle() && !isAggressive()) return "animation." + row.animation() + ".walk";
        if (isDeadOrDying() && row.lifecycle().deathAnimation().isPresent()) return row.lifecycle().deathAnimation().get();
        if (isInWaterOrBubble()) return "animation." + row.animation() + ".swim";
        if (isShiftKeyDown()) return "animation." + row.animation() + ".sneak";
        if (isSprinting()) return "animation." + row.animation() + ".sprint";
        if (row.id().equals("yaoshou_001") && !onGround()) return "animation." + row.animation() + ".fly";
        if (isVehicle() && moving) return "animation." + row.animation() + ".ride";
        if (isAggressive() && moving && !isVehicle()) return "animation." + row.animation() + ".attacking";
        return row.idle();
    }
}
