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
import net.yvanchuxiuzhen.system.skill.entity.SkillEntities;
import net.yvanchuxiuzhen.system.skill.entity.SkillEntity;
import net.yvanchuxiuzhen.system.skill.projectile.ProjectileSupport;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/**
 * 灵纹附魔的命中效果（来源 {@code EntityHurt2} 中读主手灵纹附魔的一段，combat 发出 {@link HitEvent}）：
 * 攻击者周围 2 格内已有灵纹特效时，属于自己且不足 6 枚的各加一枚；没有时在攻击者处生成一个，数量 1。
 * 来源用起尸诀附魔 {@code qishi} 的等级作为 {@code fumolevel}（写成灵纹附魔应是笔误，保持来源行为），
 * 并在受击者的灵力不少于 {@code fumolevel × 5} 时从<b>受击者</b>身上扣除这些灵力（玩家受击者才有灵力，非玩家视为 0）。
 * 来源的灵纹计数写进攻击者好友列表物品的 NBT（同一个标签对象），会在好友列表里残留，下一只从残留值接着数；这里每只从 1 起。
 */
final class LingwenHit {
    private static final int LIMIT = 6;

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
                if (other instanceof SkillEntity rune && SkillEntities.is(rune, "lingwentexiao_2") && attacker.getUUID().equals(rune.attacker())
                        && rune.getPersistentData().getInt("lingwennum") < LIMIT) {
                    rune.getPersistentData().putInt("lingwennum", rune.getPersistentData().getInt("lingwennum") + 1);
                }
            }
            return;
        }
        SkillEntity rune = SkillEntities.summon(level, attacker, "lingwentexiao_2", "", 0, 0, "灵纹");
        rune.moveTo(at.x, at.y, at.z, level.getRandom().nextFloat() * 360F, 0);
        rune.getPersistentData().putInt("friendnumber", fumolevel);
        rune.getPersistentData().putInt("lingwennum", 1);
        SkillEntities.spawn(level, rune, true);
    }
}
