package net.yvanchuxiuzhen.system.xiulian;

import java.text.DecimalFormat;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/**
 * 普通玩家重生后的修炼资源恢复与寿元修正，对应来源 {@code Entitybereborn} 的普通重生入口。
 * 游戏模式、维度和延迟属性重算属于其他系统或未迁移入口，不在此过程处理。
 */
public final class Rebirth {
    private static final DecimalFormat WHOLE = new DecimalFormat("0");

    private Rebirth() {
    }

    /** 订阅普通重生事件；来源没有按 {@code isEndConquered} 过滤。 */
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        rebirth((ServerPlayer) event.getEntity());
    }

    private static void rebirth(ServerPlayer player) {
        double health = correctedMaximum(player, Stat.HEALTH_MAX, "RebirthCorrectHealth", "RebirthCorrectHealthPercentage");
        player.setHealth((float) health);

        double power = correctedMaximum(player, Stat.POWER_MAX, "RebirthCorrectPower", "RebirthCorrectPowerPercentage");
        Cultivation.set(player, CultivationValue.POWER, power);

        double soul = correctedMaximum(player, Stat.SOUL_MAX, "RebirthCorrectJingli", "RebirthCorrectJingliPercentage");
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
        player.displayClientMessage(Component.literal((lifetimeChange > 0 ? "§6增长寿元" : "§4减去寿元")
                + WHOLE.format(Math.abs(lifetimeChange)) + "天"), false);
        Cultivation.set(player, CultivationValue.LIFETIME, lifetimeBefore + lifetimeChange);
    }

    private static double correctedMaximum(ServerPlayer player, Stat stat, String additiveKey, String percentageKey) {
        double value = PlayerAttributes.value(player, stat) * 0.2 + Cultivation.tizhiDouble(player, additiveKey);
        double percentage = Cultivation.tizhiDouble(player, percentageKey);
        if (percentage != 0) {
            value *= percentage + 1;
        }
        return value;
    }
}
