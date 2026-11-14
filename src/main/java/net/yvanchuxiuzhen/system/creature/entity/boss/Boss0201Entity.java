package net.yvanchuxiuzhen.system.creature.entity.boss;

import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
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

/** Boss02技能生成的斧刃实体，保留来源飞斧与八方斩两种运动。 */
public final class Boss0201Entity extends PathfinderMob implements GeoEntity, AnimatedLook {
    private static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(Boss0201Entity.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(Boss0201Entity.class, EntityDataSerializers.STRING);
    private final EntityModelRow model;
    private final AnimatableInstanceCache animations = GeckoLibUtil.createInstanceCache(this);
    private String procedureAnimation = "empty";

    public Boss0201Entity(EntityType<? extends Boss0201Entity> type, Level level, EntityModelRow model) {
        super(type, level);
        this.model = model;
        entityData.set(TEXTURE, model.texture());
        xpReward = 0;
        setPersistenceRequired();
        moveControl = new FlyingMoveControl(this, 10, true);
    }

    public static AttributeSupplier.Builder attributes(EntityModelRow row) {
        return row.attributes().add(Attributes.FLYING_SPEED, row.lifecycle().flyingSpeed().orElseThrow())
                .add(NeoForgeMod.SWIM_SPEED, row.lifecycle().swimSpeed().orElseThrow());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TEXTURE, "ycxzboss_02texiao");
        builder.define(ANIMATION, "undefined");
    }

    @Override protected PathNavigation createNavigation(Level level) { return new FlyingPathNavigation(this, level); }
    @Override public boolean removeWhenFarAway(double distance) { return false; }
    @Override public boolean causeFallDamage(float distance, float multiplier, DamageSource source) { return false; }
    @Override public boolean checkSpawnObstruction(LevelReader level) { return level.isUnobstructed(this); }
    @Override public boolean isPushedByFluid() { return false; }
    @Override protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) { }

    @Override public void setNoGravity(boolean ignored) { super.setNoGravity(true); }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return !(source.getDirectEntity() instanceof AbstractArrow) && !source.is(DamageTypes.FALL)
                && !source.is(DamageTypes.CACTUS) && !source.is(DamageTypes.DROWN) && super.hurt(source, amount);
    }

    @Override
    public void baseTick() {
        String request = entityData.get(ANIMATION);
        if (!request.equals("undefined")) {
            entityData.set(ANIMATION, "undefined");
            procedureAnimation = request;
        }
        super.baseTick();
        Boss02Behavior.tickAxe(this);
        refreshDimensions();
    }

    @Override public void aiStep() { super.aiStep(); setNoGravity(true); }
    @Override protected void tickDeath() { if (++deathTime == model.lifecycle().deathTicks()) { remove(RemovalReason.KILLED); dropExperience(this); } }
    @Override public String texture() { return entityData.get(TEXTURE); }
    public void setAnimation(String animation) { entityData.set(ANIMATION, animation); }
    public String getSyncedAnimation() { return entityData.get(ANIMATION); }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<Boss0201Entity>(this, "movement", 0, state -> {
            if (!procedureAnimation.equals("empty")) return PlayState.STOP;
            return state.setAndContinue(RawAnimation.begin().thenLoop(model.idle()));
        }));
        controllers.add(new AnimationController<Boss0201Entity>(this, "procedure", 0, state -> {
            AnimationController<Boss0201Entity> controller = state.getController();
            String animation = procedureAnimation;
            if (!animation.equals("empty") && controller.getAnimationState() == AnimationController.State.STOPPED) {
                controller.setAnimation(RawAnimation.begin().then(animation, Animation.LoopType.PLAY_ONCE));
                if (controller.getAnimationState() == AnimationController.State.STOPPED) {
                    procedureAnimation = "empty";
                    controller.forceAnimationReset();
                }
            }
            return PlayState.CONTINUE;
        }));
    }

    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return animations; }
}
