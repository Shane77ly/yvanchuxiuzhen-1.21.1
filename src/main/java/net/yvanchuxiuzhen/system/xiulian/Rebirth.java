package net.yvanchuxiuzhen.system.xiulian;

import java.text.DecimalFormat;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/**
 * 普通玩家重生后的修炼资源恢复与寿元修正，对应来源 {@code Entitybereborn} 的修炼片段。
 * 跨系统顺序由普通重生流程编排，本过程只写入本系统资源与玩家生命。
 */
public final class Rebirth {
    private static final DecimalFormat WHOLE = new DecimalFormat("0");

    private Rebirth() {
    }

    public static void rebirth(ServerPlayer player) {
        double health = correctedMaximum(player, player.getMaxHealth(), "RebirthCorrectHealth", "RebirthCorrectHealthPercentage");
        player.setHealth((float) health);

        double power = correctedMaximum(player, PlayerAttributes.value(player, Stat.POWER_MAX), "RebirthCorrectPower", "RebirthCorrectPowerPercentage");
        Cultivation.set(player, CultivationValue.POWER, power);

        double soul = correctedMaximum(player, PlayerAttributes.value(player, Stat.SOUL_MAX), "RebirthCorrectJingli", "RebirthCorrectJingliPercentage");
        Cultivation.set(player, CultivationValue.SOUL, soul);

        double experienceCorrection = Cultivation.tizhiDouble(player, "RebirthCorrectExperience");
        double experienceStand = Cultivation.get(player, CultivationValue.EXPERIENCE_STAND);
        if (experienceCorrection != 0) {
            experienceStand += experienceCorrection;
            Cultivation.set(player, CultivationValue.EXPERIENCE_STAND, experienceStand);
        }
        double experiencePercentage = Cultivation.tizhiDouble(player, "RebirthCorrectExperiencePercentage");
        if (experiencePercentage != 0) {
            Cultivation.set(player, CultivationValue.EXPERIENCE_STAND, Cultivation.get(player, CultivationValue.EXPERIENCE_STAND)
                    * (experiencePercentage + 1));
        }

        double lifetimeBefore = Cultivation.get(player, CultivationValue.LIFETIME);
        double lifetimeChange = -30;
        if (Cultivation.tizhiDouble(player, "RebirthCorrectLifetime") != 0) {
            // 来源过程误读 RebirthCorrectJingli；保留该行为以维持数值对齐。
            lifetimeChange += Cultivation.tizhiDouble(player, "RebirthCorrectJingli");
        }
        double lifetimePercentage = Cultivation.tizhiDouble(player, "RebirthCorrectLifetimePercentage");
        if (lifetimePercentage != 0) {
            double correctedLifetime = lifetimeBefore * (lifetimePercentage + 1);
            lifetimeChange += correctedLifetime - lifetimeBefore;
        }
        boolean increased = lifetimeChange > 0;
        player.displayClientMessage(Component.translatable(increased ? "message.yvanchuxiuzhen.rebirth.lifetime_gain"
                : "message.yvanchuxiuzhen.rebirth.lifetime_loss", WHOLE.format(Math.abs(lifetimeChange)))
                .withStyle(increased ? ChatFormatting.GOLD : ChatFormatting.DARK_RED), false);
        Cultivation.set(player, CultivationValue.LIFETIME, lifetimeBefore + lifetimeChange);
    }

    private static double correctedMaximum(ServerPlayer player, double maximum, String additiveKey, String percentageKey) {
        double value = maximum * 0.2 + Cultivation.tizhiDouble(player, additiveKey);
        double percentage = Cultivation.tizhiDouble(player, percentageKey);
        if (percentage != 0) {
            value *= percentage + 1;
        }
        return value;
    }
}
