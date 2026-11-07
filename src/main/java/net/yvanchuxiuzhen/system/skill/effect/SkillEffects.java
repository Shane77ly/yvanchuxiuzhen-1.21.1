package net.yvanchuxiuzhen.system.skill.effect;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.platform.content.EffectBehavior;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;

/**
 * 技能系统的状态效果行为。{@code skilleffect_01}–{@code 07} 在来源中没有自身过程，只作标记，不登记行为。
 * 修改属性基础值的效果成对生效：开始时加减，结束（到期、被移除、被刷新前）时还原。
 */
public final class SkillEffects {
    /** 霸体与禁施改的属性基础值幅度。 */
    private static final double SHIFT = 10;

    private SkillEffects() {
    }

    public static void register(ContentTables tables) {
        tables.effectBehavior("skilleffect_08", new Unyielding());
        tables.effectBehavior("skilleffect_09", new Casting());
        tables.effectFactory("skill_arrow_0601", InnerExplosion::new);
        InnerExplosion.install();
    }

    /** 霸体：击退抗性基础值 +10，并附加与本效果同时长的发光（无粒子）；结束时还原。 */
    private static final class Unyielding implements EffectBehavior {
        @Override
        public void started(LivingEntity entity, int amplifier) {
            entity.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(entity.getAttribute(Attributes.KNOCKBACK_RESISTANCE).getBaseValue() + SHIFT);
            MobEffectInstance own = entity.getEffect(StatusEffects.holder("skilleffect_08"));
            entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, own == null ? 0 : own.getDuration(), 0, false, false));
        }

        @Override
        public void ended(LivingEntity entity, int amplifier) {
            entity.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(entity.getAttribute(Attributes.KNOCKBACK_RESISTANCE).getBaseValue() - SHIFT);
        }
    }

    /** 施法中（禁施）：移动速度基础值 −10，结束时还原。 */
    private static final class Casting implements EffectBehavior {
        @Override
        public void started(LivingEntity entity, int amplifier) {
            entity.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(entity.getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue() - SHIFT);
        }

        @Override
        public void ended(LivingEntity entity, int amplifier) {
            entity.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(entity.getAttribute(Attributes.MOVEMENT_SPEED).getBaseValue() + SHIFT);
        }
    }
}
