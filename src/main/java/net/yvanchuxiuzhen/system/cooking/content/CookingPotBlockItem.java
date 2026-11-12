package net.yvanchuxiuzhen.system.cooking.content;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.yvanchuxiuzhen.platform.content.ContentBlockItem;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/** 两锅物品使用来源几何与显示变换，物品控制器不主动播放动画。 */
public final class CookingPotBlockItem extends ContentBlockItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private GeoRenderProvider renderProvider;

    public CookingPotBlockItem(Block block, Properties properties, List<Component> tooltip) {
        super(block, properties, tooltip);
    }

    public String id() {
        return ((CookingPotBlock) getBlock()).iron() ? "fooddaguo" : "fooddunguo";
    }

    public void setRenderProvider(GeoRenderProvider provider) {
        renderProvider = provider;
    }

    @Override
    public Object getRenderProvider() {
        return renderProvider;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, state -> PlayState.CONTINUE));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
