package net.yvanchuxiuzhen.system.creature;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;

/** 妖兽共享的生命、体力和神识恢复，计数随实体持久数据保存。 */
final class YaoshouRecovery {
    private static final TagKey<EntityType<?>> BEASTS = TagKey.create(Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "yaoshou"));

    private YaoshouRecovery() {
    }

    static void onTick(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LivingEntity beast) || !(beast.level() instanceof ServerLevel)
                || !beast.isAlive() || !beast.getType().is(BEASTS)) return;
        CompoundTag data = beast.getPersistentData();
        double ticks = data.getDouble("entityhuifutick") + 1;
        data.putDouble("entityhuifutick", ticks);
        if (!(ticks >= 40)) return;

        Scoreboard scoreboard = beast.level().getScoreboard();
        Objective objective = scoreboard.getObjective("zdztbegin");
        double multiplier = objective != null && scoreboard.getOrCreatePlayerScore(beast, objective).get() == 1
                ? 0.25 : 1;
        if (!StatusEffects.has(beast, "effect_007") && beast.getHealth() < beast.getMaxHealth()) {
            beast.setHealth((float) (beast.getHealth() + data.getDouble("health_Rec") * multiplier));
        }
        restore(data, "power", "power_Max", "power_Rec", multiplier);
        restore(data, "soul", "soul_Max", "soul_Rec", multiplier);
        data.putDouble("entityhuifutick", 0);
    }

    private static void restore(CompoundTag data, String value, String maximum, String recovery, double multiplier) {
        if (data.getDouble(value) < data.getDouble(maximum)) {
            data.putDouble(value, data.getDouble(value) + data.getDouble(recovery) * multiplier);
            if (data.getDouble(value) > data.getDouble(maximum)) {
                data.putDouble(value, data.getDouble(maximum));
            }
        }
    }
}
