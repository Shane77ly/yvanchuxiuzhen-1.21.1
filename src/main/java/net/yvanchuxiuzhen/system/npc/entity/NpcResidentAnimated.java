package net.yvanchuxiuzhen.system.npc.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
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

/** 21、22号居民的百刻微笑、来源动作顺序与七十九刻死亡表现。 */
public final class NpcResidentAnimated extends NpcResident implements GeoEntity, AnimatedLook {
    private static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(NpcResidentAnimated.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(NpcResidentAnimated.class, EntityDataSerializers.STRING);
    private final AnimatableInstanceCache animations = GeckoLibUtil.createInstanceCache(this);
    private final EntityModelRow model;
    private String procedure = "empty";
    private boolean procedureStarted;

    public NpcResidentAnimated(EntityType<? extends NpcResidentAnimated> type, Level level, EntityModelRow model) {
        super(type, level);
        this.model = model;
        entityData.set(TEXTURE, model.texture());
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TEXTURE, "");
        builder.define(ANIMATION, "undefined");
    }

    @Override public String texture() { return entityData.get(TEXTURE); }

    public String getSyncedAnimation() { return entityData.get(ANIMATION); }

    public void setAnimation(String animation) { entityData.set(ANIMATION, animation); }

    @Override public void baseTick() {
        super.baseTick();
        double ticks = getPersistentData().getDouble("tick") + 1;
        if (ticks >= 100) {
            if (getHealth() >= getMaxHealth() * .9) setAnimation("animation." + model.animation() + ".smile");
            ticks = 0;
        }
        getPersistentData().putDouble("tick", ticks);
        refreshDimensions();
    }

    @Override public void tick() {
        // 来源LivingUpdateEvent在LivingEntity.tick调用父类之前消费上一刻的同步动画。
        String request = getSyncedAnimation();
        if (!request.equals("undefined")) {
            setAnimation("undefined");
            procedure = request;
            procedureStarted = false;
        }
        super.tick();
    }

    @Override protected EntityDimensions getDefaultDimensions(Pose pose) { return super.getDefaultDimensions(pose).scale(.8F); }

    @Override public void aiStep() {
        super.aiStep();
        updateSwingTime();
    }

    @Override protected void tickDeath() {
        if (++deathTime == 79) { remove(RemovalReason.KILLED); dropExperience(this); }
    }

    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<NpcResidentAnimated>(this, "movement", model.lifecycle().animationTransitionTicks(), state -> {
            if (!procedure.equals("empty")) return PlayState.STOP;
            String prefix = "animation." + model.animation();
            boolean moving = state.isMoving() || !(state.getLimbSwingAmount() > -.15F && state.getLimbSwingAmount() < .15F);
            if (moving && !isVehicle()) return state.setAndContinue(RawAnimation.begin().thenLoop(prefix + ".walk"));
            if (isDeadOrDying()) return state.setAndContinue(RawAnimation.begin().thenPlay(prefix + ".death"));
            if (isInWaterOrBubble()) return state.setAndContinue(RawAnimation.begin().thenLoop(prefix + ".swimming"));
            if (isVehicle() && state.isMoving()) return state.setAndContinue(RawAnimation.begin().thenLoop(prefix + ".ride"));
            return state.setAndContinue(RawAnimation.begin().thenLoop(model.idle()));
        }));
        controllers.add(new AnimationController<NpcResidentAnimated>(this, "procedure", model.lifecycle().animationTransitionTicks(), state -> {
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
