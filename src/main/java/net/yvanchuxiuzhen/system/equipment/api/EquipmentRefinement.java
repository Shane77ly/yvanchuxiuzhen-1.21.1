package net.yvanchuxiuzhen.system.equipment.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
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

    public String text(String key) {
        return texts.getOrDefault(key, "");
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

    public static EquipmentRefinement fromTag(CompoundTag data) {
        if (data.isEmpty()) return EMPTY;
        Map<String, String> texts = new LinkedHashMap<>();
        Map<String, Integer> ints = new LinkedHashMap<>();
        Map<String, Double> doubles = new LinkedHashMap<>();
        Map<String, Boolean> flags = new LinkedHashMap<>();
        for (String key : data.getAllKeys()) {
            switch (data.getTagType(key)) {
                case Tag.TAG_STRING -> texts.put(key, data.getString(key));
                case Tag.TAG_BYTE -> flags.put(key, data.getBoolean(key));
                case Tag.TAG_INT, Tag.TAG_SHORT, Tag.TAG_LONG -> ints.put(key, data.getInt(key));
                case Tag.TAG_DOUBLE, Tag.TAG_FLOAT -> doubles.put(key, data.getDouble(key));
                default -> { }
            }
        }
        return new EquipmentRefinement(texts, ints, doubles, flags);
    }

    public CompoundTag toTag() {
        CompoundTag data = new CompoundTag();
        texts.forEach(data::putString);
        ints.forEach(data::putInt);
        doubles.forEach(data::putDouble);
        flags.forEach(data::putBoolean);
        return data;
    }
}
