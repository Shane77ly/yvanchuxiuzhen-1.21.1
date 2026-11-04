package net.yvanchuxiuzhen.platform.content;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

/**
 * 内容表状态效果的开始与结束钩子。来源在效果加上时调用 {@code addAttributeModifiers}，已有效果被刷新且数值有变化时（等级更高、
 * 时长更长等，与 {@link MobEffectInstance#update} 的返回值一致）先 {@code removeAttributeModifiers} 再重新应用，到期与被移除时调用
 * {@code removeAttributeModifiers}；1.21.1 的 {@code MobEffect} 没有携带实体的对应钩子，开始与刷新在 {@link MobEffectEvent.Added}
 * 中判断，到期与被移除在 {@link MobEffectEvent.Expired}、{@link MobEffectEvent.Remove} 中判断（取其他监听者都不取消之后）。
 */
final class ContentEffects {
    private static boolean installed;

    private ContentEffects() {
    }

    static void install() {
        if (!installed) {
            installed = true;
            NeoForge.EVENT_BUS.addListener(ContentEffects::onAdded);
            NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, MobEffectEvent.Expired.class, event -> onEnded(event.getEntity(), event.getEffectInstance()));
            NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, MobEffectEvent.Remove.class, event -> onEnded(event.getEntity(), event.getEffectInstance()));
        }
    }

    private static void onAdded(MobEffectEvent.Added event) {
        MobEffectInstance added = event.getEffectInstance();
        if (event.getEntity().level().isClientSide() || !(added.getEffect().value() instanceof ContentEffect effect)) {
            return;
        }
        MobEffectInstance old = event.getOldEffectInstance();
        if (old == null || new MobEffectInstance(old).update(added)) {
            if (old != null) {
                effect.behavior().ended(event.getEntity(), old.getAmplifier());
            }
            effect.behavior().started(event.getEntity(), added.getAmplifier());
        }
    }

    private static void onEnded(LivingEntity entity, MobEffectInstance ended) {
        if (!entity.level().isClientSide() && ended != null && ended.getEffect().value() instanceof ContentEffect effect) {
            effect.behavior().ended(entity, ended.getAmplifier());
        }
    }
}
