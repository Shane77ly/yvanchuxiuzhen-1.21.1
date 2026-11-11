package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

/** 019、020来源相同的三个动画控制器；冲刺动画名保留各自来源差异。 */
final class Yaoshou019020Animations {
    private boolean swinging;
    private long lastSwing;

    void register(YaoshouEntity entity, AnimatableManager.ControllerRegistrar controllers, String prefix, String sprint) {
        controllers.add(new AnimationController<YaoshouEntity>(entity, "movement", 4, state -> {
            if (!entity.procedureAnimation().equals("empty")) return PlayState.STOP;
            boolean moving = state.isMoving() || !(state.getLimbSwingAmount() > -0.15F && state.getLimbSwingAmount() < 0.15F);
            if (moving && !entity.isVehicle() && !entity.isAggressive()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop(prefix + "walk"));
            }
            if (entity.isDeadOrDying()) {
                return state.setAndContinue(RawAnimation.begin().then(prefix + "die", Animation.LoopType.PLAY_ONCE));
            }
            if (entity.isInWaterOrBubble()) return state.setAndContinue(RawAnimation.begin().thenLoop(prefix + "swimming"));
            if (entity.isShiftKeyDown()) return state.setAndContinue(RawAnimation.begin().thenLoop(prefix + "sneak"));
            if (entity.isSprinting()) return state.setAndContinue(RawAnimation.begin().thenLoop(prefix + sprint));
            if (entity.isVehicle() && state.isMoving()) return state.setAndContinue(RawAnimation.begin().thenLoop(prefix + "ride"));
            if (entity.isAggressive() && state.isMoving() && !entity.isVehicle()) {
                return state.setAndContinue(RawAnimation.begin().thenLoop(prefix + "attacking"));
            }
            return state.setAndContinue(RawAnimation.begin().thenLoop(prefix + "idle"));
        }));
        controllers.add(new AnimationController<YaoshouEntity>(entity, "attacking", 4, state -> {
            if (entity.getAttackAnim(state.getPartialTick()) > 0 && !swinging) {
                swinging = true;
                lastSwing = entity.level().getGameTime();
            }
            if (swinging && lastSwing + 15 <= entity.level().getGameTime()) swinging = false;
            AnimationController<YaoshouEntity> controller = state.getController();
            if (swinging && controller.getAnimationState() == AnimationController.State.STOPPED) {
                controller.forceAnimationReset();
                controller.setAnimation(RawAnimation.begin().then(prefix + "attack", Animation.LoopType.PLAY_ONCE));
            }
            return PlayState.CONTINUE;
        }));
        controllers.add(new AnimationController<YaoshouEntity>(entity, "procedure", 4, state -> {
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
