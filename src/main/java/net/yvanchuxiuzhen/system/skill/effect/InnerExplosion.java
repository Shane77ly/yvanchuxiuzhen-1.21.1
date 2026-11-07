package net.yvanchuxiuzhen.system.skill.effect;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.yvanchuxiuzhen.platform.content.EffectRow;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;

/**
 * 内爆在原生属性移除钩子结算，并现读实体上的效果等级。刷新时读到更新后的等级；到期或单独移除时效果已不在集合中，按零级结算；
 * 清除全部效果时，原生集合在钩子之后移除，因此仍读取当前等级。属性表到实体的弱引用仅补足 1.21.1 钩子缺失的实体参数。
 */
final class InnerExplosion extends MobEffect {
    private final Map<AttributeMap, WeakReference<LivingEntity>> owners = new WeakHashMap<>();

    InnerExplosion(EffectRow row) {
        super(MobEffectCategory.valueOf(row.category()), row.color());
    }

    static void install() {
        NeoForge.EVENT_BUS.addListener((MobEffectEvent.Added event) -> remember(event.getEntity(), event.getEffectInstance()));
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, MobEffectEvent.Expired.class,
                event -> remember(event.getEntity(), event.getEffectInstance()));
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, MobEffectEvent.Remove.class,
                event -> remember(event.getEntity(), event.getEffectInstance()));
    }

    private static void remember(LivingEntity entity, MobEffectInstance instance) {
        if (!entity.level().isClientSide() && instance != null && instance.getEffect().value() instanceof InnerExplosion effect) {
            effect.owners.put(entity.getAttributes(), new WeakReference<>(entity));
        }
    }

    @Override
    public void onEffectStarted(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide()) owners.put(entity.getAttributes(), new WeakReference<>(entity));
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        onEffectStarted(entity, amplifier);
        return true;
    }

    @Override
    public void removeAttributeModifiers(AttributeMap attributes) {
        super.removeAttributeModifiers(attributes);
        WeakReference<LivingEntity> reference = owners.remove(attributes);
        LivingEntity entity = reference == null ? null : reference.get();
        if (entity != null) {
            entity.hurt(entity.damageSources().fellOutOfWorld(), (StatusEffects.amplifier(entity, "skill_arrow_0601") + 1) * 2);
        }
    }
}
