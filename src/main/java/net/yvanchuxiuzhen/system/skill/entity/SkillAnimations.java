package net.yvanchuxiuzhen.system.skill.entity;

import java.util.function.Supplier;
import net.minecraft.world.entity.Entity;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;

/**
 * 技能动画实体的两个 GeckoLib 控制器：{@code main} 循环播放默认动画（离地时用离地动画），{@code procedure} 播放过程动画一次
 * （名字为 {@link #NONE} 时停止）。{@link SkillEntity} 与 {@link TamedSkillEntity} 共用。
 */
final class SkillAnimations {
    /** 同步的过程动画名为此值时没有过程动画。 */
    static final String NONE = "empty";

    private SkillAnimations() {
    }

    static <T extends Entity & GeoAnimatable> void register(AnimatableManager.ControllerRegistrar controllers, T owner, EntityModelRow model,
            Supplier<String> requested) {
        controllers.add(new AnimationController<T>(owner, "main", 0, state -> state.setAndContinue(RawAnimation.begin().thenLoop(model.loop(owner.onGround())))));
        controllers.add(new AnimationController<T>(owner, "procedure", 0, state -> {
            String name = requested.get();
            return name.equals(NONE) ? PlayState.STOP : state.setAndContinue(RawAnimation.begin().thenPlay(name));
        }));
    }
}
