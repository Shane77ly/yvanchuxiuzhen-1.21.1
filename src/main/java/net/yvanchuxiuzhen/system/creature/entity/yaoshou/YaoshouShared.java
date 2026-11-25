package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import java.util.List;
import java.util.Random;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.attribute.api.EntityStats;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.creature.data.CreatureData;
import net.yvanchuxiuzhen.system.creature.data.CreatureState;
import net.yvanchuxiuzhen.system.relation.api.Relations;

/** 妖兽来源共用等级、资质、尺寸、性格与团队真值。 */
public final class YaoshouShared {
    private static final List<Stat> STATS = List.of(
            Stat.HEALTH_MAX, Stat.HEALTH_REC, Stat.POWER_MAX, Stat.POWER_REC, Stat.SOUL_MAX, Stat.SOUL_REC,
            Stat.ATTACK, Stat.ATTACK_DEF, Stat.MANA_ATT, Stat.MANA_DEF, Stat.SOUL_ATT, Stat.SOUL_DEF, Stat.MOVE_SPEED);
    private static final List<Stat> COMBAT_STATS = List.of(
            Stat.ATTACK, Stat.ATTACK_DEF, Stat.MANA_ATT, Stat.MANA_DEF, Stat.SOUL_ATT, Stat.SOUL_DEF);

    private YaoshouShared() { }

    public static boolean initialize(YaoshouEntity entity, int defaultLevel, String... ratings) {
        return initialize(entity, defaultLevel, true, ratings);
    }

    public static boolean initializeWithoutExperience(YaoshouEntity entity, int defaultLevel, String... ratings) {
        return initialize(entity, defaultLevel, false, ratings);
    }

    private static boolean initialize(YaoshouEntity entity, int defaultLevel, boolean experience, String... ratings) {
        if (!EntityAttributes.realmName(entity).isEmpty()) return false;
        double level = EntityAttributes.level(entity) < 1 ? defaultLevel : EntityAttributes.level(entity);
        int sizeBase = entity instanceof Yaoshou040Entity ? new Random().nextInt(101) + 300 : new Random().nextInt(31) + 70;
        entity.getAttribute(YaoshouAttributes.SIZE).setBaseValue(sizeBase * 0.01 + (level / 4) * 0.1);
        double maxExp = EntityAttributes.experienceMax(entity);
        if (experience && level <= 40 && maxExp < 1) {
            maxExp = 100;
            for (int n = 1; n <= (int) level; n++) maxExp += n * n * 10 + 10;
        }
        double finalMaxExp = maxExp;
        String realmName = formatLevelName(level);
        EntityAttributes.update(entity, current -> {
            EntityStats updated = current.withLevel(level).withRealmName(realmName).withExperienceMax(finalMaxExp);
            for (int i = 0; i < STATS.size(); i++) {
                updated = updated.withRating(STATS.get(i), ratings[i]);
            }
            return updated;
        });
        calculateAttributes(entity, level);
        initializeCharacter(entity, CreatureData.state(entity));
        String team = entity.getType().builtInRegistryHolder().key().location().toString().replace(':', '_');
        Relations.setTeam(entity, team);
        Relations.addFriendlyTeam(entity, team);
        return true;
    }

    public static void calculateAttributes(LivingEntity entity, double level) {
        calculateAttributes(entity, level, false);
    }

    /** 来源升级过程与出生计算共用数值公式，但使用独立的五档移速基值。 */
    public static void calculateUpgradeAttributes(Entity entity, double level) {
        calculateAttributes(entity, level, true);
    }

    private static void calculateAttributes(Entity entity, double level, boolean upgrade) {
        EntityStats current = EntityAttributes.get(entity);
        double healthMax = Math.ceil(120 + maxRating(current, Stat.HEALTH_MAX, 14, 13, 12, 11) * Math.pow(level / 3, 2));
        LivingEntity living = (LivingEntity) entity;
        living.getAttribute(Attributes.MAX_HEALTH).setBaseValue(healthMax);
        living.setHealth((float) healthMax);
        double healthRec = Math.ceil(1 + Math.pow(level / recoveryRating(current, Stat.HEALTH_REC, 8.4, 8.8, 9.2, 9.6), 2));
        double powerMax = Math.ceil(120 + maxRating(current, Stat.POWER_MAX, 28, 26, 24, 22) * Math.pow(level / 3, 2));
        double powerRec = Math.ceil(Math.pow(level / recoveryRating(current, Stat.POWER_REC, 6, 6.5, 7, 7.5), 2));
        double soulMax = Math.ceil(120 + maxRating(current, Stat.SOUL_MAX, 28, 26, 24, 22) * Math.pow(level / 3, 2));
        double soulRec = Math.ceil(Math.pow(level / recoveryRating(current, Stat.SOUL_REC, 6, 6.5, 7, 7.5), 2));
        double speed = switch (current.rating(Stat.MOVE_SPEED)) {
            case "绝佳" -> upgrade ? 0.28 : 0.36;
            case "强劲" -> upgrade ? 0.26 : 0.32;
            case "平庸" -> upgrade ? 0.24 : 0.28;
            case "勉强" -> upgrade ? 0.22 : 0.24;
            default -> 0.2;
        } + level * 0.004;
        EntityAttributes.update(entity, stats -> {
            EntityStats updated = stats
                    .withStat(Stat.HEALTH_MAX, healthMax)
                    .withStat(Stat.HEALTH_REC, healthRec)
                    .withStat(Stat.POWER_MAX, powerMax)
                    .withStat(Stat.POWER_REC, powerRec)
                    .withStat(Stat.SOUL_MAX, soulMax)
                    .withStat(Stat.SOUL_REC, soulRec)
                    .withStat(Stat.MOVE_SPEED, speed);
            for (Stat stat : COMBAT_STATS) {
                double value = Math.ceil(2 + Math.pow(level / maxRating(current, stat, 2.6, 2.7, 2.8, 2.9), 2));
                updated = updated.withStat(stat, value);
            }
            return updated;
        });
        living.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(EntityAttributes.stat(entity, Stat.ATTACK));
        living.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(speed);
    }

    public static void updateLevelName(Entity entity, double level) {
        EntityAttributes.setRealmName(entity, formatLevelName(level));
    }

    private static String formatLevelName(double level) {
        int rank = Mth.clamp(((int) level - 1) / 5 + 1, 1, 10);
        int step = Mth.clamp((int) level - (rank - 1) * 5, 1, 5);
        return new String[]{"一", "二", "三", "四", "五", "六", "七", "八", "九", "十"}[rank - 1]
                + "阶" + new String[]{"初级", "低级", "中级", "高级", "圆满"}[step - 1];
    }

    public static void initializeCharacter(Mob entity, CreatureState state) {
        String character;
        if (Math.random() < 0.2) character = "慵懒";
        else if (Math.random() < 0.25) character = "谨慎";
        else if (Math.random() < 0.333) character = "急躁";
        else if (Math.random() < 0.5) character = "狡诈";
        else character = "平和";
        state.setCharacter(character);
        switch (character) {
            case "慵懒" -> setCharacter(state, 0.5, 0.5, 2, 0.5);
            case "谨慎" -> setCharacter(state, 1.5, 0.5, 1.5, 1.3);
            case "急躁" -> setCharacter(state, 0.2, 1.8, 0.5, 2);
            case "狡诈" -> setCharacter(state, 1, 1, 0.7, 1.6);
            default -> setCharacter(state, 1, 1, 1, 1);
        }
    }

    public static boolean isFriendly(Entity judge, Entity other) {
        return Relations.isFriendly(judge, other);
    }

    public static void finishSkill(Entity entity) {
        CreatureState state = CreatureData.state(entity);
        state.setSkill("");
        state.setPhase("");
        state.setTick(0);
        state.setTick2(0);
    }

    public static void escape(Entity entity) {
        CreatureState state = CreatureData.state(entity);
        String phase = state.phase();
        if (phase.isEmpty()) {
            state.setPhase("准备");
        } else if (phase.equals("准备") && state.tick() >= 20) {
            entity.stopRiding();
            entity.setDeltaMovement(entity.getLookAngle().x * 0.5, 0.7, entity.getLookAngle().z * 0.5);
            state.setPhase("跳跃");
        } else if (phase.equals("跳跃") && state.tick() >= 30) {
            finishSkill(entity);
        }
    }

    private static double maxRating(EntityStats stats, Stat stat, double excellent, double strong, double average, double weak) {
        return switch (stats.rating(stat)) {
            case "绝佳" -> excellent;
            case "强劲" -> strong;
            case "平庸" -> average;
            case "勉强" -> weak;
            default -> stat == Stat.HEALTH_MAX ? 10 : stat == Stat.POWER_MAX || stat == Stat.SOUL_MAX ? 20 : 3;
        };
    }

    private static double recoveryRating(EntityStats stats, Stat stat, double excellent, double strong, double average, double weak) {
        return switch (stats.rating(stat)) {
            case "绝佳" -> excellent;
            case "强劲" -> strong;
            case "平庸" -> average;
            case "勉强" -> weak;
            default -> stat == Stat.HEALTH_REC ? 10 : 8;
        };
    }

    private static void setCharacter(CreatureState state, double defense, double attack, double interval, double resist) {
        state.setCharacterDef(defense);
        state.setCharacterAtt(attack);
        state.setCharacterInterval(interval);
        state.setCharacterResist(resist);
    }
}
