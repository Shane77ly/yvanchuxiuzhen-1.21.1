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
 * 20 刻后消失；飞行时发送浅蓝消散粒子，命中非友方时发送浅蓝爆发粒子，并对生物造成无属性固定伤害（攻击量取整，伤害来源为发射者）。
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
        ProjectileSupport.burst(projectile, "disappearskyblue");
    }

    @Override
    public void hitEntity(SkillProjectile projectile, Entity target, Entity owner) {
        if (!ProjectileSupport.friendly(owner, target)) {
            ProjectileSupport.burst(projectile, "smallexplosionskyblue");
            if (target instanceof LivingEntity living) {
                SpellDamage.dealRounded((ServerLevel) projectile.level(), living, DamageCategory.NONE, owner.getUUID(), true,
                        projectile.getPersistentData().getDouble("attacknumber"), 1);
            }
        }
    }
}
