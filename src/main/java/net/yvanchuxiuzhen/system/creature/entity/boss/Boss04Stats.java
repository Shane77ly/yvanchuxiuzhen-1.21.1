package net.yvanchuxiuzhen.system.creature.entity.boss;

import java.util.List;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.attribute.api.EntityStats;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.skill.api.SkillDelays;

/** 04号首领采用来源修者等级与属性公式，生命值在二十刻后写入。 */
final class Boss04Stats {
    private static final List<Stat> STATS = List.of(
            Stat.HEALTH_MAX, Stat.HEALTH_REC, Stat.POWER_MAX, Stat.POWER_REC, Stat.SOUL_MAX, Stat.SOUL_REC,
            Stat.ATTACK, Stat.ATTACK_DEF, Stat.MANA_ATT, Stat.MANA_DEF, Stat.SOUL_ATT, Stat.SOUL_DEF, Stat.MOVE_SPEED);
    private static final String EXCELLENT = "绝佳";
    private static final String STRONG = "强劲";
    private static final String WEAK = "勉强";
    private static final String AVERAGE = "平庸";

    private Boss04Stats() { }

    static void initialize(Boss04Entity boss) {
        if (!EntityAttributes.realmName(boss).isEmpty()) return;
        double level = EntityAttributes.level(boss) < 1 ? 40 : EntityAttributes.level(boss);
        String[] ratings = {EXCELLENT, STRONG, WEAK, WEAK, WEAK, WEAK, EXCELLENT, STRONG, STRONG, AVERAGE, AVERAGE, WEAK, STRONG};
        double healthMax = Math.ceil(100 + 14 * Math.pow(level / 4, 2));
        double attack = Math.ceil(2 + Math.pow(level / 3, 2));
        double speed = .26 + level * .01;
        EntityAttributes.update(boss, current -> {
            EntityStats updated = current.withLevel(level)
                    .withRealmName(levelName(level))
                    .withStat(Stat.HEALTH_MAX, healthMax)
                    .withStat(Stat.HEALTH_REC, Math.ceil(1 + Math.pow(level / 8.8, 2)))
                    .withStat(Stat.POWER_MAX, Math.ceil(120 + 22 * Math.pow(level / 3, 2)))
                    .withStat(Stat.POWER_REC, Math.ceil(Math.pow(level / 7.5, 2)))
                    .withStat(Stat.SOUL_MAX, Math.ceil(120 + 22 * Math.pow(level / 3, 2)))
                    .withStat(Stat.SOUL_REC, Math.ceil(Math.pow(level / 7.5, 2)))
                    .withStat(Stat.ATTACK, attack)
                    .withStat(Stat.ATTACK_DEF, Math.ceil(2 + Math.pow(level / 3.2, 2)))
                    .withStat(Stat.MANA_ATT, Math.ceil(2 + Math.pow(level / 3.2, 2)))
                    .withStat(Stat.MANA_DEF, Math.ceil(2 + Math.pow(level / 3.4, 2)))
                    .withStat(Stat.SOUL_ATT, Math.ceil(2 + Math.pow(level / 3.4, 2)))
                    .withStat(Stat.SOUL_DEF, Math.ceil(2 + Math.pow(level / 3.6, 2)))
                    .withStat(Stat.MOVE_SPEED, speed);
            for (int i = 0; i < STATS.size(); i++) updated = updated.withRating(STATS.get(i), ratings[i]);
            return updated;
        });
        boss.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(attack);
        boss.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(speed);
        SkillDelays.after(20, () -> {
            double delayedMax = EntityAttributes.stat(boss, Stat.HEALTH_MAX);
            boss.getAttribute(Attributes.MAX_HEALTH).setBaseValue(delayedMax);
            boss.setHealth((float) delayedMax);
        });
        String team = boss.getType().builtInRegistryHolder().key().location().toString().replace(':', '_');
        Relations.setTeam(boss, team);
        Relations.addFriendlyTeam(boss, team);
    }

    private static String levelName(double level) {
        if (level >= 50) return "化神圆满";
        int value = (int) level;
        int realm = (value - 1) / 10;
        int step = value - realm * 10;
        String[] realms = {"练气", "筑基", "结丹", "元婴", "化神"};
        String[] units = {"层", "阶", "转", "重", "级"};
        String[] numbers = {"一", "二", "三", "四", "五", "六", "七", "八", "九"};
        String suffix = step == 10 ? "圆满" : numbers[step - 1] + units[realm];
        return realms[realm] + suffix;
    }
}
