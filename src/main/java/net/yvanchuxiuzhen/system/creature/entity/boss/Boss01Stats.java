package net.yvanchuxiuzhen.system.creature.entity.boss;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.skill.api.SkillDelays;

/** 首领01采用修者来源的独立资质公式、等级名与延迟生命值写入。 */
final class Boss01Stats {
    private Boss01Stats() { }

    static void initialize(Boss01Entity boss) {
        CompoundTag data = boss.getPersistentData();
        if (!data.getString("LEVELname").isEmpty()) return;
        if (data.getDouble("LEVELnumber") < 1) data.putDouble("LEVELnumber", 15);
        double level = data.getDouble("LEVELnumber");
        data.putString("health_Max_LEVEL", "强劲");
        data.putString("health_Rec_LEVEL", "平庸");
        data.putString("power_Max_LEVEL", "强劲");
        data.putString("power_Rec_LEVEL", "勉强");
        data.putString("soul_Max_LEVEL", "平庸");
        data.putString("soul_Rec_LEVEL", "勉强");
        data.putString("attack_Num_LEVEL", "平庸");
        data.putString("attack_Def_LEVEL", "平庸");
        data.putString("mana_Att_LEVEL", "强劲");
        data.putString("mana_Def_LEVEL", "平庸");
        data.putString("soul_Att_LEVEL", "强劲");
        data.putString("soul_Def_LEVEL", "强劲");
        data.putString("speed_LEVEL", "平庸");
        levelName(data, level);

        data.putDouble("health_Max", Math.ceil(100 + 13 * Math.pow(level / 4, 2)));
        data.putDouble("health_Rec", Math.ceil(1 + Math.pow(level / 9.2, 2)));
        data.putDouble("power_Max", Math.ceil(120 + 26 * Math.pow(level / 3, 2)));
        data.putDouble("power_Rec", Math.ceil(Math.pow(level / 7.5, 2)));
        data.putDouble("soul_Max", Math.ceil(120 + 24 * Math.pow(level / 3, 2)));
        data.putDouble("soul_Rec", Math.ceil(Math.pow(level / 7.5, 2)));
        data.putDouble("attack_Num", Math.ceil(2 + Math.pow(level / 3.4, 2)));
        data.putDouble("attack_Def", Math.ceil(2 + Math.pow(level / 3.4, 2)));
        data.putDouble("mana_Att", Math.ceil(2 + Math.pow(level / 3.2, 2)));
        data.putDouble("mana_Def", Math.ceil(2 + Math.pow(level / 3.4, 2)));
        data.putDouble("soul_Att", Math.ceil(2 + Math.pow(level / 3.2, 2)));
        data.putDouble("soul_Def", Math.ceil(2 + Math.pow(level / 3.2, 2)));
        data.putDouble("speed_number", 0.24 + level * 0.01);
        boss.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(data.getDouble("attack_Num"));
        boss.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(data.getDouble("speed_number"));
        Relations.setTeam(boss, "yvanchuxiuzhen_ycxzboss_01");
        Relations.addFriendlyTeam(boss, "yvanchuxiuzhen_ycxzboss_01");
        SkillDelays.after(20, () -> {
            boss.getAttribute(Attributes.MAX_HEALTH).setBaseValue(data.getDouble("health_Max"));
            boss.setHealth((float) data.getDouble("health_Max"));
        });
    }

    private static void levelName(CompoundTag data, double level) {
        int value = (int) level;
        if (value >= 46) {
            data.putString("LEVELname", value >= 50 ? "化神圆满" : "化神" + chinese(value - 40) + "级");
        } else if (value >= 41) {
            data.putString("LEVELname", "化神" + chinese(value - 40) + "级");
        } else if (value >= 31) {
            data.putString("LEVELname", "元婴" + chinese(value - 30) + "重");
        } else if (value >= 21) {
            data.putString("LEVELname", "结丹" + chinese(value - 20) + "转");
        } else if (value >= 11) {
            data.putString("LEVELname", "筑基" + chinese(value - 10) + "阶");
        } else {
            data.putString("LEVELname", "练气" + chinese(value) + "层");
        }
        if (value == 15) data.putString("LEVELname", "筑基五阶");
        if (value == 10) data.putString("LEVELname", "练气圆满");
        if (value == 20) data.putString("LEVELname", "筑基圆满");
        if (value == 30) data.putString("LEVELname", "结丹圆满");
        if (value == 40) data.putString("LEVELname", "元婴圆满");
    }

    private static String chinese(int number) {
        return switch (number) {
            case 1 -> "一"; case 2 -> "二"; case 3 -> "三"; case 4 -> "四"; case 5 -> "五";
            case 6 -> "六"; case 7 -> "七"; case 8 -> "八"; case 9 -> "九"; default -> "一";
        };
    }
}
