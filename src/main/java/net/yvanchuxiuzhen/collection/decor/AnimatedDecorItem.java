package net.yvanchuxiuzhen.collection.decor;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.yvanchuxiuzhen.platform.content.ContentBlockItem;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/** 签筒和装饰梯子物品使用来源几何与显示变换，控制器不主动播放动画。 */
public final class AnimatedDecorItem extends ContentBlockItem implements GeoItem {
    private final AnimatedDecorVariant variant;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private GeoRenderProvider renderProvider;

    public AnimatedDecorItem(AnimatedDecorVariant variant, Properties properties, List<Component> tooltip) {
        super(AnimatedDecorContent.block(variant), properties, tooltip);
        this.variant = variant;
    }

    public AnimatedDecorVariant variant() { return variant; }
    public void setRenderProvider(GeoRenderProvider provider) { renderProvider = provider; }
    @Override public Object getRenderProvider() { return renderProvider; }
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, state -> PlayState.CONTINUE));
    }
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }
}
