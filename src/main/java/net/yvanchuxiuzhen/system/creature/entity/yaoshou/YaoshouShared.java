package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import java.util.Random;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 妖兽来源共用等级、资质、尺寸、性格与团队真值。 */
public final class YaoshouShared {
    private static final String[] RATING_KEYS = {
            "health_Max", "health_Rec", "power_Max", "power_Rec", "soul_Max", "soul_Rec",
            "attack_Num", "attack_Def", "mana_Att", "mana_Def", "soul_Att", "soul_Def", "speed"
    };
    private YaoshouShared() { }

    public static boolean initialize(YaoshouEntity entity, int defaultLevel, String... ratings) {
        return initialize(entity, defaultLevel, true, ratings);
    }

    public static boolean initializeWithoutExperience(YaoshouEntity entity, int defaultLevel, String... ratings) {
        return initialize(entity, defaultLevel, false, ratings);
    }

    private static boolean initialize(YaoshouEntity entity, int defaultLevel, boolean experience, String... ratings) {
        CompoundTag data = entity.getPersistentData();
        if (!data.getString("LEVELname").isEmpty()) return false;
        if (data.getDouble("LEVELnumber") < 1) data.putDouble("LEVELnumber", defaultLevel);
        double level = data.getDouble("LEVELnumber");
        entity.getAttribute(YaoshouAttributes.SIZE).setBaseValue((new Random().nextInt(31) + 70) * 0.01 + (level / 4) * 0.1);
        for (int i = 0; i < RATING_KEYS.length; i++) {
            data.putString(RATING_KEYS[i] + "_LEVEL", ratings[i]);
        }
        updateLevelName(data, level);
        if (experience && level <= 40 && data.getDouble("experience_Max") < 1) {
            double maximum = 100;
            for (int n = 1; n <= (int) level; n++) maximum += n * n * 10 + 10;
            data.putDouble("experience_Max", maximum);
        }
        calculateAttributes(entity, data, level);
        initializeCharacter(entity, data);
        String team = entity.getType().builtInRegistryHolder().key().location().toString().replace(':', '_');
        Relations.setTeam(entity, team);
        Relations.addFriendlyTeam(entity, team);
        return true;
    }

    public static void calculateAttributes(Mob entity, CompoundTag data, double level) {
        double healthScale = maxRating(data, "health_Max", 14, 13, 12, 11);
        data.putDouble("health_Max", Math.ceil(120 + healthScale * Math.pow(level / 3, 2)));
        double healthRecovery = recoveryRating(data, "health_Rec", 8.4, 8.8, 9.2, 9.6);
        data.putDouble("health_Rec", Math.ceil(1 + Math.pow(level / healthRecovery, 2)));
        data.putDouble("power_Max", Math.ceil(120 + maxRating(data, "power_Max", 28, 26, 24, 22) * Math.pow(level / 3, 2)));
        data.putDouble("power_Rec", Math.ceil(Math.pow(level / recoveryRating(data, "power_Rec", 6, 6.5, 7, 7.5), 2)));
        data.putDouble("soul_Max", Math.ceil(120 + maxRating(data, "soul_Max", 28, 26, 24, 22) * Math.pow(level / 3, 2)));
        data.putDouble("soul_Rec", Math.ceil(Math.pow(level / recoveryRating(data, "soul_Rec", 6, 6.5, 7, 7.5), 2)));
        for (String key : new String[]{"attack_Num", "attack_Def", "mana_Att", "mana_Def", "soul_Att", "soul_Def"}) {
            double divisor = maxRating(data, key, 2.6, 2.7, 2.8, 2.9);
            data.putDouble(key, Math.ceil(2 + Math.pow(level / divisor, 2)));
        }
        double speed = switch (data.getString("speed_LEVEL")) {
            case "绝佳" -> 0.36;
            case "强劲" -> 0.32;
            case "平庸" -> 0.28;
            case "勉强" -> 0.24;
            default -> 0.2;
        } + level * 0.004;
        data.putDouble("speed_number", speed);
        entity.getAttribute(Attributes.MAX_HEALTH).setBaseValue(data.getDouble("health_Max"));
        entity.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(data.getDouble("attack_Num"));
        entity.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(speed);
        entity.setHealth((float) data.getDouble("health_Max"));
    }

    public static void updateLevelName(CompoundTag data, double level) {
        int rank = Mth.clamp(((int) level - 1) / 5 + 1, 1, 10);
        int step = Mth.clamp((int) level - (rank - 1) * 5, 1, 5);
        data.putString("LEVELname", new String[]{"初级", "低级", "中级", "高级", "圆满"}[step - 1]);
        data.putString("LEVELname", new String[]{"一", "二", "三", "四", "五", "六", "七", "八", "九", "十"}[rank - 1] + "阶" + data.getString("LEVELname"));
    }

    public static void initializeCharacter(Mob entity, CompoundTag data) {
        String character;
        if (Math.random() < 0.2) character = "慵懒";
        else if (Math.random() < 0.25) character = "谨慎";
        else if (Math.random() < 0.333) character = "急躁";
        else if (Math.random() < 0.5) character = "狡诈";
        else character = "平和";
        data.putString("character", character);
        switch (character) {
            case "慵懒" -> setCharacter(data, 0.5, 0.5, 2, 0.5);
            case "谨慎" -> setCharacter(data, 1.5, 0.5, 1.5, 1.3);
            case "急躁" -> setCharacter(data, 0.2, 1.8, 0.5, 2);
            case "狡诈" -> setCharacter(data, 1, 1, 0.7, 1.6);
            default -> setCharacter(data, 1, 1, 1, 1);
        }
    }

    public static boolean isFriendly(Entity judge, Entity other) {
        return Relations.isFriendly(judge, other);
    }

    public static void finishSkill(Entity entity) {
        CompoundTag data = entity.getPersistentData();
        data.putString("jineng", "");
        data.putString("jinengjinxing", "");
        data.putDouble("entitytick", 0);
        data.putDouble("entitytick2", 0);
    }

    public static void escape(Entity entity) {
        CompoundTag data = entity.getPersistentData();
        String phase = data.getString("jinengjinxing");
        if (phase.isEmpty()) {
            data.putString("jinengjinxing", "准备");
        } else if (phase.equals("准备") && data.getDouble("entitytick") >= 20) {
            entity.stopRiding();
            entity.setDeltaMovement(entity.getLookAngle().x * 0.5, 0.7, entity.getLookAngle().z * 0.5);
            data.putString("jinengjinxing", "跳跃");
        } else if (phase.equals("跳跃") && data.getDouble("entitytick") >= 30) {
            finishSkill(entity);
        }
    }

    private static double maxRating(CompoundTag data, String key, double excellent, double strong, double average, double weak) {
        return switch (data.getString(key + "_LEVEL")) {
            case "绝佳" -> excellent;
            case "强劲" -> strong;
            case "平庸" -> average;
            case "勉强" -> weak;
            default -> key.equals("health_Max") ? 10 : key.equals("power_Max") || key.equals("soul_Max") ? 20 : 3;
        };
    }

    private static double recoveryRating(CompoundTag data, String key, double excellent, double strong, double average, double weak) {
        return switch (data.getString(key + "_LEVEL")) {
            case "绝佳" -> excellent;
            case "强劲" -> strong;
            case "平庸" -> average;
            case "勉强" -> weak;
            default -> key.equals("health_Rec") ? 10 : 8;
        };
    }

    private static void setCharacter(CompoundTag data, double defense, double attack, double interval, double resist) {
        data.putDouble("character_def", defense);
        data.putDouble("character_att", attack);
        data.putDouble("character_interval", interval);
        data.putDouble("character_resist", resist);
    }
}
