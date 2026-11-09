package net.yvanchuxiuzhen.system.fulu.content;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.yvanchuxiuzhen.platform.ModId;

/** 10 号符的 23 种随机负面状态（来源 {@code Itemfulu10DangTouZhiWuJiZhongShiTiShi}）：序号对应的效果与持续刻数，等级统一取品阶。 */
final class RangedEffects {
    private static final String[] EFFECTS = {"effect_001", "effect_002", "effect_003", "effect_004", "effect_005", "effect_006", "effect_007",
            "duyao_01", "duyao_02", "duyao_03", "duyao_04", "duyao_05", "duyao_08", "duyao_09", "duyao_10", "duyao_11", "duyao_12", "duyao_13",
            "duyao_14", "duyao_15", "duyao_16", "", "duyao_18"};
    private static final int[] TICKS = {200, 400, 1200, 1200, 600, 1200, 1200, 600, 1200, 1, 1200, 1200, 1200, 1200, 1, 1200, 1200, 1200, 1200, 1200, 1200, 1200, 1200};

    private RangedEffects() {
    }

    /** 序号 1 至 23；22 为原版缓慢。 */
    static void apply(LivingEntity target, int number, int amplifier) {
        Holder<MobEffect> effect = EFFECTS[number - 1].isEmpty() ? MobEffects.MOVEMENT_SLOWDOWN
                : BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, EFFECTS[number - 1])).orElseThrow();
        target.addEffect(new MobEffectInstance(effect, TICKS[number - 1], amplifier));
    }
}
