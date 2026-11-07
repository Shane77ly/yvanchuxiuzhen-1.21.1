package net.yvanchuxiuzhen.system.skill.entity;

import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.world.entity.Entity;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

/**
 * 技能动画实体的两个 GeckoLib 控制器：{@code main} 播放默认动画（默认是循环的默认动画，离地时用离地动画），{@code procedure} 播放过程动画一次
 * （名字为 {@link #NONE} 时停止）。{@link SkillEntity} 与 {@link TamedSkillEntity} 共用。
 */
final class SkillAnimations {
    /** 同步的过程动画名为此值时没有过程动画。 */
    static final String NONE = "empty";

    private SkillAnimations() {
    }

    /** 默认动画取模型表里的循环动画，加上过程动画。 */
    static <T extends Entity & GeoAnimatable> void register(AnimatableManager.ControllerRegistrar controllers, T owner, EntityModelRow model,
            Supplier<String> requested) {
        movement(controllers, owner, state -> RawAnimation.begin().thenLoop(model.loop(owner.onGround())));
        procedure(controllers, owner, requested);
    }

    /** {@code main} 控制器：每刻由 {@code animation} 按实体状态给出要播放的动画。 */
    static <T extends Entity & GeoAnimatable> void movement(AnimatableManager.ControllerRegistrar controllers, T owner,
            Function<AnimationState<T>, RawAnimation> animation) {
        controllers.add(new AnimationController<T>(owner, "main", 0, state -> state.setAndContinue(animation.apply(state))));
    }

    /** {@code procedure} 控制器：播放同步的过程动画一次。 */
    static <T extends Entity & GeoAnimatable> void procedure(AnimatableManager.ControllerRegistrar controllers, T owner, Supplier<String> requested) {
        controllers.add(new AnimationController<T>(owner, "procedure", 0, state -> {
            String name = requested.get();
            return name.equals(NONE) ? PlayState.STOP : state.setAndContinue(RawAnimation.begin().thenPlay(name));
        }));
    }
}
