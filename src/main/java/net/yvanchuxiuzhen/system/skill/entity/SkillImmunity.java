package net.yvanchuxiuzhen.system.skill.entity;

import java.util.List;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow.DamageImmunity;

/** 按实体来源配置判定伤害免疫；投射物条件只检查直接实体。 */
final class SkillImmunity {
    private SkillImmunity() {
    }

    static boolean rejectsDamage(List<DamageImmunity> immunities, DamageSource source) {
        for (DamageImmunity immunity : immunities) {
            boolean rejected = switch (immunity) {
                case ARROW -> source.getDirectEntity() instanceof AbstractArrow;
                case PLAYER -> source.getDirectEntity() instanceof Player;
                case POTION -> source.getDirectEntity() instanceof ThrownPotion;
                case AREA_EFFECT_CLOUD -> source.getDirectEntity() instanceof AreaEffectCloud;
                case FALL -> source.is(DamageTypes.FALL);
                case CACTUS -> source.is(DamageTypes.CACTUS);
                case DROWN -> source.is(DamageTypes.DROWN);
                case LIGHTNING -> source.is(DamageTypes.LIGHTNING_BOLT);
                case EXPLOSION -> source.is(DamageTypeTags.IS_EXPLOSION);
                case TRIDENT -> source.getMsgId().equals("trident");
                case ANVIL -> source.is(DamageTypes.FALLING_ANVIL);
                case DRAGON_BREATH -> source.is(DamageTypes.DRAGON_BREATH);
                case WITHER -> source.is(DamageTypes.WITHER);
                case WITHER_SKULL -> source.getMsgId().equals("witherSkull");
            };
            if (rejected) {
                return true;
            }
        }
        return false;
    }
}
