package net.yvanchuxiuzhen.system.creature.entity;

import java.util.Random;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** Creature-owned translation of the source level, rating, and personality procedures. */
final class CreatureSourceStats {
    private static final String[] KEYS = {"health_Max", "health_Rec", "power_Max", "power_Rec", "soul_Max", "soul_Rec", "attack_Num", "attack_Def", "mana_Att", "mana_Def", "soul_Att", "soul_Def", "speed"};
    private CreatureSourceStats() { }

    static void initialize(Mob mob, int defaultLevel, boolean experience, String... ratings) {
        initialize(mob, defaultLevel, experience, true, ratings);
    }

    static void initialize(Mob mob, int defaultLevel, boolean experience, boolean personality, String... ratings) {
        CompoundTag data = mob.getPersistentData();
        if (!data.getString("LEVELname").isEmpty()) return;
        if (data.getDouble("LEVELnumber") < 1) data.putDouble("LEVELnumber", defaultLevel);
        double level = data.getDouble("LEVELnumber");
        for (int i = 0; i < KEYS.length; i++) data.putString(KEYS[i] + "_LEVEL", ratings[i]);
        int rank = Mth.clamp(((int) level - 1) / 5 + 1, 1, 10);
        int step = Mth.clamp((int) level - (rank - 1) * 5, 1, 5);
        data.putString("LEVELname", new String[]{"初级", "低级", "中级", "高级", "圆满"}[step - 1]);
        data.putString("LEVELname", new String[]{"一", "二", "三", "四", "五", "六", "七", "八", "九", "十"}[rank - 1] + "阶" + data.getString("LEVELname"));
        if (experience && level <= 40 && data.getDouble("experience_Max") < 1) {
            double max = 100;
            for (int n = 1; n <= (int) level; n++) max += n * n * 10 + 10;
            data.putDouble("experience_Max", max);
        }
        data.putDouble("health_Max", Math.ceil(120 + rating(data, "health_Max", 14, 13, 12, 11, 10) * Math.pow(level / 3, 2)));
        data.putDouble("health_Rec", Math.ceil(1 + Math.pow(level / rating(data, "health_Rec", 8.4, 8.8, 9.2, 9.6, 10), 2)));
        data.putDouble("power_Max", Math.ceil(120 + rating(data, "power_Max", 28, 26, 24, 22, 20) * Math.pow(level / 3, 2)));
        data.putDouble("power_Rec", Math.ceil(Math.pow(level / rating(data, "power_Rec", 6, 6.5, 7, 7.5, 8), 2)));
        data.putDouble("soul_Max", Math.ceil(120 + rating(data, "soul_Max", 28, 26, 24, 22, 20) * Math.pow(level / 3, 2)));
        data.putDouble("soul_Rec", Math.ceil(Math.pow(level / rating(data, "soul_Rec", 6, 6.5, 7, 7.5, 8), 2)));
        for (String key : new String[]{"attack_Num", "attack_Def", "mana_Att", "mana_Def", "soul_Att", "soul_Def"})
            data.putDouble(key, Math.ceil(2 + Math.pow(level / rating(data, key, 2.6, 2.7, 2.8, 2.9, 3), 2)));
        double speed = rating(data, "speed", .36, .32, .28, .24, .2) + level * .004;
        data.putDouble("speed_number", speed);
        mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(data.getDouble("health_Max"));
        mob.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(data.getDouble("attack_Num"));
        mob.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(speed);
        mob.setHealth((float) data.getDouble("health_Max"));
        if (personality) initializeCharacter(data);
        String team = mob.getType().builtInRegistryHolder().key().location().toString().replace(':', '_');
        Relations.setTeam(mob, team);
        Relations.addFriendlyTeam(mob, team);
    }

    private static double rating(CompoundTag data, String key, double excellent, double strong, double average, double weak, double poor) {
        return switch (data.getString(key + "_LEVEL")) {
            case "绝佳" -> excellent;
            case "强劲" -> strong;
            case "平庸" -> average;
            case "勉强" -> weak;
            default -> poor;
        };
    }

    static void initializeCharacter(CompoundTag data) {
        Random random = new Random();
        String character;
        if (random.nextDouble() < .2) character = "慵懒";
        else if (random.nextDouble() < .25) character = "谨慎";
        else if (random.nextDouble() < .333) character = "急躁";
        else if (random.nextDouble() < .5) character = "狡诈";
        else character = "平和";
        data.putString("character", character);
        double[] values = switch (character) {
            case "慵懒" -> new double[]{.5, .5, 2, .5};
            case "谨慎" -> new double[]{1.5, .5, 1.5, 1.3};
            case "急躁" -> new double[]{.2, 1.8, .5, 2};
            case "狡诈" -> new double[]{1, 1, .7, 1.6};
            default -> new double[]{1, 1, 1, 1};
        };
        data.putDouble("character_def", values[0]); data.putDouble("character_att", values[1]);
        data.putDouble("character_interval", values[2]); data.putDouble("character_resist", values[3]);
    }
}
