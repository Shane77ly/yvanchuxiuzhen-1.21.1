package net.yvanchuxiuzhen.system.skill.entity;

import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.yvanchuxiuzhen.system.combat.api.DamageCategory;
import net.yvanchuxiuzhen.system.combat.api.SpellDamage;

/**
 * 技能实体记下的施放信息：攻击者、攻击量、伤害倍率、伤害类别标签与技能名（来源放在 {@code getPersistentData} 的
 * {@code AttackUUID}、{@code AttackDamage}、{@code Damage_multiplier}、{@code damagetype}、{@code jinengName}）。
 * {@link SkillEntity} 与 {@link TamedSkillEntity} 各持有一份，随实体存档。
 */
public final class SkillCast {
    private UUID attacker;
    private double attackDamage;
    private double multiplier;
    private String damageType = "";
    private String skillName = "";

    public void configure(UUID attacker, double attackDamage, double multiplier, String damageType, String skillName) {
        this.attacker = attacker;
        this.attackDamage = attackDamage;
        this.multiplier = multiplier;
        this.damageType = damageType;
        this.skillName = skillName;
    }

    /** 攻击者；未设置时为空。 */
    public UUID attacker() {
        return attacker;
    }

    public double attackDamage() {
        return attackDamage;
    }

    public double multiplier() {
        return multiplier;
    }

    public String damageType() {
        return damageType;
    }

    public String skillName() {
        return skillName;
    }

    /** 对 {@code other} 造成记下的类别、攻击量与倍率的灵力伤害；{@code other} 不是生物时没有效果。 */
    public void strike(ServerLevel level, Entity other) {
        if (other instanceof LivingEntity living) {
            SpellDamage.deal(level, living, DamageCategory.byLabel(damageType), attacker, false, attackDamage, multiplier);
        }
    }

    /** 同 {@link #strike}，攻击量与倍率先取整（来源经 {@code DecimalFormat("0")} 拼进命令的调用）。 */
    public void strikeRounded(ServerLevel level, Entity other) {
        if (other instanceof LivingEntity living) {
            SpellDamage.dealRounded(level, living, DamageCategory.byLabel(damageType), attacker, false, attackDamage, multiplier);
        }
    }

    /** 攻击者实体；已离线或不在本维度时为空。 */
    public Entity attackerEntity(ServerLevel level) {
        return attacker == null ? null : level.getEntity(attacker);
    }

    public void save(CompoundTag tag) {
        tag.putDouble("attack_damage", attackDamage);
        tag.putDouble("multiplier", multiplier);
        tag.putString("damage_type", damageType);
        tag.putString("skill_name", skillName);
        if (attacker != null) {
            tag.putUUID("attacker", attacker);
        }
    }

    public void load(CompoundTag tag) {
        attackDamage = tag.getDouble("attack_damage");
        multiplier = tag.getDouble("multiplier");
        damageType = tag.getString("damage_type");
        skillName = tag.getString("skill_name");
        attacker = tag.hasUUID("attacker") ? tag.getUUID("attacker") : null;
    }
}
