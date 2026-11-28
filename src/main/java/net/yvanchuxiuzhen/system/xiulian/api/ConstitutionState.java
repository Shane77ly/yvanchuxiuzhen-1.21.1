package net.yvanchuxiuzhen.system.xiulian.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 玩家体质、丹田分页展示与外挂持续状态的强类型键值表（取代原 {@code CompoundTag} 载体 {@code tizhiitem}）。
 * 稀疏保存非零数值、非假布尔与非空文本；未写入键按 {@code 0}、{@code false}、{@code ""} 读取。
 */
public final class ConstitutionState {
    public static final Codec<ConstitutionState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).fieldOf("numbers").forGetter(s -> s.numbers),
            Codec.unboundedMap(Codec.STRING, Codec.BOOL).fieldOf("flags").forGetter(s -> s.flags),
            Codec.unboundedMap(Codec.STRING, Codec.STRING).fieldOf("texts").forGetter(s -> s.texts)
    ).apply(instance, ConstitutionState::new));

    private final Map<String, Double> numbers = new LinkedHashMap<>();
    private final Map<String, Boolean> flags = new LinkedHashMap<>();
    private final Map<String, String> texts = new LinkedHashMap<>();

    public ConstitutionState() {
    }

    private ConstitutionState(Map<String, Double> numbers, Map<String, Boolean> flags, Map<String, String> texts) {
        numbers.forEach(this::setNumber);
        flags.forEach(this::setFlag);
        texts.forEach(this::setText);
    }

    public double number(String key) {
        return numbers.getOrDefault(key, 0.0);
    }

    public void setNumber(String key, double value) {
        if (value == 0.0) {
            numbers.remove(key);
        } else {
            numbers.put(key, value);
        }
    }

    public boolean flag(String key) {
        return flags.getOrDefault(key, false);
    }

    public void setFlag(String key, boolean value) {
        if (!value) {
            flags.remove(key);
        } else {
            flags.put(key, true);
        }
    }

    public String text(String key) {
        return texts.getOrDefault(key, "");
    }

    public void setText(String key, String value) {
        if (value == null || value.isEmpty()) {
            texts.remove(key);
        } else {
            texts.put(key, value);
        }
    }

    public void clear() {
        numbers.clear();
        flags.clear();
        texts.clear();
    }

    public void copyFrom(ConstitutionState source) {
        clear();
        numbers.putAll(source.numbers);
        flags.putAll(source.flags);
        texts.putAll(source.texts);
    }

    public void copyKeyFrom(ConstitutionState source, String key) {
        setNumber(key, source.number(key));
        setFlag(key, source.flag(key));
        setText(key, source.text(key));
    }

    public ConstitutionState copy() {
        ConstitutionState copy = new ConstitutionState();
        copy.copyFrom(this);
        return copy;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ConstitutionState other)) return false;
        return numbers.equals(other.numbers) && flags.equals(other.flags) && texts.equals(other.texts);
    }

    @Override
    public int hashCode() {
        int result = numbers.hashCode();
        result = 31 * result + flags.hashCode();
        result = 31 * result + texts.hashCode();
        return result;
    }
}
