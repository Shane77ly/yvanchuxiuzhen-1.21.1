package net.yvanchuxiuzhen.system.combat.api;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;

/** 五行击杀数量与属性技能熟练度的管理入口，沿用伤害结算写入的同一体质载体键。 */
public final class ElementStatistics {
    private ElementStatistics() {
    }

    /** 来源 YvanchuGo3 的十个统计分支；只修改指定统计，不触发伤害、击杀或成长奖励。 */
    public static boolean command(ServerPlayer player, String action, String subject, String operation, String value, String feedback) {
        if (!action.equals("experienceup")) return false;
        String title = switch (subject) {
            case "killentitygold" -> "金属性技能杀死的实体数量";
            case "killentitypointsgold" -> "金属性技能熟练度";
            case "killentitywater" -> "水属性技能杀死的实体数量";
            case "killentitypointswater" -> "水属性技能熟练度";
            case "killentitytree" -> "木属性技能杀死的实体数量";
            case "killentitypointstree" -> "木属性技能熟练度";
            case "killentityfire" -> "火属性技能杀死的实体数量";
            case "killentitypointsfire" -> "火属性技能熟练度";
            case "killentitysoil" -> "土属性技能杀死的实体数量";
            case "killentitypointssoil" -> "土属性技能熟练度";
            default -> null;
        };
        if (title == null) return false;
        boolean set = operation.equals("set");
        double amount = number(value);
        Cultivation.setTizhiDouble(player, subject, set ? amount : Cultivation.tizhiDouble(player, subject) + amount);
        if (feedback.equals("true")) {
            player.displayClientMessage(Component.literal("§6" + title + (set ? "设置为" : "增减量为") + value), false);
        }
        return true;
    }

    /** 来源数值转换保留负数、小数和科学记数法，格式错误或空参数取 0。 */
    private static double number(String text) {
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException exception) {
            return 0;
        }
    }
}
