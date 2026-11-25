package net.yvanchuxiuzhen.system.guchong.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.attribute.api.EntityStats;
import net.yvanchuxiuzhen.system.guchong.api.GuchongGrowths;
import net.yvanchuxiuzhen.system.guchong.api.GuchongUpgradeEvent;
import net.yvanchuxiuzhen.system.guchong.api.GuchongVictimDataEvent;
import net.yvanchuxiuzhen.system.guchong.data.GuchongData;

/** 来源EntityDeath的蛊虫击杀经验、连续升级、挑衅标记清除与养蛊经验消费者。 */
public final class GuchongGrowth {
    private static final TagKey<EntityType<?>> INSECTS = TagKey.create(Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "guchongentity"));
    private static final TagKey<EntityType<?>> BEASTS = TagKey.create(Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "yaoshou"));
    private GuchongGrowth() { }

    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) return;
        Entity insect = event.getSource().getEntity();
        if (insect == null || insect instanceof Player || !insect.getType().is(INSECTS)
                || insect.getType().is(BEASTS)) return;
        LivingEntity victim = event.getEntity();
        if (!insect.getData(GuchongData.PROVOKE_TARGET).isEmpty()) insect.setData(GuchongData.PROVOKE_TARGET, "");
        double gain;
        if (insect.getType() == victim.getType()) {
            gain = EntityAttributes.experienceMax(victim) / 4;
        } else {
            GuchongVictimDataEvent request = NeoForge.EVENT_BUS.post(new GuchongVictimDataEvent(victim));
            double victimLevel = request.level();
            gain = victimLevel >= 1
                    ? Math.pow(victimLevel, 2.4) + victimLevel * 4 + 5 + request.additionalExperience()
                    : victim instanceof Mob ? victim.getMaxHealth() / 5 : 0;
        }
        EntityStats stats = EntityAttributes.get(insect);
        double experience = stats.experience() + gain;
        double number = stats.level();
        double experienceMax = stats.experienceMax();
        boolean advanced = false;
        while (experience >= experienceMax) {
            experience -= experienceMax;
            number += 1;
            experienceMax += number * number * 10 + 10;
            GuchongGrowths.awardOwnerExperience(level, insect, number);
            advanced = true;
        }
        EntityAttributes.set(insect, stats.withExperience(experience).withLevel(number).withExperienceMax(experienceMax));
        if (!advanced) return;
        level.playSound(null, insect.blockPosition(), BuiltInRegistries.SOUND_EVENT.get(
                ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "littlelevelup")), SoundSource.NEUTRAL, 1, 1);
        level.sendParticles(ParticleTypes.WAX_ON, insect.getX(), insect.getY(), insect.getZ(), 20, 0, 0, 0, 1);
        GuchongStats.updateLevelName(insect);
        GuchongUpgradeEvent request = NeoForge.EVENT_BUS.post(new GuchongUpgradeEvent(victim));
        if (!request.victimRecalculationSelected()) GuchongStats.calculateUpgradeAttributes(insect);
    }
}
