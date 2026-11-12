package net.yvanchuxiuzhen.system.skill.entity;

import java.util.function.Function;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

/**
 * 技能实体的本地过程动画与 GeckoLib 控制器。同步字段只承载待消费的请求，当前过程名在每个实例内保存；
 * 默认动画只在没有过程动画时播放，过程明确播放一次，完成后恢复默认动画。
 */
final class SkillAnimations {
    /** 本地没有过程动画。 */
    static final String NONE = "empty";
    /** 同步字段没有待消费的请求。 */
    static final String NO_REQUEST = "undefined";
    private final int transitionTicks;
    private String procedureName = NONE;

    SkillAnimations(int transitionTicks) {
        this.transitionTicks = transitionTicks;
    }

    /** 来源 EntityAnimationFactory 的逐刻消费：本地记下请求，同步字段立即复位，允许后续同名请求。 */
    void tick(SynchedEntityData data, EntityDataAccessor<String> animation) {
        String requested = data.get(animation);
        if (!requested.equals(NO_REQUEST)) {
            data.set(animation, NO_REQUEST);
            procedureName = requested;
        }
    }

    /** 默认动画取模型表里的循环动画，加上过程动画。 */
    <T extends LivingEntity & GeoAnimatable> void register(AnimatableManager.ControllerRegistrar controllers, T owner, EntityModelRow model) {
        movement(controllers, owner, state -> model.lifecycle().deathAnimation().isPresent() && owner.isDeadOrDying()
                ? RawAnimation.begin().then(model.lifecycle().deathAnimation().get(), Animation.LoopType.PLAY_ONCE)
                : RawAnimation.begin().thenLoop(model.loop(owner.onGround())));
        procedure(controllers, owner);
    }

    /** 默认控制器：没有过程动画时按实体状态播放。 */
    <T extends Entity & GeoAnimatable> void movement(AnimatableManager.ControllerRegistrar controllers, T owner,
            Function<AnimationState<T>, RawAnimation> animation) {
        controllers.add(new AnimationController<T>(owner, "movement", transitionTicks,
                state -> procedureName.equals(NONE) ? state.setAndContinue(animation.apply(state)) : PlayState.STOP));
    }

    /** {@code procedure} 控制器：播放同步的过程动画一次。 */
    <T extends Entity & GeoAnimatable> void procedure(AnimatableManager.ControllerRegistrar controllers, T owner) {
        controllers.add(new AnimationController<T>(owner, "procedure", transitionTicks, state -> {
            AnimationController<T> controller = state.getController();
            if (!procedureName.equals(NONE) && controller.getAnimationState() == AnimationController.State.STOPPED) {
                controller.setAnimation(RawAnimation.begin().then(procedureName, Animation.LoopType.PLAY_ONCE));
                if (controller.getAnimationState() == AnimationController.State.STOPPED) {
                    procedureName = NONE;
                    controller.forceAnimationReset();
                }
            }
            return PlayState.CONTINUE;
        }));
    }
}
