package net.yvanchuxiuzhen.system.skill.projectile;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 一种技能弹体的行为，对应箭类实体的三个过程：每刻（{@code ...TouZhiWuFeiXingTick}）、命中实体
 * （{@code ...DangTouZhiWuJiZhongShiTiShi}）、命中方块（{@code ...DangTouSheWuSheZhongCiFangKuai}）。
 * 三个钩子只在服务端调用。飞行与实体命中要求发射者；方块命中的发射者条件由具体过程决定。
 */
public interface ProjectileSkill {
    default void tick(SkillProjectile projectile, Entity owner) {
    }

    default void hitEntity(SkillProjectile projectile, Entity target, Entity owner) {
    }

    default void hitBlock(SkillProjectile projectile, BlockHitResult hit) {
    }
}
