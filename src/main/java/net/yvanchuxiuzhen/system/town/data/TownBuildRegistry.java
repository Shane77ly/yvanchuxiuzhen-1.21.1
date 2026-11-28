package net.yvanchuxiuzhen.system.town.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 城镇已完成建筑目录与类型计数强类型存档，取代旧版 {@code build_list} 拼接字符串与 {@code CompoundTag}。 */
public record TownBuildRegistry(List<Entry> entries, Map<String, Integer> typeCounts) {
    public static final TownBuildRegistry EMPTY = new TownBuildRegistry(List.of(), Map.of());

    public static final Codec<TownBuildRegistry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Entry.CODEC.listOf().fieldOf("entries").forGetter(TownBuildRegistry::entries),
            Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("type_counts").forGetter(TownBuildRegistry::typeCounts)
    ).apply(instance, TownBuildRegistry::new));

    public TownBuildRegistry {
        entries = List.copyOf(entries);
        typeCounts = Map.copyOf(typeCounts);
    }

    public boolean isEmpty() {
        return entries.isEmpty() && typeCounts.isEmpty();
    }

    public int countOf(String typeKey) {
        return typeCounts.getOrDefault(typeKey, 0);
    }

    /** 按目录条目累计各建筑类型数量，并归一化旧版命名空间前缀。 */
    public TownBuildRegistry recountTypes() {
        if (entries.isEmpty()) return this;
        Map<String, Integer> updated = new LinkedHashMap<>(typeCounts);
        for (Entry entry : entries) {
            String key = entry.type().replace("txt.yvanchuxitong.build", "txt.yvanchuxiuzhen.build");
            updated.merge(key, 1, Integer::sum);
        }
        return new TownBuildRegistry(entries, updated);
    }

    public record Entry(String name, String type) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("name").forGetter(Entry::name),
                Codec.STRING.fieldOf("type").forGetter(Entry::type)
        ).apply(instance, Entry::new));
    }
}
