package net.yvanchuxiuzhen.system.guchong.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
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
import net.yvanchuxiuzhen.system.guchong.api.GuchongGrowths;
import net.yvanchuxiuzhen.system.guchong.api.GuchongUpgradeEvent;
import net.yvanchuxiuzhen.system.guchong.api.GuchongVictimDataEvent;

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
        CompoundTag data = insect.getPersistentData();
        if (!data.getString("attackmubiao").isEmpty()) data.putString("attackmubiao", "");
        double gain;
        if (insect.getType() == victim.getType()) {
            gain = victim.getPersistentData().getDouble("experience_Max") / 4;
        } else {
            GuchongVictimDataEvent request = NeoForge.EVENT_BUS.post(new GuchongVictimDataEvent(victim));
            double victimLevel = request.level();
            gain = victimLevel >= 1
                    ? Math.pow(victimLevel, 2.4) + victimLevel * 4 + 5 + request.additionalExperience()
                    : victim instanceof Mob ? victim.getMaxHealth() / 5 : 0;
        }
        data.putDouble("experience", data.getDouble("experience") + gain);
        boolean advanced = false;
        while (data.getDouble("experience") >= data.getDouble("experience_Max")) {
            data.putDouble("experience", data.getDouble("experience") - data.getDouble("experience_Max"));
            double number = data.getDouble("LEVELnumber") + 1;
            data.putDouble("LEVELnumber", number);
            data.putDouble("experience_Max", data.getDouble("experience_Max") + number * number * 10 + 10);
            GuchongGrowths.awardOwnerExperience(level, insect, number);
            advanced = true;
        }
        if (!advanced) return;
        level.playSound(null, insect.blockPosition(), BuiltInRegistries.SOUND_EVENT.get(
                ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "littlelevelup")), SoundSource.NEUTRAL, 1, 1);
        level.sendParticles(ParticleTypes.WAX_ON, insect.getX(), insect.getY(), insect.getZ(), 20, 0, 0, 0, 1);
        GuchongStats.updateLevelName(insect);
        GuchongUpgradeEvent request = NeoForge.EVENT_BUS.post(new GuchongUpgradeEvent(victim));
        if (!request.victimRecalculationSelected()) GuchongStats.calculateUpgradeAttributes(insect);
    }
}
