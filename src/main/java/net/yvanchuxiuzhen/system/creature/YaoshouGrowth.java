package net.yvanchuxiuzhen.system.creature;

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
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.creature.entity.yaoshou.YaoshouShared;
import net.yvanchuxiuzhen.system.guchong.api.GuchongGrowths;

/** 来源妖兽击杀经验、连续升级与升级属性；妖兽分支优先于蛊虫分支。 */
public final class YaoshouGrowth {
    private static final TagKey<EntityType<?>> BEASTS = TagKey.create(Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "yaoshou"));

    private YaoshouGrowth() { }

    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) return;
        Entity killer = event.getSource().getEntity();
        if (killer == null || killer instanceof Player || !killer.getType().is(BEASTS)) return;
        LivingEntity victim = event.getEntity();
        CompoundTag victimData = victim.getPersistentData();
        double victimLevel = victimData.getDouble("LEVELnumber");
        double gain = victimLevel >= 1
                ? Math.pow(victimLevel, 2.4) + victimLevel * 4 + 5 + victimData.getDouble("additional_experience")
                : victim instanceof Mob ? victim.getMaxHealth() / 5 : 0;
        if (killer instanceof LivingEntity living && StatusEffects.has(living, "effect_037")) {
            // 来源先对效果等级执行整数除法，再将所得倍率乘入经验。
            gain *= (StatusEffects.amplifier(living, "effect_037") + 1) / 5 + 1;
        }
        CompoundTag data = killer.getPersistentData();
        data.putDouble("experience", data.getDouble("experience") + gain);
        boolean advanced = false;
        while (data.getDouble("experience") >= data.getDouble("experience_Max")) {
            data.putDouble("experience", data.getDouble("experience") - data.getDouble("experience_Max"));
            double number = data.getDouble("LEVELnumber") + 1;
            data.putDouble("LEVELnumber", number);
            data.putDouble("experience_Max", data.getDouble("experience_Max") + number * number * 10 + 10);
            GuchongGrowths.awardOwnerExperience(level, killer, number);
            advanced = true;
        }
        if (!advanced) return;
        double number = data.getDouble("LEVELnumber");
        YaoshouShared.updateLevelName(data, number);
        level.playSound(null, killer.blockPosition(), BuiltInRegistries.SOUND_EVENT.get(
                ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "littlelevelup")), SoundSource.NEUTRAL, 1, 1);
        level.sendParticles(ParticleTypes.WAX_ON, killer.getX(), killer.getY(), killer.getZ(), 20, 0, 0, 0, 1);
        YaoshouShared.calculateUpgradeAttributes(killer, data, number);
    }
}
