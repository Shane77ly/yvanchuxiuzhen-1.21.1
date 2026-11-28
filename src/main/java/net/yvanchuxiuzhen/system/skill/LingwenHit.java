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
 * 攻击者周围 2 格内已有灵纹特效时，逐一检查附近灵纹的共享归属并增加计数；没有时生成灵纹特效。
 * 已按阶段 4 裁定修正 Q45：数量与灵力消耗读取主手 {@code lingwen} 等级，并从<b>攻击者</b>扣减灵力。
 */
final class LingwenHit {
    private LingwenHit() {
    }

    static void onHit(HitEvent event) {
        if (!(event.attacker() instanceof LivingEntity attacker) || !(attacker.level() instanceof ServerLevel level)) {
            return;
        }
        int lingwen = Enchants.mainHand(attacker, Enchants.key("lingwen"));
        if (lingwen == 0) {
            return;
        }
        double cost = lingwen * 5.0;
        if (attacker instanceof ServerPlayer player) {
            if (Cultivation.get(player, CultivationValue.POWER) < cost) {
                return;
            }
            Cultivation.add(player, CultivationValue.POWER, -cost);
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
        Relations.initializeRune(attacker, rune, lingwen);
        SkillEntities.spawn(level, rune, true);
    }
}
