package net.yvanchuxiuzhen.system.npc.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.yvanchuxiuzhen.system.skill.api.SkillDelays;

/** 修士来源十三项资质公式，保留不识别资质文本进入最低档的分支。 */
final class NpcStats {
    private NpcStats() { }

    static void calculate(NpcMob npc) {
        CompoundTag data = npc.getPersistentData();
        double level = data.getDouble("LEVELnumber");
        data.putDouble("health_Max", Math.ceil(100 + rating(data, "health_Max", 14, 13, 12, 11, 10) * Math.pow(level / 4, 2)));
        data.putDouble("health_Rec", Math.ceil(1 + Math.pow(level / rating(data, "health_Rec", 8.4, 8.8, 9.2, 9.6, 10), 2)));
        for (String resource : new String[]{"power", "soul"}) {
            data.putDouble(resource + "_Max", Math.ceil(120 + rating(data, resource + "_Max", 28, 26, 24, 22, 20) * Math.pow(level / 3, 2)));
            data.putDouble(resource + "_Rec", Math.ceil(Math.pow(level / rating(data, resource + "_Rec", 6, 6.5, 7, 7.5, 8), 2)));
        }
        for (String field : new String[]{"attack_Num", "attack_Def", "mana_Att", "mana_Def", "soul_Att", "soul_Def"}) {
            data.putDouble(field, Math.ceil(2 + Math.pow(level / rating(data, field, 3, 3.2, 3.4, 3.6, 3.8), 2)));
        }
        data.putDouble("speed_number", rating(data, "speed", .28, .26, .24, .22, .2) + level * .01);
        npc.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(data.getDouble("attack_Num"));
        npc.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(data.getDouble("speed_number"));
        SkillDelays.after(20, () -> {
            npc.getAttribute(Attributes.MAX_HEALTH).setBaseValue(data.getDouble("health_Max"));
            npc.setHealth((float) data.getDouble("health_Max"));
        });
        setLevelName(data, level);
    }

    private static double rating(CompoundTag data, String field, double excellent, double strong, double average, double weak, double poor) {
        return switch (data.getString(field + "_LEVEL")) {
            case "绝佳" -> excellent;
            case "强劲" -> strong;
            case "平庸" -> average;
            case "勉强" -> weak;
            default -> poor;
        };
    }

    private static void setLevelName(CompoundTag data, double level) {
        if (level < 1) { data.putString("LEVELname", "凡人"); return; }
        if (level >= 50) { data.putString("LEVELname", "化神圆满"); return; }
        int value = (int) level;
        int realm = (value - 1) / 10;
        int step = value - realm * 10;
        String[] realms = {"练气", "筑基", "结丹", "元婴", "化神"};
        String[] units = {"层", "阶", "转", "重", "级"};
        String[] numbers = {"一", "二", "三", "四", "五", "六", "七", "八", "九"};
        data.putString("LEVELname", realms[realm] + (step == 10 ? "圆满" : numbers[step - 1] + units[realm]));
    }
}
