package net.yvanchuxiuzhen.system.fulu;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/**
 * 符箓施用的共享前半段：精力付款（来源 {@code Fulujinglineed}）与持续效果的激活登记
 * （来源 {@code Itemfulu02DangYouJianDianJiKongQiShiShiTiDeWeiZhi}，02、03、13、17、19、24、31 号符共用）。
 *
 * <p>效果状态保存在玩家体质标签：{@code fuluxiaoguo<编号>} 为是否生效，{@code fulu<编号>level} 为生效时的品阶，
 * {@code fulunumber} 为同时生效的符箓数；逐刻推进与结束见 {@link FuluTick}。
 */
public final class FuluUse {
    private static final double BASE_COST_PER_SQUARE = 10;

    private FuluUse() {
    }

    /** 符箓状态键的当前值：玩家在体质标签，其他实体在实体持久数据（弹体读取发射者的品阶用）。 */
    public static double stateValue(Entity owner, String key) {
        return owner instanceof Player player ? Cultivation.tizhiDouble(player, key) : owner.getPersistentData().getDouble(key);
    }

    /** 成品组件 {@code fululevel}；低于 1 按 1 计，品阶与付款共用。 */
    static double grade(ItemStack stack) {
        double level = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getDouble("fululevel");
        return level < 1 ? 1 : level;
    }

    /**
     * 精力付款：非创造玩家扣除 {@code 品阶² × 10}，体质 {@code FuluConsumeJingli} 非 0 时加上固定值；
     * {@code FuluConsumeJingliPercentage} 非 0 时来源乘的是 {@code (FuluConsumeJingli + 1)} 而不是自己的值，
     * 疑似字段误用，保持来源（登记为 Q64）。精力不足返回 false 且不扣；创造模式与非玩家直接通过。
     */
    static boolean pay(Entity entity, ItemStack stack) {
        if (!(entity instanceof Player player) || player.isCreative()) {
            return true;
        }
        double fixed = Cultivation.tizhiDouble(player, "FuluConsumeJingli");
        double cost = Math.pow(grade(stack), 2) * BASE_COST_PER_SQUARE;
        if (fixed != 0) {
            cost += fixed;
        }
        if (Cultivation.tizhiDouble(player, "FuluConsumeJingliPercentage") != 0) {
            cost *= fixed + 1;
        }
        double soul = Cultivation.get(player, CultivationValue.SOUL);
        if (soul < cost) {
            return false;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            Cultivation.set(serverPlayer, CultivationValue.SOUL, soul - cost);
        }
        return true;
    }

    /**
     * 使用一张持续效果符：先付款，再判断该编号是否已生效。已生效时精力已经扣除而符不消耗（来源顺序）；
     * 未生效时登记品阶、计入生效数、播放释放声并消耗一张。21 号符另清除低于 {@code 2×品阶−1} 的负面状态，
     * 其生效数来源读的是物品而不是玩家的 {@code fulunumber}（登记为 Q65）；39 号符释放声先于判断，已生效时仍续水下呼吸并消耗符。
     */
    public static void activate(ServerPlayer player, ItemStack stack, int number) {
        if (FuluInstant.handles(number)) {
            FuluInstant.use(player, stack, number);
            return;
        }
        if (!pay(player, stack)) {
            return;
        }
        String suffix = "%02d".formatted(number);
        String flag = "fuluxiaoguo" + suffix;
        double grade = grade(stack);
        boolean breath = number == 39;
        boolean active = Cultivation.tizhiBoolean(player, flag);
        if (active && !breath) {
            return;
        }
        if (breath) {
            cast(player);
        }
        if (!active) {
            double itemCount = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getDouble("fulunumber");
            Cultivation.editTizhi(player, tizhi -> {
                tizhi.putBoolean(flag, true);
                tizhi.putDouble("fulu" + suffix + "level", grade);
                tizhi.putDouble("fulunumber", Math.round((number == 21 ? itemCount : tizhi.getDouble("fulunumber")) + 1));
            });
        }
        if (breath) {
            player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, (int) (12000 * Math.pow(grade, 2)), 0));
        } else {
            cast(player);
            if (number == 21) {
                cleanse(player, 2 * grade - 1);
            }
        }
        stack.shrink(1);
    }

    /** 在玩家位置上方一格播放释放声。 */
    static void cast(ServerPlayer player) {
        player.serverLevel().playSound(null, BlockPos.containing(player.getX(), player.getY() + 1, player.getZ()),
                BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "fulushifang")), SoundSource.NEUTRAL, 1, 1);
    }

    /** 21 号符的清除清单（来源顺序）：毒药 01–17、效果 001–007，以及原版的缓慢、挖掘疲劳、反胃、失明、饥饿、虚弱、中毒、凋零、霉运。 */
    private static void cleanse(ServerPlayer player, double level) {
        for (int index = 1; index <= 17; index++) {
            remove(player, level, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "duyao_%02d".formatted(index)));
        }
        for (int index = 1; index <= 7; index++) {
            remove(player, level, ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "effect_%03d".formatted(index)));
        }
        for (Holder<MobEffect> effect : List.of(MobEffects.MOVEMENT_SLOWDOWN, MobEffects.DIG_SLOWDOWN, MobEffects.CONFUSION, MobEffects.BLINDNESS,
                MobEffects.HUNGER, MobEffects.WEAKNESS, MobEffects.POISON, MobEffects.WITHER, MobEffects.UNLUCK)) {
            remove(player, level, effect);
        }
    }

    private static void remove(ServerPlayer player, double level, ResourceLocation id) {
        remove(player, level, BuiltInRegistries.MOB_EFFECT.getHolder(id).orElseThrow());
    }

    /** 状态等级（无则按 0）不超过符箓清除等级时移除。 */
    private static void remove(ServerPlayer player, double level, Holder<MobEffect> effect) {
        MobEffectInstance active = player.getEffect(effect);
        if (level >= (active == null ? 0 : active.getAmplifier())) {
            player.removeEffect(effect);
        }
    }
}
