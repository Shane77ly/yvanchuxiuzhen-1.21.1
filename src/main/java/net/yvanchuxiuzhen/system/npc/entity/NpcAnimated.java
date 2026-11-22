package net.yvanchuxiuzhen.system.npc.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.platform.ModId;
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

/** 来源动画商人与大管家，保留行走、受击、管家近战及二百刻死亡表现。 */
public final class NpcAnimated extends NpcMob implements GeoEntity, AnimatedLook {
    private static final EntityDataAccessor<String> TEXTURE = SynchedEntityData.defineId(NpcAnimated.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> ANIMATION = SynchedEntityData.defineId(NpcAnimated.class, EntityDataSerializers.STRING);
    private final AnimatableInstanceCache animations = GeckoLibUtil.createInstanceCache(this);
    private final EntityModelRow model;
    private String procedure = "empty";
    private boolean procedureStarted;
    private boolean swinging;
    private long lastSwing;

    public NpcAnimated(EntityType<? extends NpcAnimated> type, Level level, EntityModelRow model, CompoundTag profile) {
        super(type, level, profile);
        this.model = model;
        entityData.set(TEXTURE, model.texture());
    }

    @Override protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(TEXTURE, "");
        builder.define(ANIMATION, "undefined");
    }

    @Override public void addAdditionalSaveData(CompoundTag data) {
        super.addAdditionalSaveData(data);
        data.putString("Texture", texture());
    }

    @Override public void readAdditionalSaveData(CompoundTag data) {
        super.readAdditionalSaveData(data);
        if (data.contains("Texture")) entityData.set(TEXTURE, data.getString("Texture"));
    }

    @Override public String texture() { return entityData.get(TEXTURE); }

    @Override protected void sourceHurt() {
        if (kind().equals("daguanjia")) entityData.set(ANIMATION, "animation.dahuguanjia.attacked");
        if (!kind().equals("businessman_04") || !isAlive() || hasEffect(BuiltInRegistries.MOB_EFFECT
                .getHolder(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "skilleffect_08")).orElseThrow())) return;
        CompoundTag data = getPersistentData();
        if (!data.getString("jineng").isEmpty()) {
            entityData.set(ANIMATION, "empty");
            data.putDouble("entitytick", 0);
            data.putString("jineng", "");
            data.putString("jinengjinxing", "");
            data.putDouble("entitytick2", 0);
        }
        entityData.set(ANIMATION, "animation.businessman04.attacked");
    }

    @Override public void baseTick() {
        super.baseTick();
        refreshDimensions();
        String request = entityData.get(ANIMATION);
        if (!request.equals("undefined")) {
            entityData.set(ANIMATION, "undefined");
            procedure = request;
            procedureStarted = false;
        }
    }

    @Override protected void tickDeath() {
        if (++deathTime == model.lifecycle().deathTicks()) { remove(RemovalReason.KILLED); dropExperience(this); }
    }

    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<NpcAnimated>(this, "movement", model.lifecycle().animationTransitionTicks(), state -> {
            if (!procedure.equals("empty")) return PlayState.STOP;
            boolean moving = state.isMoving() || !(state.getLimbSwingAmount() > -.15F && state.getLimbSwingAmount() < .15F);
            if (moving && (!kind().equals("daguanjia") || !isAggressive()))
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation." + model.animation() + ".walk"));
            if (kind().equals("daguanjia")) {
                if (isDeadOrDying()) return state.setAndContinue(RawAnimation.begin().thenPlay("animation.dahuguanjia.die"));
                if (isAggressive() && state.isMoving()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.dahuguanjia.attacking"));
            }
            return state.setAndContinue(RawAnimation.begin().thenLoop(model.idle()));
        }));
        if (kind().equals("daguanjia")) {
            controllers.add(new AnimationController<NpcAnimated>(this, "attacking", 4, state -> {
                if (getAttackAnim(state.getPartialTick()) > 0 && !swinging) { swinging = true; lastSwing = level().getGameTime(); }
                if (swinging && lastSwing + 15 <= level().getGameTime()) swinging = false;
                if (swinging && state.getController().getAnimationState() == AnimationController.State.STOPPED) {
                    state.getController().forceAnimationReset();
                    return state.setAndContinue(RawAnimation.begin().thenPlay("animation.dahuguanjia.attack"));
                }
                return PlayState.CONTINUE;
            }));
        }
        controllers.add(new AnimationController<NpcAnimated>(this, "procedure", model.lifecycle().animationTransitionTicks(), state -> {
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
