package net.yvanchuxiuzhen.system.profession;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.yvanchuxiuzhen.system.profession.api.Profession;
import net.yvanchuxiuzhen.system.profession.api.Professions;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/** 来源 YvanchuGo/2 的十职业管理分支，字段写入仍由职业公开操作完成。 */
public final class ProfessionCommand {
    private ProfessionCommand() {
    }

    public static boolean execute(ServerPlayer player, String action, String name, String operation, String value, String feedback) {
        if (!action.equals("levelup") && !action.equals("experienceup")) return false;
        Profession profession = switch (name) {
            case "alchemy" -> Profession.ALCHEMY;
            case "refiner" -> Profession.REFINING;
            case "rune" -> Profession.RUNE;
            case "collection" -> Profession.GATHERING;
            case "cooking" -> Profession.COOKING;
            case "puppet" -> Profession.PUPPET;
            case "breed" -> Profession.BREEDING;
            case "ghost" -> Profession.GHOST;
            case "insect" -> Profession.INSECT;
            case "blood" -> Profession.BLOOD;
            default -> null;
        };
        if (profession == null) return false;
        double amount = number(value);
        boolean set = operation.equals("set");
        boolean level = action.equals("levelup");
        if (level) {
            Professions.setLevel(player, profession, set ? amount : Professions.level(player, profession) + amount);
        } else if (set) {
            Professions.setExperience(player, profession, amount);
        } else {
            Professions.addExperience(player, profession, amount);
        }
        if (feedback.equals("true")) {
            String title = profession == Profession.COOKING ? "仙厨" : profession.displayName();
            String message;
            if (level) {
                // 来源加减分支的反馈读取修炼层数，执行仍修改所选职业等级。
                message = set ? title + "等级设为" + value
                        : title + "等级变更为" + Cultivation.get(player, CultivationValue.LEVEL_NUMBER);
            } else {
                message = title + (set ? "经验设置为" : "经验增减量为") + value;
            }
            player.displayClientMessage(Component.literal("§6" + message), false);
        }
        return true;
    }

    /** 来源 Double.parseDouble 失败时采用 0，保留负数、小数和科学记数法。 */
    private static double number(String text) {
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException exception) {
            return 0;
        }
    }
}
