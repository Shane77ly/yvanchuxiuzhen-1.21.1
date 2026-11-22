package net.yvanchuxiuzhen.system.creature.content;

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

/** 棺材方块物品沿用来源实体模型及各持握显示变换。 */
public final class CoffinBlockItem extends ContentBlockItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private GeoRenderProvider renderer;
    public CoffinBlockItem(Block block, Properties properties, List<Component> tooltip) { super(block, properties, tooltip); }
    public void setRenderProvider(GeoRenderProvider renderer) { this.renderer = renderer; }
    @Override public Object getRenderProvider() { return renderer; }
    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }
    @Override public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, state -> PlayState.CONTINUE));
    }
}
