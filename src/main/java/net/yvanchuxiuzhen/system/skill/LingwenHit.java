package net.yvanchuxiuzhen.system.skill;

import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.combat.api.Enchants;
import net.yvanchuxiuzhen.system.combat.api.HitEvent;
import net.yvanchuxiuzhen.system.relation.api.Relations;
import net.yvanchuxiuzhen.system.skill.entity.SkillEntities;
import net.yvanchuxiuzhen.system.skill.entity.SkillEntity;
import net.yvanchuxiuzhen.system.skill.projectile.ProjectileSupport;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/**
 * 灵纹附魔的命中效果（来源 {@code EntityHurt2} 中读主手灵纹附魔的一段，combat 发出 {@link HitEvent}）：
 * 攻击者周围 2 格内已有灵纹特效时，逐一检查附近实体的共享表并增加计数；没有时生成特效并增加共享计数。
 * 来源用起尸诀附魔 {@code qishi} 的等级作为 {@code fumolevel}（写成灵纹附魔应是笔误，保持来源行为），
 * 并在受击者的灵力不少于 {@code fumolevel × 5} 时从<b>受击者</b>身上扣除这些灵力（玩家受击者才有灵力，非玩家视为 0）。
 * 灵纹计数与主人标识存放于共享好友子表；重读存档后各载体独立。
 */
final class LingwenHit {
    private LingwenHit() {
    }

    static void onHit(HitEvent event) {
        if (!(event.attacker() instanceof LivingEntity attacker) || !(attacker.level() instanceof ServerLevel level)
                || Enchants.mainHand(attacker, Enchants.key("lingwen")) == 0) {
            return;
        }
        int fumolevel = Enchants.mainHand(attacker, Enchants.key("qishi"));
        double cost = fumolevel * 5.0;
        if (event.victim() instanceof ServerPlayer victim) {
            if (Cultivation.get(victim, CultivationValue.POWER) < cost) {
                return;
            }
            Cultivation.add(victim, CultivationValue.POWER, -cost);
        } else if (cost > 0) {
            return;
        }
        Vec3 at = attacker.position();
        List<SkillEntity> nearby = level.getEntitiesOfClass(SkillEntity.class, new AABB(at, at).inflate(2), entity -> SkillEntities.is(entity, "lingwentexiao_2"));
        if (!nearby.isEmpty()) {
            for (Entity other : ProjectileSupport.around(level, at, 2)) {
                Relations.incrementRuneForOwner(other, attacker);
            }
            return;
        }
        SkillEntity rune = SkillEntities.summon(level, attacker, "lingwentexiao_2", "", 0, 0, "灵纹");
        rune.moveTo(at.x, at.y, at.z, level.getRandom().nextFloat() * 360F, 0);
        Relations.initializeRune(attacker, rune, fumolevel);
        SkillEntities.spawn(level, rune, true);
    }
}
