package net.yvanchuxiuzhen.system.craft.content;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
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

/** 石磨与石碾物品复用各自几何，来源物品控制器不主动选择动画。 */
public final class StoneMillBlockItem extends ContentBlockItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private GeoRenderProvider renderProvider;

    public StoneMillBlockItem(Block block, Properties properties) {
        this(block, properties, List.of());
    }

    public StoneMillBlockItem(Block block, Properties properties, List<Component> tooltip) {
        super(block, properties, tooltip);
    }

    public String id() {
        return BuiltInRegistries.BLOCK.getKey(getBlock()).getPath();
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
