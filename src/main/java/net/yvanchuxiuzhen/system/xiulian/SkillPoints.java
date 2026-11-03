package net.yvanchuxiuzhen.system.xiulian;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;
import net.yvanchuxiuzhen.system.xiulian.api.SkillPoint;

/**
 * 分配属性点：有属性点且该项已分配点数不超过层数 × 1.5 时，该项成长加“每点增量 + 体质修正”，属性点减 1、该项点数加 1，
 * 寿元加 {@code ShouyvanAddSkillCorrect} 后乘 (1 + {@code ShouyvanAddSkillCorrectPercentage})。
 * 生命上限另回复 4 点生命，灵力与神识上限另加当前值 40。成长作为 attribute 的基础层贡献提交，原版属性随之重算。
 */
public final class SkillPoints {
    private SkillPoints() {
    }

    public static void spend(ServerPlayer player, SkillPoint point) {
        if (Cultivation.get(player, CultivationValue.SKILL_POINTS) < 1) {
            player.displayClientMessage(Component.literal("§4技能点不足，无法分配点数。"), false);
            return;
        }
        if (Cultivation.get(player, point.spent()) > Cultivation.get(player, CultivationValue.LEVEL_NUMBER) * 1.5) {
            player.displayClientMessage(Component.literal("§4分配点数已达当前境界最高值，无法继续增加。"), false);
            return;
        }
        Cultivation.addGrowth(player, point.stat(), point.amount() + Cultivation.tizhiDouble(player, point.correctionKey()));
        switch (point) {
            case HEALTH_MAX -> player.setHealth(player.getHealth() + 4);
            case POWER_MAX -> Cultivation.add(player, CultivationValue.POWER, 40);
            case SOUL_MAX -> Cultivation.add(player, CultivationValue.SOUL, 40);
            default -> {
            }
        }
        Cultivation.add(player, CultivationValue.SKILL_POINTS, -1);
        Cultivation.add(player, point.spent(), 1);
        double lifetimeBonus = Cultivation.tizhiDouble(player, "ShouyvanAddSkillCorrect");
        if (lifetimeBonus != 0) {
            Cultivation.add(player, CultivationValue.LIFETIME, lifetimeBonus);
        }
        double lifetimePercentage = Cultivation.tizhiDouble(player, "ShouyvanAddSkillCorrectPercentage");
        if (lifetimePercentage != 0) {
            Cultivation.set(player, CultivationValue.LIFETIME, Cultivation.get(player, CultivationValue.LIFETIME) * (lifetimePercentage + 1));
        }
    }
}
