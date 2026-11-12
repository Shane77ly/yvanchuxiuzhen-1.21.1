package net.yvanchuxiuzhen.system.xiulian;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/**
 * 每秒恢复生命、灵力、神识（来源 {@code Playerhuifu}），恢复量取 attribute 的最终值（来源 {@code *_display}）
 * 加 {@code tizhiitem} 中 {@code *RestorePercentage} × 上限。战斗中（记分项 {@code zdztbegin} 为 1）或雷劫中恢复量为四分之一，
 * 生命按四舍五入取整；效果 {@code effect_007} 期间不恢复生命。灵力不超过上限，主修“繁星呓语”时夜晚露天无雨乘 1.1；
 * 神识恢复不截断上限（来源如此），超过上限且不满足恢复条件时压回上限。
 */
final class Recovery {
    private Recovery() {
    }

    static void tick(ServerPlayer player) {
        if (!player.isAlive()) {
            return;
        }
        Cultivation.set(player, CultivationValue.POWER_DISPLAY, Cultivation.get(player, CultivationValue.POWER));
        Cultivation.set(player, CultivationValue.SOUL_DISPLAY, Cultivation.get(player, CultivationValue.SOUL));
        boolean fighting = LevelUp.score(player, "zdztbegin") == 1 || Cultivation.thunderOpen(player);
        double scale = fighting ? 0.25 : 1;
        if (!CultivationTick.hasEffect(player, "effect_007")) {
            double rec = PlayerAttributes.value(player, Stat.HEALTH_REC);
            double percentage = Cultivation.tizhiDouble(player, "HealthRestorePercentage");
            if (player.getHealth() < player.getMaxHealth() && (rec > 0 || percentage != 0)) {
                double amount = rec + player.getMaxHealth() * percentage;
                player.setHealth((float) (player.getHealth() + (fighting ? Math.round(amount * 0.25) : amount)));
            }
        }
        double power = Cultivation.get(player, CultivationValue.POWER);
        double powerMax = PlayerAttributes.value(player, Stat.POWER_MAX);
        double powerRec = PlayerAttributes.value(player, Stat.POWER_REC);
        double powerPercentage = Cultivation.tizhiDouble(player, "PowerRestorePercentage");
        if (power < powerMax && (powerRec > 0 || powerPercentage != 0)) {
            double amount = powerRec + powerMax * powerPercentage;
            ServerLevel level = player.serverLevel();
            if (Cultivation.mainTechnique(player).name().equals("繁星呓语") && level.canSeeSkyFromBelowWater(player.blockPosition())
                    && !level.isDay() && !level.isRaining()) {
                amount *= 1.1;
            }
            amount *= scale;
            Cultivation.set(player, CultivationValue.POWER, Math.min(power + amount, powerMax));
        } else if (power > powerMax) {
            Cultivation.set(player, CultivationValue.POWER, powerMax);
        }
        double soul = Cultivation.get(player, CultivationValue.SOUL);
        double soulMax = PlayerAttributes.value(player, Stat.SOUL_MAX);
        double soulRec = PlayerAttributes.value(player, Stat.SOUL_REC);
        double soulPercentage = Cultivation.tizhiDouble(player, "JingliRestorePercentage");
        if (soul < soulMax && (soulRec > 0 || soulPercentage != 0)) {
            Cultivation.set(player, CultivationValue.SOUL, soul + (soulRec + soulMax * soulPercentage) * scale);
        } else if (soul > soulMax) {
            Cultivation.set(player, CultivationValue.SOUL, soulMax);
        }
    }
}
