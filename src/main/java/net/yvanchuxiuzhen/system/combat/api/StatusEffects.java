package net.yvanchuxiuzhen.system.combat.api;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.yvanchuxiuzhen.platform.ModId;

/** 本 Mod 状态效果的查询与移除，效果按路径名（如 {@code effect_036}）指定。 */
public final class StatusEffects {
    private StatusEffects() {
    }

    public static Holder<MobEffect> holder(String effect) {
        return BuiltInRegistries.MOB_EFFECT.getHolderOrThrow(
                ResourceKey.create(Registries.MOB_EFFECT, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, effect)));
    }

    /** 实体当前身上该效果的等级（来源的 {@code getAmplifier}），没有为 0。 */
    public static int amplifier(LivingEntity entity, String effect) {
        MobEffectInstance instance = entity.getEffect(holder(effect));
        return instance == null ? 0 : instance.getAmplifier();
    }

    public static boolean has(LivingEntity entity, String effect) {
        return entity.hasEffect(holder(effect));
    }

    public static void remove(LivingEntity entity, String effect) {
        entity.removeEffect(holder(effect));
    }

    /** 附加该效果，覆盖或叠加按原版 {@code addEffect} 的规则。 */
    public static void add(LivingEntity entity, String effect, int duration, int amplifier) {
        entity.addEffect(new MobEffectInstance(holder(effect), duration, amplifier));
    }
}
