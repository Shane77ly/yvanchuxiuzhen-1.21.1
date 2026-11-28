package net.yvanchuxiuzhen.system.xiulian;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/** 原初管理命令的修炼字段、灵根、体质标记与本系统世界规则分支。 */
public final class CultivationCommand {
    private CultivationCommand() {
    }

    public static boolean execute(ServerLevel level, ServerPlayer player, String action, String subject,
            String operation, String value, String feedback) {
        if (action.equals("levelup") && subject.equals("level")) {
            double amount = number(value);
            Cultivation.set(player, CultivationValue.LEVEL_NUMBER,
                    operation.equals("set") ? amount : Cultivation.get(player, CultivationValue.LEVEL_NUMBER) + amount);
            if (feedback.equals("true")) {
                message(player, operation.equals("set") ? "§6等级设置为" + value
                        : "§6等级变更为" + Cultivation.get(player, CultivationValue.LEVEL_NUMBER));
            }
            Cultivation.set(player, CultivationValue.EXPERIENCE_MAX,
                    100 + Math.pow(Cultivation.get(player, CultivationValue.LEVEL_NUMBER), 3) * 50);
            return true;
        }
        if (action.equals("experienceup")) {
            return experience(level, player, subject, operation, value, feedback);
        }
        if (subject.equals("rebirthgui") && (action.equals("unlock") || action.equals("lock"))) {
            boolean enabled = action.equals("unlock");
            level.getGameRules().getRule(XiulianRules.BIRTH_SELECTION).set(enabled, level.getServer());
            if (operation.equals("true")) {
                message(player, enabled ? "已开启，进入游戏后，打开灵根体质出身选择界面。"
                        : "已关闭，进入游戏后，打开灵根体质出身选择界面。");
            }
            return true;
        }
        if (!action.equals("unlock")) return false;
        switch (subject) {
            case "chongzhilinggen" -> Cultivation.resetRoots(player);
            case "chongzhilinggenmax" -> Cultivation.maximizeRoots(player);
            case "tizhi" -> {
                boolean add = operation.equals("add");
                Cultivation.editTizhi(player, tag -> {
                    tag.setFlag(value, add);
                    if (feedback.equals("true")) {
                        message(player, "§6" + (add ? "获得名为" : "移除名为") + value + "的体质");
                    }
                    tag.setNumber("tizhinumber", tag.number("tizhinumber") + (add ? 1 : -1));
                });
            }
            default -> { return false; }
        }
        return true;
    }

    private static boolean experience(ServerLevel level, ServerPlayer player, String subject,
            String operation, String value, String feedback) {
        double amount = number(value);
        boolean set = operation.equals("set");
        if (subject.equals("dailyexperience")) {
            XiulianWorldData data = XiulianWorldData.get(level.getServer());
            data.setDailyExperience(set ? amount : data.dailyExperience() + amount);
            if (feedback.equals("true")) {
                message(player, "§6成功设置每日修为获取上限：" + (set ? value : Double.toString(data.dailyExperience())));
            }
            return true;
        }
        if (subject.equals("linggenlevel")) {
            int old = level.getGameRules().getInt(XiulianRules.ROOT_LEVEL);
            level.getGameRules().getRule(XiulianRules.ROOT_LEVEL).set((int) (set ? amount : old + amount), level.getServer());
            if (feedback.equals("true")) {
                message(player, "§6成功设置灵根等级随机上限："
                        + (set ? value : Integer.toString(level.getGameRules().getInt(XiulianRules.ROOT_LEVEL))));
            }
            return true;
        }
        CultivationValue field = switch (subject) {
            case "experience" -> CultivationValue.EXPERIENCE;
            case "experiencestand" -> CultivationValue.EXPERIENCE_STAND;
            case "rate" -> CultivationValue.EXPERIENCE_RATE;
            case "hinder" -> CultivationValue.EXPERIENCE_HINDER;
            case "skillpoints" -> CultivationValue.SKILL_POINTS;
            case "vigorous" -> CultivationValue.VIGOROUS;
            case "merits" -> CultivationValue.MERITS;
            case "bodygold" -> CultivationValue.ROOT_GOLD;
            case "bodywater" -> CultivationValue.ROOT_WATER;
            case "bodytree" -> CultivationValue.ROOT_TREE;
            case "bodyfire" -> CultivationValue.ROOT_FIRE;
            case "bodysoil" -> CultivationValue.ROOT_SOIL;
            case "lifetime" -> CultivationValue.LIFETIME;
            case "liftlock" -> CultivationValue.LIFTLOCK;
            default -> null;
        };
        if (field == null) return false;
        double old = Cultivation.get(player, field);
        Cultivation.set(player, field, set ? amount : old + amount);
        if (field == CultivationValue.ROOT_GOLD || field == CultivationValue.ROOT_FIRE) {
            double yinYang = Cultivation.get(player, CultivationValue.YINYANG);
            Cultivation.set(player, CultivationValue.YINYANG, set ? yinYang + amount - old : yinYang + amount);
        } else if (field == CultivationValue.ROOT_WATER || field == CultivationValue.ROOT_TREE) {
            double yinYang = Cultivation.get(player, CultivationValue.YINYANG);
            Cultivation.set(player, CultivationValue.YINYANG, set ? yinYang - (amount - old) : yinYang - amount);
        }
        if (feedback.equals("true")) {
            String title = switch (field) {
                case EXPERIENCE, EXPERIENCE_STAND -> "经验";
                case EXPERIENCE_RATE -> "修为吸收率";
                case EXPERIENCE_HINDER -> "道途";
                case ROOT_GOLD -> "金灵根";
                case ROOT_WATER -> "水灵根";
                case ROOT_TREE -> "木灵根";
                case ROOT_FIRE -> "火灵根";
                case ROOT_SOIL -> "土灵根";
                case LIFETIME -> "寿元";
                case LIFTLOCK -> "命轮锁";
                default -> "";
            };
            if (!title.isEmpty()) message(player, "§6" + title + (set ? "设置为" : "增减量为") + value);
        }
        return true;
    }

    private static void message(ServerPlayer player, String text) {
        player.displayClientMessage(Component.literal(text), false);
    }

    /** 来源管理命令按 double 读取，格式错误或空参数取 0。 */
    private static double number(String text) {
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException exception) {
            return 0;
        }
    }
}
