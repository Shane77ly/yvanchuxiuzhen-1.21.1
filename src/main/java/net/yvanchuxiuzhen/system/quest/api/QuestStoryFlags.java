package net.yvanchuxiuzhen.system.quest.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** 任务剧情变量扩展强类型附件，分别保存文本、布尔、整数与浮点剧情标记。 */
public record QuestStoryFlags(
        Map<String, String> texts,
        Map<String, Boolean> booleans,
        Map<String, Integer> ints,
        Map<String, Double> doubles) {

    public static final QuestStoryFlags EMPTY = new QuestStoryFlags(Map.of(), Map.of(), Map.of(), Map.of());

    public static final Codec<QuestStoryFlags> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf("texts").forGetter(QuestStoryFlags::texts),
            Codec.unboundedMap(Codec.STRING, Codec.BOOL).fieldOf("booleans").forGetter(QuestStoryFlags::booleans),
            Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("ints").forGetter(QuestStoryFlags::ints),
            Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).fieldOf("doubles").forGetter(QuestStoryFlags::doubles)
    ).apply(instance, QuestStoryFlags::new));

    public static final StreamCodec<ByteBuf, QuestStoryFlags> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public String text(String key) {
        return texts.getOrDefault(key, "");
    }

    public boolean bool(String key) {
        return booleans.getOrDefault(key, false);
    }

    public int intValue(String key) {
        return ints.getOrDefault(key, 0);
    }

    public double doubleValue(String key) {
        return doubles.getOrDefault(key, 0.0);
    }

    public QuestStoryFlags withText(String key, String value) {
        Map<String, String> updated = new LinkedHashMap<>(texts);
        updated.put(key, value);
        return new QuestStoryFlags(Map.copyOf(updated), booleans, ints, doubles);
    }

    public QuestStoryFlags withBool(String key, boolean value) {
        Map<String, Boolean> updated = new LinkedHashMap<>(booleans);
        updated.put(key, value);
        return new QuestStoryFlags(texts, Map.copyOf(updated), ints, doubles);
    }

    public QuestStoryFlags withInt(String key, int value) {
        Map<String, Integer> updated = new LinkedHashMap<>(ints);
        updated.put(key, value);
        return new QuestStoryFlags(texts, booleans, Map.copyOf(updated), doubles);
    }

    public QuestStoryFlags withDouble(String key, double value) {
        Map<String, Double> updated = new LinkedHashMap<>(doubles);
        updated.put(key, value);
        return new QuestStoryFlags(texts, booleans, ints, Map.copyOf(updated));
    }
}
