package net.yvanchuxiuzhen.system.profession.api;

import java.text.DecimalFormat;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.yvanchuxiuzhen.system.profession.data.ProfessionAttachments;
import net.yvanchuxiuzhen.system.profession.data.ProfessionData;
import net.yvanchuxiuzhen.system.profession.data.ProfessionProgress;

/** 副职业公开操作。生产和命令只在服务端写入，客户端查询本人同步快照；增加经验不立即升级。 */
public final class Professions {
    private Professions() {
    }

    public static double level(Player player, Profession profession) {
        return player.getData(ProfessionAttachments.PROGRESS).get(profession).level();
    }

    public static double experience(Player player, Profession profession) {
        return player.getData(ProfessionAttachments.PROGRESS).get(profession).experience();
    }

    public static double experienceMax(Player player, Profession profession) {
        return player.getData(ProfessionAttachments.PROGRESS).get(profession).experienceMax();
    }

    /** 来源生产入口只增加经验；允许超过升级上限，统一由玩家每刻升级。 */
    public static void addExperience(ServerPlayer player, Profession profession, double amount) {
        setExperience(player, profession, experience(player, profession) + amount);
    }

    /** 来源 {@code YvanchuGo2} 的 experience set/add 分支，不按上限截断。 */
    public static void setExperience(ServerPlayer player, Profession profession, double amount) {
        ProfessionProgress current = player.getData(ProfessionAttachments.PROGRESS).get(profession);
        player.getData(ProfessionAttachments.PROGRESS).set(profession,
                new ProfessionProgress(current.level(), amount, current.experienceMax()));
        player.syncData(ProfessionAttachments.PROGRESS);
    }

    /** 来源等级命令只改等级，不联动修改经验或经验上限。 */
    public static void setLevel(ServerPlayer player, Profession profession, double level) {
        ProfessionProgress current = player.getData(ProfessionAttachments.PROGRESS).get(profession);
        player.getData(ProfessionAttachments.PROGRESS).set(profession,
                new ProfessionProgress(level, current.experience(), current.experienceMax()));
        if (profession == Profession.GATHERING) {
            player.getPersistentData().putDouble("collection_LEVEL", level);
        }
        player.syncData(ProfessionAttachments.PROGRESS);
    }

    /** 浮生丹把十职业的等级、上限、经验依次恢复到 0、100、0；血脉字段归修炼系统。 */
    public static void reset(ServerPlayer player) {
        ProfessionData data = player.getData(ProfessionAttachments.PROGRESS);
        for (Profession profession : Profession.values()) {
            data.set(profession, ProfessionProgress.initial());
        }
        player.getPersistentData().putDouble("collection_LEVEL", 0);
        player.syncData(ProfessionAttachments.PROGRESS);
    }

    /** 来源 {@code ZhiyeTXT01/03/.../19}：职业等级按 DecimalFormat("0") 显示。 */
    public static String levelText(Player player, Profession profession) {
        return profession.displayName() + "等级：" + new DecimalFormat("0").format(level(player, profession));
    }

    /** 来源 {@code ZhiyeTXT02/04/.../20} 的 double 字符串形式，九级仍显示经验与上限。 */
    public static String experienceText(Player player, Profession profession) {
        return experience(player, profession) + "/" + experienceMax(player, profession);
    }
}
