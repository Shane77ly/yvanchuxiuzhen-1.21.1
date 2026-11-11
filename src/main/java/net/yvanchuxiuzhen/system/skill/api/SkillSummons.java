package net.yvanchuxiuzhen.system.skill.api;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.skill.entity.SkillEntities;
import net.yvanchuxiuzhen.system.skill.entity.SkillEntity;
import net.yvanchuxiuzhen.system.skill.SkillSetup;
import net.yvanchuxiuzhen.system.skill.projectile.SkillProjectile;

/** 来源技能实体的受控生成入口。 */
public final class SkillSummons {
    private SkillSummons() { }

    /** 生成并初始化毒蔓鞭，位置和朝向沿用施放者视线，团队归属从施放者复制。 */
    public static void summonVineWhip(ServerLevel level, Entity owner, Vec3 position) {
        SkillEntity vine = SkillEntities.summon(level, owner, "skill_tree_06", "木", 4, 1, "毒蔓鞭");
        vine.setAnimation("animation.skillTree06.skill01");
        SkillEntities.place(level, vine, position, owner, 0);
        SkillEntities.spawn(level, vine, true);
    }

    /** 妖兽来源水球发射入口：保留基础伤害、击退、静音、速度和散布。 */
    public static void shootWater03(Entity owner, Vec3 position, Vec3 direction) {
        ServerLevel level = (ServerLevel) owner.level();
        SkillProjectile projectile = SkillSetup.<SkillProjectile>entityType("projectile_skill_water_03").get()
                .create(level).launchedWith(1, 0);
        projectile.setOwner(owner);
        projectile.setBaseDamage(0.1);
        projectile.setSilent(true);
        projectile.setPos(position);
        projectile.shoot(direction.x, direction.y, direction.z, 1, 10);
        level.addFreshEntity(projectile);
    }
}
