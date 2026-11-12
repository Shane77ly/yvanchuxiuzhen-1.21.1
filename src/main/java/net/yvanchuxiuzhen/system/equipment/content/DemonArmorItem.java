package net.yvanchuxiuzhen.system.equipment.content;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
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

/** 妖化的四件原生护甲，使用来源骨骼与循环动画；计时由装备系统的玩家刻入口推进。 */
public final class DemonArmorItem extends ArmorItem implements GeoItem {
    private static final RawAnimation IDLE_ANIMATION = RawAnimation.begin().thenLoop("animation.yaohua_0101.idle");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private GeoRenderProvider renderProvider;
    private String animationProcedure = "empty";

    public DemonArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }

    /** 客户端消费护甲标签后更新该物品单例的过程动画槽。 */
    public void requestAnimation(String animation) {
        animationProcedure = animation;
    }

    /** 仅由客户端装配提供，服务端不构造渲染器。 */
    public void setRenderProvider(GeoRenderProvider provider) {
        renderProvider = provider;
    }

    @Override
    public Object getRenderProvider() {
        return renderProvider;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 5, this::idlePredicate));
        controllers.add(new AnimationController<>(this, "procedureController", 5, this::procedurePredicate));
    }

    private PlayState idlePredicate(AnimationState<DemonArmorItem> state) {
        if (animationProcedure.equals("empty")) {
            return state.setAndContinue(IDLE_ANIMATION);
        }
        return PlayState.STOP;
    }

    private PlayState procedurePredicate(AnimationState<DemonArmorItem> state) {
        AnimationController<DemonArmorItem> controller = state.getController();
        if (!animationProcedure.equals("empty") && controller.getAnimationState() == AnimationController.State.STOPPED) {
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
}
