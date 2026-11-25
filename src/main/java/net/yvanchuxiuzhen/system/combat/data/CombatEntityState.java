package net.yvanchuxiuzhen.system.combat.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.LinkedHashMap;
import java.util.Map;

/** 非持久化键堆砌的战斗与状态效果运行时实体状态，随 NeoForge 实体附件保存。 */
public record CombatEntityState(
        boolean foundEnemy,
        boolean noAi,
        boolean flightBlocked,
        double stepX,
        double stepZ,
        double clientStepX,
        double clientStepZ,
        double miasmaLevel,
        double dayDeathLevel,
        double corpsePoisonLevel,
        Map<String, Double> ticks
) {
    public static final CombatEntityState EMPTY = new CombatEntityState(
            false, false, false, 0, 0, 0, 0, 0, 0, 0, Map.of());

    public static final Codec<CombatEntityState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("found_enemy", false).forGetter(CombatEntityState::foundEnemy),
            Codec.BOOL.optionalFieldOf("no_ai", false).forGetter(CombatEntityState::noAi),
            Codec.BOOL.optionalFieldOf("flight_blocked", false).forGetter(CombatEntityState::flightBlocked),
            Codec.DOUBLE.optionalFieldOf("step_x", 0.0).forGetter(CombatEntityState::stepX),
            Codec.DOUBLE.optionalFieldOf("step_z", 0.0).forGetter(CombatEntityState::stepZ),
            Codec.DOUBLE.optionalFieldOf("client_step_x", 0.0).forGetter(CombatEntityState::clientStepX),
            Codec.DOUBLE.optionalFieldOf("client_step_z", 0.0).forGetter(CombatEntityState::clientStepZ),
            Codec.DOUBLE.optionalFieldOf("miasma_level", 0.0).forGetter(CombatEntityState::miasmaLevel),
            Codec.DOUBLE.optionalFieldOf("day_death_level", 0.0).forGetter(CombatEntityState::dayDeathLevel),
            Codec.DOUBLE.optionalFieldOf("corpse_poison_level", 0.0).forGetter(CombatEntityState::corpsePoisonLevel),
            Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).optionalFieldOf("ticks", Map.of()).forGetter(CombatEntityState::ticks)
    ).apply(instance, CombatEntityState::new));

    public CombatEntityState withFoundEnemy(boolean value) {
        return new CombatEntityState(value, noAi, flightBlocked, stepX, stepZ, clientStepX, clientStepZ, miasmaLevel, dayDeathLevel, corpsePoisonLevel, ticks);
    }

    public CombatEntityState withNoAi(boolean value) {
        return new CombatEntityState(foundEnemy, value, flightBlocked, stepX, stepZ, clientStepX, clientStepZ, miasmaLevel, dayDeathLevel, corpsePoisonLevel, ticks);
    }

    public CombatEntityState withFlightBlocked(boolean value) {
        return new CombatEntityState(foundEnemy, noAi, value, stepX, stepZ, clientStepX, clientStepZ, miasmaLevel, dayDeathLevel, corpsePoisonLevel, ticks);
    }

    public CombatEntityState withStep(double x, double z) {
        return new CombatEntityState(foundEnemy, noAi, flightBlocked, x, z, clientStepX, clientStepZ, miasmaLevel, dayDeathLevel, corpsePoisonLevel, ticks);
    }

    public CombatEntityState withClientStep(double x, double z) {
        return new CombatEntityState(foundEnemy, noAi, flightBlocked, stepX, stepZ, x, z, miasmaLevel, dayDeathLevel, corpsePoisonLevel, ticks);
    }

    public CombatEntityState withMiasmaLevel(double value) {
        return new CombatEntityState(foundEnemy, noAi, flightBlocked, stepX, stepZ, clientStepX, clientStepZ, value, dayDeathLevel, corpsePoisonLevel, ticks);
    }

    public CombatEntityState withDayDeathLevel(double value) {
        return new CombatEntityState(foundEnemy, noAi, flightBlocked, stepX, stepZ, clientStepX, clientStepZ, miasmaLevel, value, corpsePoisonLevel, ticks);
    }

    public CombatEntityState withCorpsePoisonLevel(double value) {
        return new CombatEntityState(foundEnemy, noAi, flightBlocked, stepX, stepZ, clientStepX, clientStepZ, miasmaLevel, dayDeathLevel, value, ticks);
    }

    public double tick(String key) {
        return ticks.getOrDefault(key, 0.0);
    }

    public CombatEntityState withTick(String key, double value) {
        Map<String, Double> next = new LinkedHashMap<>(ticks);
        if (value == 0) next.remove(key);
        else next.put(key, value);
        return new CombatEntityState(foundEnemy, noAi, flightBlocked, stepX, stepZ, clientStepX, clientStepZ, miasmaLevel, dayDeathLevel, corpsePoisonLevel, Map.copyOf(next));
    }
}
