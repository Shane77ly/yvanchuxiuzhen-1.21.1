package net.yvanchuxiuzhen.system.skill.projectile;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 一种技能弹体的行为，对应箭类实体的三个过程：每刻（{@code ...TouZhiWuFeiXingTick}）、命中实体
 * （{@code ...DangTouZhiWuJiZhongShiTiShi}）、命中方块（{@code ...DangTouSheWuSheZhongCiFangKuai}）。
 * 三个钩子只在服务端且弹体有发射者时调用，与来源“发射者为空则过程直接返回”一致。
 */
public interface ProjectileSkill {
    default void tick(SkillProjectile projectile, Entity owner) {
    }

    default void hitEntity(SkillProjectile projectile, Entity target, Entity owner) {
    }

    default void hitBlock(SkillProjectile projectile, BlockHitResult hit, Entity owner) {
    }
}
