package net.yvanchuxiuzhen.system.npc.entity;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.attribute.api.EntityStats;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.skill.api.SkillDelays;

/** 修士来源十三项资质公式，保留不识别资质文本进入最低档的分支。 */
final class NpcStats {
    private NpcStats() { }

    static void calculate(Mob npc) {
        EntityStats current = EntityAttributes.get(npc);
        double level = current.level();
        String realmName = realmNameForLevel(level);
        Map<Stat, Double> values = new EnumMap<>(Stat.class);
        double healthMax = Math.ceil(100 + rating(current, Stat.HEALTH_MAX, 14, 13, 12, 11, 10) * Math.pow(level / 4, 2));
        values.put(Stat.HEALTH_MAX, healthMax);
        SkillDelays.after(20, () -> {
            double delayedHealth = EntityAttributes.stat(npc, Stat.HEALTH_MAX);
            npc.getAttribute(Attributes.MAX_HEALTH).setBaseValue(delayedHealth);
            npc.setHealth((float) delayedHealth);
        });
        values.put(Stat.HEALTH_REC, Math.ceil(1 + Math.pow(level / rating(current, Stat.HEALTH_REC, 8.4, 8.8, 9.2, 9.6, 10), 2)));
        values.put(Stat.POWER_MAX, Math.ceil(120 + rating(current, Stat.POWER_MAX, 28, 26, 24, 22, 20) * Math.pow(level / 3, 2)));
        values.put(Stat.POWER_REC, Math.ceil(Math.pow(level / rating(current, Stat.POWER_REC, 6, 6.5, 7, 7.5, 8), 2)));
        values.put(Stat.SOUL_MAX, Math.ceil(120 + rating(current, Stat.SOUL_MAX, 28, 26, 24, 22, 20) * Math.pow(level / 3, 2)));
        values.put(Stat.SOUL_REC, Math.ceil(Math.pow(level / rating(current, Stat.SOUL_REC, 6, 6.5, 7, 7.5, 8), 2)));
        double attack = Math.ceil(2 + Math.pow(level / rating(current, Stat.ATTACK, 3, 3.2, 3.4, 3.6, 3.8), 2));
        values.put(Stat.ATTACK, attack);
        npc.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(attack);
        for (Stat field : new Stat[]{Stat.ATTACK_DEF, Stat.MANA_ATT, Stat.MANA_DEF, Stat.SOUL_ATT, Stat.SOUL_DEF}) {
            values.put(field, Math.ceil(2 + Math.pow(level / rating(current, field, 3, 3.2, 3.4, 3.6, 3.8), 2)));
        }
        double speed = rating(current, Stat.MOVE_SPEED, .28, .26, .24, .22, .2) + level * .01;
        values.put(Stat.MOVE_SPEED, speed);
        npc.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(speed);
        EntityAttributes.set(npc, current.withRealmName(realmName).withValues(values));
    }

    private static double rating(EntityStats stats, Stat field, double excellent, double strong, double average, double weak, double poor) {
        return switch (stats.rating(field)) {
            case "绝佳" -> excellent;
            case "强劲" -> strong;
            case "平庸" -> average;
            case "勉强" -> weak;
            default -> poor;
        };
    }

    private static String realmNameForLevel(double level) {
        if (!(level >= 1)) return "凡人";
        if (level >= 50) return "化神圆满";
        int value = (int) level;
        int realm = (value - 1) / 10;
        int step = value - realm * 10;
        String[] realms = {"练气", "筑基", "结丹", "元婴", "化神"};
        String[] units = {"层", "阶", "转", "重", "级"};
        String[] numbers = {"一", "二", "三", "四", "五", "六", "七", "八", "九"};
        return realms[realm] + (step == 10 ? "圆满" : numbers[step - 1] + units[realm]);
    }
}
