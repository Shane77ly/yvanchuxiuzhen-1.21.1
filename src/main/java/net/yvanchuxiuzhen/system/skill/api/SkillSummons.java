package net.yvanchuxiuzhen.system.skill.api;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.yvanchuxiuzhen.system.skill.entity.SkillEntities;
import net.yvanchuxiuzhen.system.skill.entity.SkillEntity;
import net.yvanchuxiuzhen.system.skill.SkillSetup;
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

    /**
     * 妖兽054冰爪的裂冰斩入口：来源消费者读取friendlist子标签中的整数参数；根标签的同名写入不参与结算。
     * 友方集合共享施放者引用；攻击者文字无效时不造成命令伤害，冻结和出土生命周期仍执行。
     */
    public static void summonEntityIceSpike(Entity owner, Vec3 position) {
        summonEntityIceSpike(owner, position, owner.getPersistentData().getCompound("friendlist"));
    }

    /** 来源需要局部改写friendlist参数时，复制传入的子标签但不写回施放者。 */
    public static void summonEntityIceSpike(Entity owner, Vec3 position, CompoundTag parameters) {
        ServerLevel level = (ServerLevel) owner.level();
        SkillEntity spike = SkillSetup.<SkillEntity>entityType("skill_ice_05").get().create(level);
        spike.configure(owner, 5, 1.2, "冰", "裂冰斩");
        Relations.shareEntityAllegiance(owner, spike);
        spike.moveTo(position.x, position.y, position.z, level.getRandom().nextFloat() * 360F, 0);
        SkillEntities.turn(spike, Mth.nextInt(level.getRandom(), 1, 360), 0);
        CompoundTag data = spike.getPersistentData();
        data.putDouble("jiaodu", owner.getYRot());
        data.put("friendlist", parameters);
        SkillEntities.spawn(level, spike, true);
    }
}
