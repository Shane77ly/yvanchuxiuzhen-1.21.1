package net.yvanchuxiuzhen.system.creature.entity.boss;

import java.util.List;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.attribute.api.EntityStats;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.skill.api.SkillDelays;

/** 首领01采用修者来源的独立资质公式、等级名与延迟生命值写入。 */
final class Boss01Stats {
    private static final List<Stat> STATS = List.of(
            Stat.HEALTH_MAX, Stat.HEALTH_REC, Stat.POWER_MAX, Stat.POWER_REC, Stat.SOUL_MAX, Stat.SOUL_REC,
            Stat.ATTACK, Stat.ATTACK_DEF, Stat.MANA_ATT, Stat.MANA_DEF, Stat.SOUL_ATT, Stat.SOUL_DEF, Stat.MOVE_SPEED);
    private static final String[] RATINGS = {"强劲", "平庸", "强劲", "勉强", "平庸", "勉强", "平庸", "平庸", "强劲", "平庸", "强劲", "强劲", "平庸"};

    private Boss01Stats() { }

    static void initialize(Boss01Entity boss) {
        if (!EntityAttributes.realmName(boss).isEmpty()) return;
        double level = EntityAttributes.level(boss) < 1 ? 15 : EntityAttributes.level(boss);
        double healthMax = Math.ceil(100 + 13 * Math.pow(level / 4, 2));
        double attack = Math.ceil(2 + Math.pow(level / 3.4, 2));
        double manaAttack = Math.ceil(2 + Math.pow(level / 3.2, 2));
        double speed = 0.24 + level * 0.01;
        EntityAttributes.update(boss, current -> {
            EntityStats updated = current.withLevel(level)
                    .withRealmName(levelName(level))
                    .withStat(Stat.HEALTH_MAX, healthMax)
                    .withStat(Stat.HEALTH_REC, Math.ceil(1 + Math.pow(level / 9.2, 2)))
                    .withStat(Stat.POWER_MAX, Math.ceil(120 + 26 * Math.pow(level / 3, 2)))
                    .withStat(Stat.POWER_REC, Math.ceil(Math.pow(level / 7.5, 2)))
                    .withStat(Stat.SOUL_MAX, Math.ceil(120 + 24 * Math.pow(level / 3, 2)))
                    .withStat(Stat.SOUL_REC, Math.ceil(Math.pow(level / 7.5, 2)))
                    .withStat(Stat.ATTACK, attack)
                    .withStat(Stat.ATTACK_DEF, attack)
                    .withStat(Stat.MANA_ATT, manaAttack)
                    .withStat(Stat.MANA_DEF, attack)
                    .withStat(Stat.SOUL_ATT, manaAttack)
                    .withStat(Stat.SOUL_DEF, manaAttack)
                    .withStat(Stat.MOVE_SPEED, speed);
            for (int i = 0; i < STATS.size(); i++) {
                updated = updated.withRating(STATS.get(i), RATINGS[i]);
            }
            return updated;
        });
        boss.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(attack);
        boss.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(speed);
        Relations.setTeam(boss, "yvanchuxiuzhen_ycxzboss_01");
        Relations.addFriendlyTeam(boss, "yvanchuxiuzhen_ycxzboss_01");
        SkillDelays.after(20, () -> {
            double delayedMax = EntityAttributes.stat(boss, Stat.HEALTH_MAX);
            boss.getAttribute(Attributes.MAX_HEALTH).setBaseValue(delayedMax);
            boss.setHealth((float) delayedMax);
        });
    }

    private static String levelName(double level) {
        int value = (int) level;
        if (value == 15) return "筑基五阶";
        if (value == 10) return "练气圆满";
        if (value == 20) return "筑基圆满";
        if (value == 30) return "结丹圆满";
        if (value == 40) return "元婴圆满";
        if (value >= 50) return "化神圆满";
        if (value >= 41) return "化神" + chinese(value - 40) + "级";
        if (value >= 31) return "元婴" + chinese(value - 30) + "重";
        if (value >= 21) return "结丹" + chinese(value - 20) + "转";
        if (value >= 11) return "筑基" + chinese(value - 10) + "阶";
        return "练气" + chinese(value) + "层";
    }

    private static String chinese(int number) {
        return switch (number) {
            case 1 -> "一"; case 2 -> "二"; case 3 -> "三"; case 4 -> "四"; case 5 -> "五";
            case 6 -> "六"; case 7 -> "七"; case 8 -> "八"; case 9 -> "九"; default -> "一";
        };
    }
}
