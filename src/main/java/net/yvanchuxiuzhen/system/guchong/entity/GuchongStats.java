package net.yvanchuxiuzhen.system.guchong.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 003/004 号蛊虫来源等级与十三项资质计算。 */
final class GuchongStats {
    private static final String[] KEYS = {"health_Max", "health_Rec", "power_Max", "power_Rec", "soul_Max", "soul_Rec", "attack_Num", "attack_Def", "mana_Att", "mana_Def", "soul_Att", "soul_Def", "speed"};
    private GuchongStats() { }

    static void initialize(GuchongEntity entity, String... ratings) {
        CompoundTag data = entity.getPersistentData();
        if (!data.getString("LEVELname").isEmpty()) return;
        if (data.getDouble("LEVELnumber") < 1) data.putDouble("LEVELnumber", 1);
        double level = data.getDouble("LEVELnumber");
        for (int i = 0; i < KEYS.length; i++) data.putString(KEYS[i] + "_LEVEL", ratings[i]);
        int rank = Mth.clamp(((int) level - 1) / 5 + 1, 1, 10);
        int step = Mth.clamp((int) level - (rank - 1) * 5, 1, 5);
        data.putString("LEVELname", new String[]{"初级", "低级", "中级", "高级", "圆满"}[step - 1]);
        data.putString("LEVELname", new String[]{"一", "二", "三", "四", "五", "六", "七", "八", "九", "十"}[rank - 1] + "阶" + data.getString("LEVELname"));
        if (level <= 40 && data.getDouble("experience_Max") < 1) {
            double max = 100;
            for (int n = 1; n <= (int) level; n++) max += n * n * 10 + 10;
            data.putDouble("experience_Max", max);
        }
        data.putDouble("health_Max", Math.ceil(120 + rating(data, "health_Max", 7, 6, 5, 4, 3) * Math.pow(level / 3, 2)));
        data.putDouble("health_Rec", Math.ceil(1 + Math.pow(level / rating(data, "health_Rec", 16, 18, 20, 22, 24), 2)));
        data.putDouble("power_Max", Math.ceil(120 + rating(data, "power_Max", 14, 13, 12, 11, 10) * Math.pow(level / 3, 2)));
        data.putDouble("power_Rec", Math.ceil(Math.pow(level / rating(data, "power_Rec", 24, 22, 20, 18, 16), 2)));
        data.putDouble("soul_Max", Math.ceil(120 + rating(data, "soul_Max", 14, 13, 12, 11, 10) * Math.pow(level / 3, 2)));
        data.putDouble("soul_Rec", Math.ceil(Math.pow(level / rating(data, "soul_Rec", 24, 22, 20, 18, 16), 2)));
        for (String key : new String[]{"attack_Num", "attack_Def", "mana_Att", "mana_Def", "soul_Att", "soul_Def"})
            data.putDouble(key, Math.ceil(2 + Math.pow(level / rating(data, key, 2.6, 3.2, 3.8, 4.4, 5), 2)));
        double speed = rating(data, "speed", .3, .25, .2, .15, .1) + level * .005;
        data.putDouble("speed_number", speed);
        entity.getAttribute(Attributes.MAX_HEALTH).setBaseValue(data.getDouble("health_Max"));
        entity.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(data.getDouble("attack_Num"));
        entity.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(speed);
        entity.setHealth((float) data.getDouble("health_Max"));
        String team = entity.getType().builtInRegistryHolder().key().location().toString().replace(':', '_');
        Relations.setTeam(entity, team); Relations.addFriendlyTeam(entity, team);
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
}
