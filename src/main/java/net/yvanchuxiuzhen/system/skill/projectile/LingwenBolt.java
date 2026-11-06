package net.yvanchuxiuzhen.system.skill.projectile;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.yvanchuxiuzhen.system.combat.api.DamageCategory;
import net.yvanchuxiuzhen.system.combat.api.Enchants;
import net.yvanchuxiuzhen.system.combat.api.SpellDamage;

/**
 * 灵纹弹（来源 {@code Lingwentexiao} 的三个过程）：灵纹特效射出的弹体。第一刻按发射者主手武器的灵纹附魔等级记下攻击量（等级 × 4）并取消重力，
 * 20 刻后消失；命中非友方生物时造成无属性的固定伤害（攻击量取整，伤害来源为发射者）。来源在命中处和飞行中的自定义粒子
 * （{@code SMALLEXPLOSIONSKYBLUE}、{@code DISAPPEARSKYBLUE}）随 particle 迁移。
 */
final class LingwenBolt implements ProjectileSkill {
    private static final int LIFETIME = 20;

    @Override
    public void tick(SkillProjectile projectile, Entity owner) {
        CompoundTag data = projectile.getPersistentData();
        if (projectile.begin() && owner instanceof LivingEntity living) {
            data.putDouble("attacknumber", Enchants.mainHand(living, Enchants.key("lingwen")) * 4);
        }
        if (projectile.advance() >= LIFETIME) {
            projectile.discard();
        }
    }

    @Override
    public void hitEntity(SkillProjectile projectile, Entity target, Entity owner) {
        if (!ProjectileSupport.friendly(owner, target) && target instanceof LivingEntity living) {
            SpellDamage.dealRounded((ServerLevel) projectile.level(), living, DamageCategory.NONE, owner.getUUID(), true,
                    projectile.getPersistentData().getDouble("attacknumber"), 1);
        }
    }
}
