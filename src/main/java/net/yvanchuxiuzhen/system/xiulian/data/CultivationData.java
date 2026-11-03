package net.yvanchuxiuzhen.system.xiulian.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationText;
import net.yvanchuxiuzhen.system.xiulian.api.CultivationValue;

/**
 * 玩家的修炼档案：境界、修为、灵力与神识、灵根与阴阳、罡煞与功德、雷劫、血脉、属性点、寿元、出身与体质、
 * 闭关天数，本命法宝槽（来源 {@code benmingfabao}），以及体质与修正值的键值存储（来源隐藏载体 {@code tizhiitem} 的物品标签，
 * 键见 {@code docs/systems/xiulian/ledger/tizhiitem_keys.csv}）。存档只写与初始值不同的项。
 * 对象可变，修改后由调用方 {@code syncData} 同步给本人。
 */
public final class CultivationData {
    private static final Map<String, CultivationValue> VALUES_BY_ID = Arrays.stream(CultivationValue.values())
            .collect(Collectors.toUnmodifiableMap(CultivationValue::id, Function.identity()));
    private static final Map<String, CultivationText> TEXTS_BY_ID = Arrays.stream(CultivationText.values())
            .collect(Collectors.toUnmodifiableMap(CultivationText::id, Function.identity()));
    private static final Codec<CultivationValue> VALUE_CODEC = Codec.STRING.comapFlatMap(
            id -> VALUES_BY_ID.containsKey(id) ? DataResult.success(VALUES_BY_ID.get(id)) : DataResult.error(() -> "未知修炼数值项 " + id),
            CultivationValue::id);
    private static final Codec<CultivationText> TEXT_CODEC = Codec.STRING.comapFlatMap(
            id -> TEXTS_BY_ID.containsKey(id) ? DataResult.success(TEXTS_BY_ID.get(id)) : DataResult.error(() -> "未知修炼文本项 " + id),
            CultivationText::id);
    public static final Codec<CultivationData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(VALUE_CODEC, Codec.DOUBLE).fieldOf("values").forGetter(CultivationData::changedValues),
            Codec.unboundedMap(TEXT_CODEC, Codec.STRING).fieldOf("texts").forGetter(CultivationData::changedTexts),
            Codec.BOOL.fieldOf("thunder_open").forGetter(CultivationData::thunderOpen),
            ItemStack.OPTIONAL_CODEC.fieldOf("benming").forGetter(CultivationData::benming),
            CompoundTag.CODEC.fieldOf("tizhi").forGetter(CultivationData::tizhi)
    ).apply(instance, CultivationData::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, CultivationData> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    private final Map<CultivationValue, Double> values = new EnumMap<>(CultivationValue.class);
    private final Map<CultivationText, String> texts = new EnumMap<>(CultivationText.class);
    private boolean thunderOpen;
    private ItemStack benming;
    private final CompoundTag tizhi;

    public CultivationData() {
        this(Map.of(), Map.of(), false, ItemStack.EMPTY, new CompoundTag());
    }

    private CultivationData(Map<CultivationValue, Double> values, Map<CultivationText, String> texts, boolean thunderOpen, ItemStack benming,
                            CompoundTag tizhi) {
        for (CultivationValue value : CultivationValue.values()) {
            this.values.put(value, values.getOrDefault(value, value.initial()));
        }
        for (CultivationText text : CultivationText.values()) {
            this.texts.put(text, texts.getOrDefault(text, text.initial()));
        }
        this.thunderOpen = thunderOpen;
        this.benming = benming;
        this.tizhi = tizhi.copy();
    }

    private Map<CultivationValue, Double> changedValues() {
        Map<CultivationValue, Double> changed = new EnumMap<>(CultivationValue.class);
        values.forEach((value, amount) -> {
            if (amount != value.initial()) {
                changed.put(value, amount);
            }
        });
        return changed;
    }

    private Map<CultivationText, String> changedTexts() {
        Map<CultivationText, String> changed = new EnumMap<>(CultivationText.class);
        texts.forEach((text, content) -> {
            if (!content.equals(text.initial())) {
                changed.put(text, content);
            }
        });
        return changed;
    }

    public double get(CultivationValue value) {
        return values.get(value);
    }

    public void set(CultivationValue value, double amount) {
        values.put(value, amount);
    }

    public String get(CultivationText text) {
        return texts.get(text);
    }

    public void set(CultivationText text, String content) {
        texts.put(text, content);
    }

    public boolean thunderOpen() {
        return thunderOpen;
    }

    public void setThunderOpen(boolean thunderOpen) {
        this.thunderOpen = thunderOpen;
    }

    public ItemStack benming() {
        return benming;
    }

    public void setBenming(ItemStack benming) {
        this.benming = benming;
    }

    /** 体质与修正值；键与类型沿用来源，未写入的键读作 0、false 或空串。 */
    public CompoundTag tizhi() {
        return tizhi;
    }
}
