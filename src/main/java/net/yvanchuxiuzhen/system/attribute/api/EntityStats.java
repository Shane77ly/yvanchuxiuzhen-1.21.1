package net.yvanchuxiuzhen.system.attribute.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 非玩家实体的境界、当前灵力、经验与十三项资质/属性数值的持久化载体。
 * 通过 {@code yvanchuxiuzhen:entity_stats} 附件随实体保存。
 */
public record EntityStats(
        double level,
        String realmName,
        double experience,
        double experienceMax,
        double power,
        Map<Stat, Double> values,
        Map<Stat, String> ratings
) {
    private static final Map<String, Stat> STATS_BY_ID = Arrays.stream(Stat.values())
            .collect(Collectors.toUnmodifiableMap(Stat::id, Function.identity()));
    public static final Codec<Stat> STAT_CODEC = Codec.STRING.comapFlatMap(
            id -> STATS_BY_ID.containsKey(id) ? DataResult.success(STATS_BY_ID.get(id)) : DataResult.error(() -> "未知属性项 " + id),
            Stat::id);
    public static final EntityStats EMPTY = new EntityStats(0, "", 0, 0, 0, Map.of(), Map.of());
    public static final Codec<EntityStats> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.optionalFieldOf("level", 0.0).forGetter(EntityStats::level),
            Codec.STRING.optionalFieldOf("realm_name", "").forGetter(EntityStats::realmName),
            Codec.DOUBLE.optionalFieldOf("experience", 0.0).forGetter(EntityStats::experience),
            Codec.DOUBLE.optionalFieldOf("experience_max", 0.0).forGetter(EntityStats::experienceMax),
            Codec.DOUBLE.optionalFieldOf("power", 0.0).forGetter(EntityStats::power),
            Codec.unboundedMap(STAT_CODEC, Codec.DOUBLE).optionalFieldOf("values", Map.of()).forGetter(EntityStats::values),
            Codec.unboundedMap(STAT_CODEC, Codec.STRING).optionalFieldOf("ratings", Map.of()).forGetter(EntityStats::ratings)
    ).apply(instance, EntityStats::new));

    public EntityStats {
        realmName = realmName == null ? "" : realmName;
        values = Map.copyOf(values);
        ratings = Map.copyOf(ratings);
    }

    public double stat(Stat stat) {
        return values.getOrDefault(stat, 0.0);
    }

    public String rating(Stat stat) {
        return ratings.getOrDefault(stat, "");
    }

    public EntityStats withLevel(double nextLevel) {
        return new EntityStats(nextLevel, realmName, experience, experienceMax, power, values, ratings);
    }

    public EntityStats withRealmName(String nextRealmName) {
        return new EntityStats(level, nextRealmName, experience, experienceMax, power, values, ratings);
    }

    public EntityStats withExperience(double nextExperience) {
        return new EntityStats(level, realmName, nextExperience, experienceMax, power, values, ratings);
    }

    public EntityStats withExperienceMax(double nextExperienceMax) {
        return new EntityStats(level, realmName, experience, nextExperienceMax, power, values, ratings);
    }

    public EntityStats withPower(double nextPower) {
        return new EntityStats(level, realmName, experience, experienceMax, nextPower, values, ratings);
    }

    public EntityStats withStat(Stat stat, double value) {
        Map<Stat, Double> next = new LinkedHashMap<>(values);
        next.put(stat, value);
        return new EntityStats(level, realmName, experience, experienceMax, power, next, ratings);
    }

    public EntityStats withRating(Stat stat, String rating) {
        Map<Stat, String> next = new LinkedHashMap<>(ratings);
        next.put(stat, rating == null ? "" : rating);
        return new EntityStats(level, realmName, experience, experienceMax, power, values, next);
    }

    public EntityStats withValues(Map<Stat, Double> nextValues) {
        Map<Stat, Double> merged = new LinkedHashMap<>(values);
        merged.putAll(nextValues);
        return new EntityStats(level, realmName, experience, experienceMax, power, merged, ratings);
    }

    public EntityStats withRatings(Map<Stat, String> nextRatings) {
        Map<Stat, String> merged = new LinkedHashMap<>(ratings);
        nextRatings.forEach((stat, rating) -> merged.put(stat, rating == null ? "" : rating));
        return new EntityStats(level, realmName, experience, experienceMax, power, values, merged);
    }
}
