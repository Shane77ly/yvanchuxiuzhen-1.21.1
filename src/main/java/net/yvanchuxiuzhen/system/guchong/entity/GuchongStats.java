package net.yvanchuxiuzhen.system.guchong.entity;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.attribute.api.EntityStats;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 01、03、04号蛊虫的来源等级与十三项资质计算。 */
final class GuchongStats {
    private GuchongStats() { }

    static void initialize(GuchongEntity entity, String... ratings) {
        initialize(entity, 1, ratings);
    }

    static void initialize(GuchongEntity entity, int defaultLevel, String... ratings) {
        EntityStats current = EntityAttributes.get(entity);
        if (!current.realmName().isEmpty()) return;
        double level = current.level() < 1 ? defaultLevel : current.level();
        Map<Stat, String> nextRatings = new EnumMap<>(Stat.class);
        for (int i = 0; i < EntityAttributes.COMBAT_STATS.size(); i++) {
            nextRatings.put(EntityAttributes.COMBAT_STATS.get(i), ratings[i]);
        }
        double experienceMax = current.experienceMax();
        if (level <= 40 && experienceMax < 1) {
            double max = 100;
            for (int n = 1; n <= (int) level; n++) max += n * n * 10 + 10;
            experienceMax = max;
        }
        EntityAttributes.set(entity, current.withLevel(level).withExperienceMax(experienceMax)
                .withRealmName(realmNameForLevel(level)).withRatings(nextRatings));
        calculateAttributes(entity);
        String team = entity.getType().builtInRegistryHolder().key().location().toString().replace(':', '_');
        Relations.setTeam(entity, team);
        Relations.addFriendlyTeam(entity, team);
    }

    static void updateLevelName(Entity entity) {
        EntityAttributes.setRealmName(entity, realmNameForLevel(EntityAttributes.level(entity)));
    }

    private static String realmNameForLevel(double level) {
        int rank = Mth.clamp(((int) level - 1) / 5 + 1, 1, 10);
        int step = Mth.clamp((int) level - (rank - 1) * 5, 1, 5);
        String stepText = new String[]{"初级", "低级", "中级", "高级", "圆满"}[step - 1];
        return new String[]{"一", "二", "三", "四", "五", "六", "七", "八", "九", "十"}[rank - 1] + "阶" + stepText;
    }

    static void calculateUpgradeAttributes(Entity entity) {
        calculateAttributes(entity);
    }

    static void calculateAttributes(Entity entity) {
        EntityStats current = EntityAttributes.get(entity);
        double level = current.level();
        Map<Stat, Double> values = new EnumMap<>(Stat.class);
        double healthMax = Math.ceil(120 + rating(current, Stat.HEALTH_MAX, 7, 6, 5, 4, 3) * Math.pow(level / 3, 2));
        values.put(Stat.HEALTH_MAX, healthMax);
        LivingEntity living = (LivingEntity) entity;
        living.getAttribute(Attributes.MAX_HEALTH).setBaseValue(healthMax);
        living.setHealth((float) healthMax);
        values.put(Stat.HEALTH_REC, Math.ceil(1 + Math.pow(level / rating(current, Stat.HEALTH_REC, 16, 18, 20, 22, 24), 2)));
        values.put(Stat.POWER_MAX, Math.ceil(120 + rating(current, Stat.POWER_MAX, 14, 13, 12, 11, 10) * Math.pow(level / 3, 2)));
        values.put(Stat.POWER_REC, Math.ceil(Math.pow(level / rating(current, Stat.POWER_REC, 24, 22, 20, 18, 16), 2)));
        values.put(Stat.SOUL_MAX, Math.ceil(120 + rating(current, Stat.SOUL_MAX, 14, 13, 12, 11, 10) * Math.pow(level / 3, 2)));
        values.put(Stat.SOUL_REC, Math.ceil(Math.pow(level / rating(current, Stat.SOUL_REC, 24, 22, 20, 18, 16), 2)));
        for (Stat stat : new Stat[]{Stat.ATTACK, Stat.ATTACK_DEF, Stat.MANA_ATT, Stat.MANA_DEF, Stat.SOUL_ATT, Stat.SOUL_DEF}) {
            double value = Math.ceil(2 + Math.pow(level / rating(current, stat, 2.6, 3.2, 3.8, 4.4, 5), 2));
            values.put(stat, value);
            if (stat == Stat.ATTACK) living.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(value);
        }
        double speed = rating(current, Stat.MOVE_SPEED, .3, .25, .2, .15, .1) + level * .005;
        values.put(Stat.MOVE_SPEED, speed);
        living.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(speed);
        EntityAttributes.set(entity, current.withValues(values));
    }

    private static double rating(EntityStats stats, Stat key, double excellent, double strong, double average, double weak, double poor) {
        return switch (stats.rating(key)) {
            case "绝佳" -> excellent;
            case "强劲" -> strong;
            case "平庸" -> average;
            case "勉强" -> weak;
            default -> poor;
        };
    }
}
