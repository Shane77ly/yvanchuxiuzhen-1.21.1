package net.yvanchuxiuzhen.system.equipment.content;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.yvanchuxiuzhen.platform.content.ContentItem;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/** 文王鼓与赶仙鞭共用动画物品生命周期，按实例标签请求切换物品单例的过程动画。 */
public abstract class AnimatedFabaoItem extends ContentItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final String modelName;
    private final RawAnimation idleAnimation;
    private RenderProvider renderProvider;
    private String animationProcedure = "empty";
    private boolean rendered;

    protected AnimatedFabaoItem(Properties properties, List<Component> tooltip, String modelName) {
        super(properties, tooltip);
        this.modelName = modelName;
        idleAnimation = RawAnimation.begin().thenLoop("animation." + modelName + ".idle");
    }

    public String modelName() {
        return modelName;
    }

    /** 由客户端渲染入口在完成一次渲染后登记，与来源首次获得显示上下文的时机一致。 */
    public void markRendered() {
        rendered = true;
    }

    /** 客户端消费标签后更新过程槽，并恢复来源的主副手当前帧与前一帧持物高度。 */
    public void requestAnimation(String animation) {
        animationProcedure = animation;
        renderProvider.resetHandHeight();
    }

    /** 仅由客户端装配提供，服务端不构造渲染器。 */
    public void setRenderProvider(RenderProvider provider) {
        renderProvider = provider;
    }

    @Override
    public Object getRenderProvider() {
        return renderProvider;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "procedureController", 0, this::procedurePredicate));
        controllers.add(new AnimationController<>(this, "idleController", 0, this::idlePredicate));
    }

    private PlayState idlePredicate(AnimationState<AnimatedFabaoItem> state) {
        if (rendered && animationProcedure.equals("empty")) {
            return state.setAndContinue(idleAnimation);
        }
        return PlayState.STOP;
    }

    private PlayState procedurePredicate(AnimationState<AnimatedFabaoItem> state) {
        AnimationController<AnimatedFabaoItem> controller = state.getController();
        if (rendered && !animationProcedure.equals("empty")
                && controller.getAnimationState() == AnimationController.State.STOPPED) {
            controller.setAnimation(RawAnimation.begin().then(animationProcedure, Animation.LoopType.PLAY_ONCE));
            if (controller.getAnimationState() == AnimationController.State.STOPPED) {
                animationProcedure = "empty";
                controller.forceAnimationReset();
            }
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    /** 客户端提供实际渲染器与来源请求发生时的持物高度更新。 */
    public interface RenderProvider extends GeoRenderProvider {
        void resetHandHeight();
    }
}
