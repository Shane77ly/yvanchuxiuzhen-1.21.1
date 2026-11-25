package net.yvanchuxiuzhen.system.creature;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.attribute.api.EntityAttributes;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.combat.api.StatusEffects;
import net.yvanchuxiuzhen.system.creature.data.CreatureData;
import net.yvanchuxiuzhen.system.creature.data.CreatureState;
import net.yvanchuxiuzhen.system.xiulian.api.Cultivation;

/** 妖兽共享的生命、体力和神识恢复，计数随实体附件保存。 */
final class YaoshouRecovery {
    private static final TagKey<EntityType<?>> BEASTS = TagKey.create(Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "yaoshou"));

    private YaoshouRecovery() {
    }

    static void onTick(EntityTickEvent.Pre event) {
        if (!(event.getEntity() instanceof LivingEntity beast) || !(beast.level() instanceof ServerLevel)
                || !beast.isAlive() || !beast.getType().is(BEASTS)) return;
        CreatureState state = CreatureData.state(beast);
        double ticks = state.recoveryTicks() + 1;
        state.setRecoveryTicks(ticks);
        if (!(ticks >= 40)) return;

        Scoreboard scoreboard = beast.level().getScoreboard();
        Objective objective = scoreboard.getObjective("zdztbegin");
        double multiplier = objective != null && scoreboard.getOrCreatePlayerScore(beast, objective).get() == 1
                ? 0.25 : 1;
        if (!StatusEffects.has(beast, "effect_007") && beast.getHealth() < beast.getMaxHealth()) {
            beast.setHealth((float) (beast.getHealth() + EntityAttributes.stat(beast, Stat.HEALTH_REC) * multiplier));
        }
        double powerMax = EntityAttributes.stat(beast, Stat.POWER_MAX);
        double power = EntityAttributes.power(beast);
        if (power < powerMax) {
            EntityAttributes.setPower(beast, Math.min(powerMax, power + EntityAttributes.stat(beast, Stat.POWER_REC) * multiplier));
        }
        double soulMax = EntityAttributes.stat(beast, Stat.SOUL_MAX);
        double soul = Cultivation.entitySoul(beast);
        if (soul < soulMax) {
            Cultivation.setEntitySoul(beast, Math.min(soulMax, soul + EntityAttributes.stat(beast, Stat.SOUL_REC) * multiplier));
        }
        state.setRecoveryTicks(0);
    }
}
