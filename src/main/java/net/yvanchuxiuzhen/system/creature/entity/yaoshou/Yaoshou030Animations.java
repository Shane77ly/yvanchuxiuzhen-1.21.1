package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

/** 030号妖兽的移动、挥击与过程动画控制器。 */
final class Yaoshou030Animations {
    private boolean swinging;
    private long lastSwing;

    void register(YaoshouEntity entity, AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<YaoshouEntity>(entity, "movement", 2, state -> {
            if (!entity.procedureAnimation().equals("empty")) return PlayState.STOP;
            boolean moving = state.isMoving() || !(state.getLimbSwingAmount() > -0.15F && state.getLimbSwingAmount() < 0.15F);
            if (moving && entity.onGround() && !entity.isAggressive()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou030.walk"));
            if (entity.isDeadOrDying()) return state.setAndContinue(RawAnimation.begin().then("animation.yaoshou030.die", Animation.LoopType.PLAY_ONCE));
            if (entity.isShiftKeyDown()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou030.sneak"));
            if (!entity.onGround()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou030.fly"));
            if (entity.isAggressive() && state.isMoving()) return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou030.attacking"));
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.yaoshou030.idle"));
        }));
        controllers.add(new AnimationController<YaoshouEntity>(entity, "attacking", 2, state -> {
            if (entity.getAttackAnim(state.getPartialTick()) > 0 && !swinging) {
                swinging = true;
                lastSwing = entity.level().getGameTime();
            }
            if (swinging && lastSwing + 15 <= entity.level().getGameTime()) swinging = false;
            AnimationController<YaoshouEntity> controller = state.getController();
            if (swinging && controller.getAnimationState() == AnimationController.State.STOPPED) {
                controller.forceAnimationReset();
                controller.setAnimation(RawAnimation.begin().then("animation.yaoshou030.attack", Animation.LoopType.PLAY_ONCE));
            }
            return PlayState.CONTINUE;
        }));
        controllers.add(new AnimationController<YaoshouEntity>(entity, "procedure", 2, state -> {
            AnimationController<YaoshouEntity> controller = state.getController();
            if (!entity.procedureAnimation().equals("empty") && controller.getAnimationState() == AnimationController.State.STOPPED) {
                controller.setAnimation(RawAnimation.begin().then(entity.procedureAnimation(), Animation.LoopType.PLAY_ONCE));
                if (controller.getAnimationState() == AnimationController.State.STOPPED) {
                    entity.clearProcedureAnimation();
                    controller.forceAnimationReset();
                }
            }
            return PlayState.CONTINUE;
        }));
    }
}
