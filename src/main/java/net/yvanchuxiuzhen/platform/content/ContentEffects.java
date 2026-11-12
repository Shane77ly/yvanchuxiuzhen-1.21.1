package net.yvanchuxiuzhen.platform.content;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

/**
 * 为内容表效果的原生属性钩子绑定实体上下文。适用性查询覆盖普通与强制施加；到期和移除事件为刚载入且尚未推进的效果补足上下文。
 * 事件只绑定上下文，开始、刷新、隐藏效果恢复及结束的执行顺序由原生属性钩子决定。
 */
final class ContentEffects {
    private static boolean installed;

    private ContentEffects() {
    }

    static void install() {
        if (!installed) {
            installed = true;
            NeoForge.EVENT_BUS.addListener((MobEffectEvent.Applicable event) -> remember(event.getEntity(), event.getEffectInstance()));
            NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, MobEffectEvent.Expired.class, event -> remember(event.getEntity(), event.getEffectInstance()));
            NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, MobEffectEvent.Remove.class, event -> remember(event.getEntity(), event.getEffectInstance()));
        }
    }

    private static void remember(LivingEntity entity, MobEffectInstance instance) {
        if (!entity.level().isClientSide() && instance != null && instance.getEffect().value() instanceof ContentEffect effect) {
            effect.remember(entity, instance.getAmplifier());
        }
    }
}
