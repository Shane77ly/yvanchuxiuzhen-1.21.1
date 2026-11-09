package net.yvanchuxiuzhen.system.fulu;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
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
     * 未生效时登记品阶、计入生效数、播放释放声并消耗一张。
     */
    public static void activate(ServerPlayer player, ItemStack stack, int number) {
        if (!pay(player, stack)) {
            return;
        }
        String suffix = "%02d".formatted(number);
        String flag = "fuluxiaoguo" + suffix;
        if (Cultivation.tizhiBoolean(player, flag)) {
            return;
        }
        double grade = grade(stack);
        Cultivation.editTizhi(player, tizhi -> {
            tizhi.putBoolean(flag, true);
            tizhi.putDouble("fulu" + suffix + "level", grade);
            tizhi.putDouble("fulunumber", Math.round(tizhi.getDouble("fulunumber") + 1));
        });
        ServerLevel level = player.serverLevel();
        level.playSound(null, BlockPos.containing(player.getX(), player.getY() + 1, player.getZ()),
                BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "fulushifang")), SoundSource.NEUTRAL, 1, 1);
        stack.shrink(1);
    }
}
