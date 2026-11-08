package net.yvanchuxiuzhen.system.equipment.content;

import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorMaterial;
import net.yvanchuxiuzhen.platform.content.ContentArmorItem;
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

/** 装备系统的动画护甲；材质和部位数值来自内容表，实例标签请求驱动物品单例的过程动画。 */
public final class EquipmentGeoArmorItem extends ContentArmorItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final EquipmentArmorVisual visual;
    private GeoRenderProvider renderProvider;
    private String animationProcedure = "empty";

    public EquipmentGeoArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties,
                                 String materialId, List<Component> tooltip) {
        super(material, type, properties, tooltip);
        visual = EquipmentArmorVisual.byId(materialId);
    }

    public EquipmentArmorVisual visual() {
        return visual;
    }

    /** 客户端按来源将已消费的物品标签写入该物品单例的过程动画槽。 */
    public void requestAnimation(String animation) {
        animationProcedure = animation;
    }

    /** 客户端装配注入渲染提供者，服务端仅保留公共动画数据。 */
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

    private PlayState idlePredicate(AnimationState<EquipmentGeoArmorItem> state) {
        if (animationProcedure.equals("empty")) {
            return state.setAndContinue(RawAnimation.begin().thenLoop(visual.idle()));
        }
        return PlayState.STOP;
    }

    private PlayState procedurePredicate(AnimationState<EquipmentGeoArmorItem> state) {
        AnimationController<EquipmentGeoArmorItem> controller = state.getController();
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
