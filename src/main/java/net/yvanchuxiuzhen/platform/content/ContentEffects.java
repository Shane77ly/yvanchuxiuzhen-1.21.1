package net.yvanchuxiuzhen.platform.content;

import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

/**
 * 内容表状态效果的开始钩子。来源在效果加上时调用 {@code addAttributeModifiers}，已有效果被刷新且数值有变化时（等级更高、
 * 时长更长等，与 {@link MobEffectInstance#update} 的返回值一致）先移除再重新应用，所以同样调用；
 * 1.21.1 的 {@code MobEffect} 没有携带实体的对应钩子，改在 {@link MobEffectEvent.Added} 中判断。
 */
final class ContentEffects {
    private static boolean installed;

    private ContentEffects() {
    }

    static void install() {
        if (!installed) {
            installed = true;
            NeoForge.EVENT_BUS.addListener(ContentEffects::onAdded);
        }
    }

    private static void onAdded(MobEffectEvent.Added event) {
        MobEffectInstance added = event.getEffectInstance();
        if (event.getEntity().level().isClientSide() || !(added.getEffect().value() instanceof ContentEffect effect)) {
            return;
        }
        MobEffectInstance old = event.getOldEffectInstance();
        if (old == null || new MobEffectInstance(old).update(added)) {
            effect.behavior().started(event.getEntity(), added.getAmplifier());
        }
    }
}
