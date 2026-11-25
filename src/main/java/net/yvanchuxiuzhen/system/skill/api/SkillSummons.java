package net.yvanchuxiuzhen.system.skill.api;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.skill.entity.SkillEntities;
import net.yvanchuxiuzhen.system.skill.entity.SkillEntity;
import net.yvanchuxiuzhen.system.skill.SkillSetup;
import net.yvanchuxiuzhen.system.skill.projectile.ProjectileSupport;
import net.yvanchuxiuzhen.system.skill.projectile.SkillProjectile;
import net.yvanchuxiuzhen.system.relation.api.Relations;

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
        shootWater03(owner, position, direction, 1, 10);
    }

    /** 保留不同妖兽来源发射过程指定的固定击退与散布。 */
    public static void shootWater03(Entity owner, Vec3 position, Vec3 direction, int knockback, float inaccuracy) {
        ServerLevel level = (ServerLevel) owner.level();
        SkillProjectile projectile = SkillSetup.<SkillProjectile>entityType("projectile_skill_water_03").get()
                .create(level).launchedWith(knockback, 0);
        projectile.setOwner(owner);
        projectile.setBaseDamage(0.1);
        projectile.setSilent(true);
        projectile.setPos(position);
        projectile.shoot(direction.x, direction.y, direction.z, 1, inaccuracy);
        level.addFreshEntity(projectile);
    }

    /** 妖兽023回旋光弹：按来源光弹类型、伤害、击退、速度与散布生成。 */
    public static void shootLight01(Entity owner, Vec3 position, Vec3 direction) {
        ServerLevel level = (ServerLevel) owner.level();
        SkillProjectile projectile = SkillSetup.<SkillProjectile>entityType("projectile_skill_light_01").get()
                .create(level).launchedWith(1, 0);
        projectile.setOwner(owner);
        projectile.setBaseDamage(0.1);
        projectile.setSilent(true);
        projectile.setPos(position);
        projectile.shoot(direction.x, direction.y, direction.z, 1, 0);
        level.addFreshEntity(projectile);
    }

    /** 妖兽054冰锥：保留基础伤害、击退、静音及速度1、散布10。 */
    public static void shootIce01(Entity owner, Vec3 position, Vec3 direction) {
        ServerLevel level = (ServerLevel) owner.level();
        SkillProjectile projectile = SkillSetup.<SkillProjectile>entityType("projectile_skill_ice_01").get()
                .create(level).launchedWith(1, 0);
        projectile.setOwner(owner);
        projectile.setBaseDamage(0.1);
        projectile.setSilent(true);
        projectile.setPos(position);
        projectile.shoot(direction.x, direction.y, direction.z, 1, 10);
        level.addFreshEntity(projectile);
    }

    /** 首领火球术在眼部下方0.1格发射，基础伤害0、击退1、静音、速度1且无散布。 */
    public static void shootBossFireball(ServerLevel level, Entity owner) {
        Vec3 origin = new Vec3(owner.getX(), owner.getEyeY() - 0.1, owner.getZ());
        ProjectileSupport.shoot(level, owner, "projectile_skill_fire_01", origin, owner.getLookAngle(), 0, 1, 0, 1, 0)
                .setSilent(true);
    }

    /**
     * 妖兽054冰爪的裂冰斩入口：使用关系系统共享施放者阵营并写入冰刺伤害参数。
     */
    public static void summonEntityIceSpike(Entity owner, Vec3 position) {
        summonEntityIceSpike(owner, position, 0, 1);
    }

    /** 以指定攻击量（同时作为后续冰刺剩余根数）与倍率召唤裂冰斩。 */
    public static void summonEntityIceSpike(Entity owner, Vec3 position, int attackDamage, int multiplier) {
        ServerLevel level = (ServerLevel) owner.level();
        SkillEntity spike = SkillSetup.<SkillEntity>entityType("skill_ice_05").get().create(level);
        spike.configure(owner, 5, 1.2, "冰", "裂冰斩");
        spike.moveTo(position.x, position.y, position.z, level.getRandom().nextFloat() * 360F, 0);
        SkillEntities.turn(spike, Mth.nextInt(level.getRandom(), 1, 360), 0);
        Relations.initializeIceCast(owner, spike, attackDamage, multiplier);
        Relations.setIceCastDirection(spike, (int) owner.getYRot());
        SkillEntities.spawn(level, spike, true);
    }
}
