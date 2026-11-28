package net.yvanchuxiuzhen.system.creature.entity;

import java.util.List;
import java.util.Random;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.attribute.api.EntityStats;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.creature.data.CreatureData;
import net.yvanchuxiuzhen.system.creature.data.CreatureState;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 生物来源等级、资质与性格初始化。 */
final class CreatureSourceStats {
    private static final List<Stat> STATS = List.of(
            Stat.HEALTH_MAX, Stat.HEALTH_REC, Stat.POWER_MAX, Stat.POWER_REC, Stat.SOUL_MAX, Stat.SOUL_REC,
            Stat.ATTACK, Stat.ATTACK_DEF, Stat.MANA_ATT, Stat.MANA_DEF, Stat.SOUL_ATT, Stat.SOUL_DEF, Stat.MOVE_SPEED);
    private static final List<Stat> COMBAT_STATS = List.of(
            Stat.ATTACK, Stat.ATTACK_DEF, Stat.MANA_ATT, Stat.MANA_DEF, Stat.SOUL_ATT, Stat.SOUL_DEF);

    private CreatureSourceStats() { }

    static void initialize(Mob mob, int defaultLevel, boolean experience, String... ratings) {
        initialize(mob, defaultLevel, experience, true, ratings);
    }

    static void initialize(Mob mob, int defaultLevel, boolean experience, boolean personality, String... ratings) {
        if (!EntityAttributes.realmName(mob).isEmpty()) return;
        double level = EntityAttributes.level(mob) < 1 ? defaultLevel : EntityAttributes.level(mob);
        int rank = Mth.clamp(((int) level - 1) / 5 + 1, 1, 10);
        int step = Mth.clamp((int) level - (rank - 1) * 5, 1, 5);
        String realmName = new String[]{"一", "二", "三", "四", "五", "六", "七", "八", "九", "十"}[rank - 1]
                + "阶" + new String[]{"初级", "低级", "中级", "高级", "圆满"}[step - 1];
        double expMax = EntityAttributes.experienceMax(mob);
        if (experience && level <= 40 && expMax < 1) {
            expMax = 100;
            for (int n = 1; n <= (int) level; n++) expMax += n * n * 10 + 10;
        }
        double healthMax = Math.ceil(120 + rating(ratings[0], 14, 13, 12, 11, 10) * Math.pow(level / 3, 2));
        double healthRec = Math.ceil(1 + Math.pow(level / rating(ratings[1], 8.4, 8.8, 9.2, 9.6, 10), 2));
        double powerMax = Math.ceil(120 + rating(ratings[2], 28, 26, 24, 22, 20) * Math.pow(level / 3, 2));
        double powerRec = Math.ceil(Math.pow(level / rating(ratings[3], 6, 6.5, 7, 7.5, 8), 2));
        double soulMax = Math.ceil(120 + rating(ratings[4], 28, 26, 24, 22, 20) * Math.pow(level / 3, 2));
        double soulRec = Math.ceil(Math.pow(level / rating(ratings[5], 6, 6.5, 7, 7.5, 8), 2));
        double speed = rating(ratings[12], .36, .32, .28, .24, .2) + level * .004;
        double finalExpMax = expMax;
        EntityAttributes.update(mob, current -> {
            EntityStats updated = current.withLevel(level)
                    .withRealmName(realmName)
                    .withExperienceMax(finalExpMax)
                    .withStat(Stat.HEALTH_MAX, healthMax)
                    .withStat(Stat.HEALTH_REC, healthRec)
                    .withStat(Stat.POWER_MAX, powerMax)
                    .withStat(Stat.POWER_REC, powerRec)
                    .withStat(Stat.SOUL_MAX, soulMax)
                    .withStat(Stat.SOUL_REC, soulRec)
                    .withStat(Stat.MOVE_SPEED, speed);
            for (int i = 0; i < STATS.size(); i++) {
                updated = updated.withRating(STATS.get(i), ratings[i]);
            }
            for (int i = 0; i < COMBAT_STATS.size(); i++) {
                double statValue = Math.ceil(2 + Math.pow(level / rating(ratings[6 + i], 2.6, 2.7, 2.8, 2.9, 3), 2));
                updated = updated.withStat(COMBAT_STATS.get(i), statValue);
            }
            return updated;
        });
        mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(healthMax);
        mob.setHealth((float) healthMax);
        mob.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(EntityAttributes.stat(mob, Stat.ATTACK));
        mob.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(speed);
        if (personality) initializeCharacter(CreatureData.state(mob));
        String team = mob.getType().builtInRegistryHolder().key().location().toString().replace(':', '_');
        Relations.setTeam(mob, team);
        Relations.addFriendlyTeam(mob, team);
    }

    private static double rating(String rating, double excellent, double strong, double average, double weak, double poor) {
        return switch (rating) {
            case "绝佳" -> excellent;
            case "强劲" -> strong;
            case "平庸" -> average;
            case "勉强" -> weak;
            default -> poor;
        };
    }

    static void initializeCharacter(CreatureState state) {
        Random random = new Random();
        String character;
        if (random.nextDouble() < .2) character = "慵懒";
        else if (random.nextDouble() < .25) character = "谨慎";
        else if (random.nextDouble() < .333) character = "急躁";
        else if (random.nextDouble() < .5) character = "狡诈";
        else character = "平和";
        state.setCharacter(character);
        double[] values = switch (character) {
            case "慵懒" -> new double[]{.5, .5, 2, .5};
            case "谨慎" -> new double[]{1.5, .5, 1.5, 1.3};
            case "急躁" -> new double[]{.2, 1.8, .5, 2};
            case "狡诈" -> new double[]{1, 1, .7, 1.6};
            default -> new double[]{1, 1, 1, 1};
        };
        state.setCharacterDef(values[0]);
        state.setCharacterAtt(values[1]);
        state.setCharacterInterval(values[2]);
        state.setCharacterResist(values[3]);
    }
}
