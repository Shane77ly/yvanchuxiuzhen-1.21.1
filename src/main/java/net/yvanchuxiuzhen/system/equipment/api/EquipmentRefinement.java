package net.yvanchuxiuzhen.system.equipment.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** 炼器成品与升级胚体上的强类型炼器原料及特性组件（{@code yvanchuxiuzhen:equipment_refinement}）。 */
public record EquipmentRefinement(
        Map<String, String> texts,
        Map<String, Integer> ints,
        Map<String, Double> doubles,
        Map<String, Boolean> flags) {
    public static final EquipmentRefinement EMPTY = new EquipmentRefinement(Map.of(), Map.of(), Map.of(), Map.of());
    public static final Codec<EquipmentRefinement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("texts", Map.of()).forGetter(EquipmentRefinement::texts),
            Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("ints", Map.of()).forGetter(EquipmentRefinement::ints),
            Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).optionalFieldOf("doubles", Map.of()).forGetter(EquipmentRefinement::doubles),
            Codec.unboundedMap(Codec.STRING, Codec.BOOL).optionalFieldOf("flags", Map.of()).forGetter(EquipmentRefinement::flags)
    ).apply(instance, EquipmentRefinement::new));
    public static final StreamCodec<ByteBuf, EquipmentRefinement> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public EquipmentRefinement {
        texts = Map.copyOf(texts);
        ints = Map.copyOf(ints);
        doubles = Map.copyOf(doubles);
        flags = Map.copyOf(flags);
    }

    public boolean isEmpty() {
        return texts.isEmpty() && ints.isEmpty() && doubles.isEmpty() && flags.isEmpty();
    }

    public int integer(String key) {
        if (ints.containsKey(key)) return ints.get(key);
        Double value = doubles.get(key);
        return value == null ? 0 : value.intValue();
    }

    public double number(String key) {
        if (doubles.containsKey(key)) return doubles.get(key);
        Integer value = ints.get(key);
        return value == null ? 0 : value.doubleValue();
    }

    public String text(String key) {
        return texts.getOrDefault(key, "");
    }

    public boolean hasText(String key) {
        return texts.containsKey(key);
    }

    public boolean flag(String key) {
        return flags.getOrDefault(key, false);
    }

    public EquipmentRefinement withFlag(String key, boolean value) {
        Map<String, Boolean> updated = new LinkedHashMap<>(flags);
        updated.put(key, value);
        return new EquipmentRefinement(texts, ints, doubles, updated);
    }

    public EquipmentRefinement withIntAdded(String key, int delta) {
        Map<String, Integer> updated = new LinkedHashMap<>(ints);
        updated.put(key, integer(key) + delta);
        return new EquipmentRefinement(texts, updated, doubles, flags);
    }

    public EquipmentRefinement withIntSet(String key, int value) {
        Map<String, Integer> updated = new LinkedHashMap<>(ints);
        updated.put(key, value);
        return new EquipmentRefinement(texts, updated, doubles, flags);
    }

    public static Mutable mutable() {
        return new Mutable();
    }

    public Mutable toMutable() {
        return new Mutable(this);
    }

    /** 炼器与图纸预览共用的强类型可变暂存区，取代中转 {@code CompoundTag}。 */
    public static final class Mutable {
        private final Map<String, String> texts = new LinkedHashMap<>();
        private final Map<String, Integer> ints = new LinkedHashMap<>();
        private final Map<String, Double> doubles = new LinkedHashMap<>();
        private final Map<String, Boolean> flags = new LinkedHashMap<>();

        private Mutable() {
        }

        private Mutable(EquipmentRefinement source) {
            texts.putAll(source.texts);
            ints.putAll(source.ints);
            doubles.putAll(source.doubles);
            flags.putAll(source.flags);
        }

        public String text(String key) {
            return texts.getOrDefault(key, "");
        }

        public boolean hasText(String key) {
            return texts.containsKey(key);
        }

        public void putString(String key, String value) {
            texts.put(key, value);
        }

        public void removeText(String key) {
            texts.remove(key);
        }

        public int integer(String key) {
            if (ints.containsKey(key)) return ints.get(key);
            Double value = doubles.get(key);
            return value == null ? 0 : value.intValue();
        }

        public void putInt(String key, int value) {
            doubles.remove(key);
            ints.put(key, value);
        }

        public double number(String key) {
            if (doubles.containsKey(key)) return doubles.get(key);
            Integer value = ints.get(key);
            return value == null ? 0 : value.doubleValue();
        }

        public void putDouble(String key, double value) {
            ints.remove(key);
            doubles.put(key, value);
        }

        public boolean flag(String key) {
            return flags.getOrDefault(key, false);
        }

        public void putBoolean(String key, boolean value) {
            flags.put(key, value);
        }

        public EquipmentRefinement toImmutable() {
            return new EquipmentRefinement(texts, ints, doubles, flags);
        }
    }
}
