package net.yvanchuxiuzhen.system.skill.entity;

import net.minecraft.world.damagesource.DamageSource;

/**
 * 一种技能动画实体的行为，对应来源实体类调用的过程：每刻（{@code ...DangShiTiGengXinKeShi}）、初始生成
 * （{@code ...ShiTiChuShiShengChengShi}）、被杀死（{@code ...DangShiTiBeiShaSiShi}）、受伤（{@code ...DangShiTiShouShangShi}）。
 * 全部只在服务端调用。
 */
public interface EntitySkill {
    default void tick(SkillEntity self) {
    }

    default void spawned(SkillEntity self) {
    }

    default void died(SkillEntity self, DamageSource source) {
    }

    default void hurt(SkillEntity self, DamageSource source, float amount) {
    }
}
