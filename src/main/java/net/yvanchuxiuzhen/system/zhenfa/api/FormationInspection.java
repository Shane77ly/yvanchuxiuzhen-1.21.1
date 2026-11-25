package net.yvanchuxiuzhen.system.zhenfa.api;

import java.text.DecimalFormat;
import java.util.Comparator;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.attribute.api.EntityStats;
import net.yvanchuxiuzhen.system.attribute.api.PlayerAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.gongfa.api.TechniqueBook;
import net.yvanchuxiuzhen.system.gongfa.api.Techniques;
import net.yvanchuxiuzhen.system.skill.api.SkillBook;
import net.yvanchuxiuzhen.system.skill.api.SkillKind;
import net.yvanchuxiuzhen.system.skill.api.Skills;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationText;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/** 阵法检测反馈操作；结果发送给参数坐标附近的玩家。 */
public final class FormationInspection {
    private FormationInspection() { }

    public static void report(ServerLevel level, Entity inspected, double rank, Vec3 center) {
        for (Entity entity : level.getEntitiesOfClass(Entity.class, new AABB(center, center).inflate(0.5)).stream()
                .sorted(Comparator.comparingDouble(candidate -> candidate.distanceToSqr(center))).toList()) {
            if (!(entity instanceof Player recipient)) continue;
            if (rank >= 1) recipient.displayClientMessage(Component.literal(inspected.getDisplayName().getString()), false);
            EntityStats entityStats = inspected instanceof Player ? null : EntityAttributes.get(inspected);
            if (rank >= 2) {
                String name;
                double experience;
                double maximum;
                if (inspected instanceof Player player) {
                    name = Cultivation.get(player, CultivationText.LEVEL_NAME);
                    experience = Cultivation.get(player, CultivationValue.EXPERIENCE);
                    maximum = Cultivation.get(player, CultivationValue.EXPERIENCE_MAX);
                } else {
                    name = entityStats.realmName();
                    experience = entityStats.experience();
                    maximum = entityStats.experienceMax();
                }
                recipient.displayClientMessage(Component.literal(name + "（" + experience + "/" + maximum + "）"), false);
            } else {
                String name = inspected instanceof Player player
                        ? Cultivation.get(player, CultivationText.LEVEL_NAME)
                        : entityStats.realmName();
                recipient.displayClientMessage(Component.literal(name), false);
            }
            if (inspected instanceof Player player) {
                reportPlayer(recipient, player, rank);
            } else {
                if (rank >= 4) send(recipient, "血量上限：" + ((LivingEntity) inspected).getAttribute(Attributes.MAX_HEALTH).getBaseValue());
                if (rank >= 5) send(recipient, "物理攻击：" + ((LivingEntity) inspected).getAttribute(Attributes.ATTACK_DAMAGE).getBaseValue());
                if (rank >= 5) send(recipient, "法术攻击：" + entityStats.stat(Stat.MANA_ATT));
                if (rank >= 6) send(recipient, "精神攻击：" + entityStats.stat(Stat.SOUL_ATT));
            }
        }
    }

    private static void reportPlayer(Player recipient, Player inspected, double rank) {
        if (rank >= 4) send(recipient, "血量上限：" + PlayerAttributes.baseValue(inspected, Stat.HEALTH_MAX));
        if (rank >= 5) send(recipient, "物理攻击：" + PlayerAttributes.baseValue(inspected, Stat.ATTACK));
        if (rank >= 5) send(recipient, "法术攻击：" + PlayerAttributes.baseValue(inspected, Stat.MANA_ATT));
        if (rank >= 6) send(recipient, "精神攻击：" + PlayerAttributes.baseValue(inspected, Stat.SOUL_ATT));
        if (rank >= 4) reportSigned(recipient, Cultivation.get(inspected, CultivationValue.VIGOROUS));
        if (rank >= 6) reportSigned(recipient, Cultivation.get(inspected, CultivationValue.MERITS));
        if (rank >= 3) {
            TechniqueBook book = Techniques.main(inspected).orElse(null);
            send(recipient, "功法：" + (book == null ? "" : book.definition().name()) + "（"
                    + (book == null ? "" : book.definition().gradeText()) + "）");
            send(recipient, "已修炼至" + new DecimalFormat("0").format(book == null ? 0 : book.level()) + "重天");
        } else if (rank >= 2) {
            String name = Skills.book(Techniques.slot(inspected)).filter(book -> book.kind() == SkillKind.SPELL)
                    .map(SkillBook::name).orElse("");
            send(recipient, "功法：" + name);
        }
        if (rank >= 7) send(recipient, "吸纳天地灵气转换为自身修为的效率：" + Cultivation.get(inspected, CultivationValue.EXPERIENCE_RATE));
        if (rank >= 8) send(recipient, "晋升的难度：" + Cultivation.get(inspected, CultivationValue.EXPERIENCE_HINDER));
    }

    private static void reportSigned(Player recipient, double amount) {
        if (amount > 0) send(recipient, "罡气：" + amount);
        else if (amount < 0) send(recipient, "煞气：" + amount);
    }

    private static void send(Player recipient, String text) {
        recipient.displayClientMessage(Component.literal(text), false);
    }
}
