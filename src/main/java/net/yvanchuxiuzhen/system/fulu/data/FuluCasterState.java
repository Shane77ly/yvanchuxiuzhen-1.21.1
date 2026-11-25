package net.yvanchuxiuzhen.system.fulu.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.LinkedHashMap;
import java.util.Map;

/** 实体上的符箓施法品阶、持续效果与焚烧知识标记的持久化载体。 */
public record FuluCasterState(
        boolean burnedKnowledge,
        double pendingLevel,
        double level05,
        double level29,
        double level32,
        boolean splitFirst32,
        double level41,
        double level45,
        double level46,
        double activeCount,
        Map<String, Boolean> activeEffects,
        Map<String, Double> effectLevels,
        Map<String, Double> effectTicks,
        Map<String, Double> volleyTicks
) {
    public static final FuluCasterState EMPTY = new FuluCasterState(
            false, 0, 0, 0, 0, false, 0, 0, 0, 0, Map.of(), Map.of(), Map.of(), Map.of());
    public static final Codec<FuluCasterState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("burned_knowledge", false).forGetter(FuluCasterState::burnedKnowledge),
            Codec.DOUBLE.optionalFieldOf("pending_level", 0.0).forGetter(FuluCasterState::pendingLevel),
            Codec.DOUBLE.optionalFieldOf("level_05", 0.0).forGetter(FuluCasterState::level05),
            Codec.DOUBLE.optionalFieldOf("level_29", 0.0).forGetter(FuluCasterState::level29),
            Codec.DOUBLE.optionalFieldOf("level_32", 0.0).forGetter(FuluCasterState::level32),
            Codec.BOOL.optionalFieldOf("split_first_32", false).forGetter(FuluCasterState::splitFirst32),
            Codec.DOUBLE.optionalFieldOf("level_41", 0.0).forGetter(FuluCasterState::level41),
            Codec.DOUBLE.optionalFieldOf("level_45", 0.0).forGetter(FuluCasterState::level45),
            Codec.DOUBLE.optionalFieldOf("level_46", 0.0).forGetter(FuluCasterState::level46),
            Codec.DOUBLE.optionalFieldOf("active_count", 0.0).forGetter(FuluCasterState::activeCount),
            Codec.unboundedMap(Codec.STRING, Codec.BOOL).optionalFieldOf("active_effects", Map.of()).forGetter(FuluCasterState::activeEffects),
            Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).optionalFieldOf("effect_levels", Map.of()).forGetter(FuluCasterState::effectLevels),
            Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).optionalFieldOf("effect_ticks", Map.of()).forGetter(FuluCasterState::effectTicks),
            Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).optionalFieldOf("volley_ticks", Map.of()).forGetter(FuluCasterState::volleyTicks)
    ).apply(instance, FuluCasterState::new));

    public FuluCasterState {
        activeEffects = Map.copyOf(activeEffects);
        effectLevels = Map.copyOf(effectLevels);
        effectTicks = Map.copyOf(effectTicks);
        volleyTicks = Map.copyOf(volleyTicks);
    }

    public boolean isEffectActive(String suffix) {
        return activeEffects.getOrDefault(suffix, false);
    }

    public double effectLevel(String suffix) {
        return effectLevels.getOrDefault(suffix, 0.0);
    }

    public double effectTick(String suffix) {
        return effectTicks.getOrDefault(suffix, 0.0);
    }

    public double volleyTick(String suffix) {
        return volleyTicks.getOrDefault(suffix, 0.0);
    }

    public FuluCasterState withBurnedKnowledge(boolean next) {
        return new FuluCasterState(next, pendingLevel, level05, level29, level32, splitFirst32, level41, level45, level46,
                activeCount, activeEffects, effectLevels, effectTicks, volleyTicks);
    }

    public FuluCasterState withPendingLevel(double next) {
        return new FuluCasterState(burnedKnowledge, next, level05, level29, level32, splitFirst32, level41, level45, level46,
                activeCount, activeEffects, effectLevels, effectTicks, volleyTicks);
    }

    public FuluCasterState withLevel05(double next) {
        return new FuluCasterState(burnedKnowledge, pendingLevel, next, level29, level32, splitFirst32, level41, level45, level46,
                activeCount, activeEffects, effectLevels, effectTicks, volleyTicks);
    }

    public FuluCasterState withLevel29(double next) {
        return new FuluCasterState(burnedKnowledge, pendingLevel, level05, next, level32, splitFirst32, level41, level45, level46,
                activeCount, activeEffects, effectLevels, effectTicks, volleyTicks);
    }

    public FuluCasterState withCluster32(double level, boolean splitFirst) {
        return new FuluCasterState(burnedKnowledge, pendingLevel, level05, level29, level, splitFirst, level41, level45, level46,
                activeCount, activeEffects, effectLevels, effectTicks, volleyTicks);
    }

    public FuluCasterState withLevel41(double next) {
        return new FuluCasterState(burnedKnowledge, pendingLevel, level05, level29, level32, splitFirst32, next, level45, level46,
                activeCount, activeEffects, effectLevels, effectTicks, volleyTicks);
    }

    public FuluCasterState withLevel45(double next) {
        return new FuluCasterState(burnedKnowledge, pendingLevel, level05, level29, level32, splitFirst32, level41, next, level46,
                activeCount, activeEffects, effectLevels, effectTicks, volleyTicks);
    }

    public FuluCasterState withLevel46(double next) {
        return new FuluCasterState(burnedKnowledge, pendingLevel, level05, level29, level32, splitFirst32, level41, level45, next,
                activeCount, activeEffects, effectLevels, effectTicks, volleyTicks);
    }

    public FuluCasterState withActiveCount(double next) {
        return new FuluCasterState(burnedKnowledge, pendingLevel, level05, level29, level32, splitFirst32, level41, level45, level46,
                next, activeEffects, effectLevels, effectTicks, volleyTicks);
    }

    public FuluCasterState withEffect(String suffix, boolean active, double level, double tick) {
        Map<String, Boolean> nextActive = new LinkedHashMap<>(activeEffects);
        nextActive.put(suffix, active);
        Map<String, Double> nextLevels = new LinkedHashMap<>(effectLevels);
        nextLevels.put(suffix, level);
        Map<String, Double> nextTicks = new LinkedHashMap<>(effectTicks);
        nextTicks.put(suffix, tick);
        return new FuluCasterState(burnedKnowledge, pendingLevel, level05, level29, level32, splitFirst32, level41, level45, level46,
                activeCount, nextActive, nextLevels, nextTicks, volleyTicks);
    }

    public FuluCasterState withVolleyTick(String suffix, double next) {
        Map<String, Double> nextVolley = new LinkedHashMap<>(volleyTicks);
        nextVolley.put(suffix, next);
        return new FuluCasterState(burnedKnowledge, pendingLevel, level05, level29, level32, splitFirst32, level41, level45, level46,
                activeCount, activeEffects, effectLevels, effectTicks, nextVolley);
    }
}
