package net.yvanchuxiuzhen.platform.content;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;

/** 内容表登记的状态效果，行为由 {@link EffectBehavior} 承担。 */
final class ContentEffect extends MobEffect {
    private final EffectBehavior behavior;
    private final Map<AttributeMap, Owner> owners = new WeakHashMap<>();

    ContentEffect(MobEffectCategory category, int color, EffectBehavior behavior) {
        super(category, color);
        this.behavior = behavior;
    }

    /** 在原生属性钩子之前绑定实体；弱键和弱实体引用避免由全局效果实例保留已卸载实体。 */
    void remember(LivingEntity entity, int amplifier) {
        owners.put(entity.getAttributes(), new Owner(new WeakReference<>(entity), amplifier));
    }

    @Override
    public void addAttributeModifiers(AttributeMap attributes, int amplifier) {
        super.addAttributeModifiers(attributes, amplifier);
        behavior.started(owners.get(attributes).entity().get(), amplifier);
    }

    @Override
    public void removeAttributeModifiers(AttributeMap attributes) {
        super.removeAttributeModifiers(attributes);
        Owner owner = owners.get(attributes);
        LivingEntity entity = owner.entity().get();
        MobEffectInstance current = entity.getEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(this));
        behavior.ended(entity, current == null ? owner.amplifier() : current.getAmplifier());
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide()) {
            behavior.clientTick(entity, amplifier);
            return true;
        }
        // 存档载入不重放开始钩子；首次刻绑定后，隐藏效果恢复仍由原生属性钩子处理。
        remember(entity, amplifier);
        return behavior.tick(entity, amplifier);
    }

    private record Owner(WeakReference<LivingEntity> entity, int amplifier) {
    }
}
