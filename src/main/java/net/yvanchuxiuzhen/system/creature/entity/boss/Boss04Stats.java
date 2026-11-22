package net.yvanchuxiuzhen.system.creature.entity.boss;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.skill.api.SkillDelays;

/** 04号首领采用来源修者等级与属性公式，生命值在二十刻后写入。 */
final class Boss04Stats {
    private static final String[] KEYS = {"health_Max", "health_Rec", "power_Max", "power_Rec", "soul_Max", "soul_Rec", "attack_Num", "attack_Def", "mana_Att", "mana_Def", "soul_Att", "soul_Def", "speed"};
    private static final String EXCELLENT = "绝佳";
    private static final String STRONG = "强劲";
    private static final String WEAK = "勉强";
    private static final String AVERAGE = "平庸";

    private Boss04Stats() { }

    static void initialize(Boss04Entity boss) {
        CompoundTag data = boss.getPersistentData();
        if (!data.getString("LEVELname").isEmpty()) return;
        if (data.getDouble("LEVELnumber") < 1) data.putDouble("LEVELnumber", 40);
        double level = data.getDouble("LEVELnumber");
        String[] ratings = {EXCELLENT, STRONG, WEAK, WEAK, WEAK, WEAK, EXCELLENT, STRONG, STRONG, AVERAGE, AVERAGE, WEAK, STRONG};
        for (int i = 0; i < KEYS.length; i++) data.putString(KEYS[i] + "_LEVEL", ratings[i]);
        setLevelName(data, level);
        data.putDouble("health_Max", Math.ceil(100 + 14 * Math.pow(level / 4, 2)));
        data.putDouble("health_Rec", Math.ceil(1 + Math.pow(level / 8.8, 2)));
        data.putDouble("power_Max", Math.ceil(120 + 22 * Math.pow(level / 3, 2)));
        data.putDouble("power_Rec", Math.ceil(Math.pow(level / 7.5, 2)));
        data.putDouble("soul_Max", Math.ceil(120 + 22 * Math.pow(level / 3, 2)));
        data.putDouble("soul_Rec", Math.ceil(Math.pow(level / 7.5, 2)));
        data.putDouble("attack_Num", Math.ceil(2 + Math.pow(level / 3, 2)));
        data.putDouble("attack_Def", Math.ceil(2 + Math.pow(level / 3.2, 2)));
        data.putDouble("mana_Att", Math.ceil(2 + Math.pow(level / 3.2, 2)));
        data.putDouble("mana_Def", Math.ceil(2 + Math.pow(level / 3.4, 2)));
        data.putDouble("soul_Att", Math.ceil(2 + Math.pow(level / 3.4, 2)));
        data.putDouble("soul_Def", Math.ceil(2 + Math.pow(level / 3.6, 2)));
        double speed = .26 + level * .01;
        data.putDouble("speed_number", speed);
        boss.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(data.getDouble("attack_Num"));
        boss.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(speed);
        SkillDelays.after(20, () -> {
            boss.getAttribute(Attributes.MAX_HEALTH).setBaseValue(data.getDouble("health_Max"));
            boss.setHealth((float) data.getDouble("health_Max"));
        });
        String team = boss.getType().builtInRegistryHolder().key().location().toString().replace(':', '_');
        Relations.setTeam(boss, team);
        Relations.addFriendlyTeam(boss, team);
    }

    private static void setLevelName(CompoundTag data, double level) {
        if (level >= 50) {
            data.putString("LEVELname", "化神圆满");
            return;
        }
        int value = (int) level;
        int realm = (value - 1) / 10;
        int step = value - realm * 10;
        String[] realms = {"练气", "筑基", "结丹", "元婴", "化神"};
        String[] units = {"层", "阶", "转", "重", "级"};
        String[] numbers = {"一", "二", "三", "四", "五", "六", "七", "八", "九"};
        String suffix = step == 10 ? "圆满" : numbers[step - 1] + units[realm];
        data.putString("LEVELname", realms[realm] + suffix);
    }
}
