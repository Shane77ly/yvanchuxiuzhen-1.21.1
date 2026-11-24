package net.yvanchuxiuzhen.system.guchong.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** 蛊虫、蛊罐与虫笛物品承载的结构化属性与槽位状态组件（{@code yvanchuxiuzhen:guchong_state}）。 */
public record GuchongState(Map<String, String> texts, Map<String, Double> doubles,
                           Map<String, Integer> ints, Map<String, Boolean> flags) {
    public static final GuchongState EMPTY = new GuchongState(Map.of(), Map.of(), Map.of(), Map.of());
    public static final Codec<GuchongState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("texts", Map.of()).forGetter(GuchongState::texts),
            Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).optionalFieldOf("doubles", Map.of()).forGetter(GuchongState::doubles),
            Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("ints", Map.of()).forGetter(GuchongState::ints),
            Codec.unboundedMap(Codec.STRING, Codec.BOOL).optionalFieldOf("flags", Map.of()).forGetter(GuchongState::flags)
    ).apply(instance, GuchongState::new));
    public static final StreamCodec<ByteBuf, GuchongState> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public GuchongState {
        texts = Map.copyOf(texts);
        doubles = Map.copyOf(doubles);
        ints = Map.copyOf(ints);
        flags = Map.copyOf(flags);
    }

    public boolean isEmpty() {
        return texts.isEmpty() && doubles.isEmpty() && ints.isEmpty() && flags.isEmpty();
    }

    public static GuchongState fromTag(CompoundTag data) {
        if (data.isEmpty()) return EMPTY;
        Map<String, String> texts = new LinkedHashMap<>();
        Map<String, Double> doubles = new LinkedHashMap<>();
        Map<String, Integer> ints = new LinkedHashMap<>();
        Map<String, Boolean> flags = new LinkedHashMap<>();
        for (String key : data.getAllKeys()) {
            byte type = data.getTagType(key);
            switch (type) {
                case Tag.TAG_STRING -> texts.put(key, data.getString(key));
                case Tag.TAG_BYTE -> flags.put(key, data.getBoolean(key));
                case Tag.TAG_INT, Tag.TAG_SHORT, Tag.TAG_LONG -> ints.put(key, data.getInt(key));
                case Tag.TAG_DOUBLE, Tag.TAG_FLOAT -> doubles.put(key, data.getDouble(key));
                default -> { }
            }
        }
        return new GuchongState(texts, doubles, ints, flags);
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
