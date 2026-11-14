package net.yvanchuxiuzhen.system.guchong.content;

import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.yvanchuxiuzhen.platform.content.ContentItem;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

/** 蛊笛的动画物品与普通右键开界面、潜行右键收回入口。 */
public final class GuchongFluteItem extends ContentItem implements GeoItem {
    private static final RawAnimation IDLE = RawAnimation.begin().thenLoop("animation.fabao018.idle");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private GeoRenderProvider renderProvider;
    private boolean rendered;

    public GuchongFluteItem(Properties properties, List<Component> tooltip) {
        super(properties, tooltip);
    }

    /** 客户端完成首次渲染后，来源空闲控制器才获得显示上下文。 */
    public void markRendered() {
        rendered = true;
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
        controllers.add(new AnimationController<>(this, "idleController", 0,
                state -> rendered ? state.setAndContinue(IDLE) : PlayState.STOP));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        InteractionResultHolder<ItemStack> result = super.use(level, player, hand);
        if (!(level instanceof ServerLevel server)) return result;
        if (StatusEffects.has(player, "effect_002")) {
            player.displayClientMessage(Component.translatable("message.yvanchuxiuzhen.guchong_flute_silenced"), false);
        } else if (player.isShiftKeyDown()) {
            GuchongFluteRuntime.recall(server, player);
        } else if (player instanceof ServerPlayer owner) {
            GuchongMenus.openRelease(owner);
        }
        return result;
    }
}
