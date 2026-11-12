package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import java.util.function.Function;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

/** 来源三个动画控制器；移动分支由每个编号保留各自顺序和动画名。 */
final class Yaoshou037054Animations {
    private boolean swinging;
    private long lastSwing;

    void register(YaoshouEntity entity, AnimatableManager.ControllerRegistrar controllers, int transitionTicks,
            String prefix, Function<AnimationState<YaoshouEntity>, PlayState> movement) {
        controllers.add(new AnimationController<YaoshouEntity>(entity, "movement", transitionTicks, movement::apply));
        controllers.add(new AnimationController<YaoshouEntity>(entity, "attacking", transitionTicks, state -> {
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
        controllers.add(new AnimationController<YaoshouEntity>(entity, "procedure", transitionTicks, state -> {
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
