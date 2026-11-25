package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.combat.api.EffectExperienceEvent;
import net.yvanchuxiuzhen.system.combat.api.HeavyHitEvent;
import net.yvanchuxiuzhen.system.creature.data.CreatureData;
import net.yvanchuxiuzhen.system.particle.api.Particles;

/** 妖兽对来源重击与修为效果事件的响应。 */
public final class YaoshouCombat {
    private static final TagKey<EntityType<?>> YAOSHOU = TagKey.create(Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "yaoshou"));

    private YaoshouCombat() { }

    public static void register() {
        NeoForge.EVENT_BUS.addListener(YaoshouCombat::onHeavyHit);
        NeoForge.EVENT_BUS.addListener(YaoshouCombat::onEffectExperience);
    }

    private static void onEffectExperience(EffectExperienceEvent event) {
        LivingEntity entity = event.entity();
        if (!entity.getType().is(YAOSHOU)) return;
        double experience = EntityAttributes.experience(entity) + event.experience();
        double maximum = EntityAttributes.experienceMax(entity);
        if (experience >= maximum) {
            EntityAttributes.setExperience(entity, experience - maximum);
            double level = EntityAttributes.level(entity) + 1;
            EntityAttributes.setLevel(entity, level);
            EntityAttributes.setExperienceMax(entity, maximum + Math.pow(level, 2) * 10 + 10);
            YaoshouShared.updateLevelName(entity, level);
            YaoshouShared.calculateUpgradeAttributes(entity, level);
        } else {
            EntityAttributes.setExperience(entity, experience);
        }
    }

    private static void onHeavyHit(HeavyHitEvent event) {
        if (!event.victim().getType().is(YAOSHOU)) return;
        if (!(event.victim() instanceof YaoshouEntity entity)) return;
        if (CreatureData.state(entity).skill().isEmpty()) return;
        CreatureData.state(entity).setInterrupted(true);
        YaoshouShared.finishSkill(entity);
        if (entity.level() instanceof ServerLevel level) {
            level.sendParticles(Particles.type("daduan"), entity.getX(), entity.getY() + entity.getBbHeight() / 2,
                    entity.getZ(), 1, 0, 0, 0, 0);
        }
    }
}
