package net.yvanchuxiuzhen.system.npc.entity;

import net.minecraft.world.entity.PathfinderMob;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/** 阴司实体的动作控制器，保留黑白无常与牛头马面的动作优先级差异。 */
final class NpcUnderworldAnimations<T extends PathfinderMob & GeoEntity> {
    private final T entity;
    private final EntityModelRow model;
    private final AnimatableInstanceCache cache;
    private final boolean judge;
    private final boolean wuchang;
    private final String prefix;
    private String procedure = "empty";
    private boolean procedureStarted;
    private boolean swinging;
    private long lastSwing;

    NpcUnderworldAnimations(T entity, EntityModelRow model) {
        this.entity = entity;
        this.model = model;
        cache = GeckoLibUtil.createInstanceCache(entity);
        judge = model.id().equals("yanwang");
        wuchang = model.id().equals("wuchangbai") || model.id().equals("wuchanghei");
        prefix = wuchang ? "animation.baiwuchang" : "animation.mamian";
    }

    void request(String animation) {
        procedure = animation;
        procedureStarted = false;
    }

    AnimatableInstanceCache cache() { return cache; }

    void register(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<T>(entity, "movement", model.lifecycle().animationTransitionTicks(), state -> {
            if (!procedure.equals("empty")) return PlayState.STOP;
            if (judge) return state.setAndContinue(RawAnimation.begin().thenLoop(model.idle()));
            boolean moving = state.isMoving() || !(state.getLimbSwingAmount() > -.15F && state.getLimbSwingAmount() < .15F);
            if (moving && (!wuchang || entity.onGround()))
                return state.setAndContinue(RawAnimation.begin().thenLoop(prefix + ".walk"));
            if (!wuchang && entity.isDeadOrDying())
                return state.setAndContinue(RawAnimation.begin().thenPlay(model.lifecycle().deathAnimation().orElseThrow()));
            if (entity.isSprinting() || wuchang && !entity.onGround())
                return state.setAndContinue(RawAnimation.begin().thenLoop(prefix + ".sprint"));
            return state.setAndContinue(RawAnimation.begin().thenLoop(model.idle()));
        }));
        if (!judge) {
            controllers.add(new AnimationController<T>(entity, "attacking", 4, state -> {
                if (entity.getAttackAnim(state.getPartialTick()) > 0 && !swinging) {
                    swinging = true;
                    lastSwing = entity.level().getGameTime();
                }
                if (swinging && lastSwing + 15 <= entity.level().getGameTime()) swinging = false;
                if (swinging && state.getController().getAnimationState() == AnimationController.State.STOPPED) {
                    state.getController().forceAnimationReset();
                    return state.setAndContinue(RawAnimation.begin().thenPlay(prefix + ".attack"));
                }
                return PlayState.CONTINUE;
            }));
        }
        controllers.add(new AnimationController<T>(entity, "procedure", model.lifecycle().animationTransitionTicks(), state -> {
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
}
