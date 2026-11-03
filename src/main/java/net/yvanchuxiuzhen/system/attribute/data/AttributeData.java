package net.yvanchuxiuzhen.system.attribute.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.yvanchuxiuzhen.system.attribute.api.Contribution;
import net.yvanchuxiuzhen.system.attribute.api.Layer;
import net.yvanchuxiuzhen.system.attribute.api.Stat;
import net.yvanchuxiuzhen.system.attribute.rule.AttributeTotals;

/**
 * 玩家附件的内容：按来源键保存的贡献。基础值与最终值由贡献推导，不单独存档。
 * 值不可变，修改时生成新对象并通过 {@code setData} 写回，写回即同步给本人。
 */
public final class AttributeData {
    private static final Map<String, Stat> STATS_BY_ID = Arrays.stream(Stat.values())
            .collect(Collectors.toUnmodifiableMap(Stat::id, Function.identity()));
    private static final Map<String, Layer> LAYERS_BY_ID = Arrays.stream(Layer.values())
            .collect(Collectors.toUnmodifiableMap(Layer::id, Function.identity()));
    private static final Codec<Stat> STAT_CODEC = Codec.STRING.comapFlatMap(
            id -> STATS_BY_ID.containsKey(id) ? DataResult.success(STATS_BY_ID.get(id)) : DataResult.error(() -> "未知属性项 " + id),
            Stat::id);
    private static final Codec<Layer> LAYER_CODEC = Codec.STRING.comapFlatMap(
            id -> LAYERS_BY_ID.containsKey(id) ? DataResult.success(LAYERS_BY_ID.get(id)) : DataResult.error(() -> "未知贡献层 " + id),
            Layer::id);
    private static final Codec<Contribution> CONTRIBUTION_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            LAYER_CODEC.fieldOf("layer").forGetter(Contribution::layer),
            Codec.unboundedMap(STAT_CODEC, Codec.DOUBLE).fieldOf("values").forGetter(Contribution::values)
    ).apply(instance, Contribution::new));
    public static final Codec<AttributeData> CODEC = Codec.unboundedMap(Codec.STRING, CONTRIBUTION_CODEC)
            .xmap(AttributeData::new, AttributeData::contributions);
    public static final StreamCodec<RegistryFriendlyByteBuf, AttributeData> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);
    public static final AttributeData EMPTY = new AttributeData(Map.of());

    private final Map<String, Contribution> contributions;
    private final AttributeTotals totals;

    private AttributeData(Map<String, Contribution> contributions) {
        this.contributions = Map.copyOf(contributions);
        this.totals = new AttributeTotals(this.contributions.values());
    }

    public Map<String, Contribution> contributions() {
        return contributions;
    }

    public AttributeTotals totals() {
        return totals;
    }

    public AttributeData with(String source, Contribution contribution) {
        Map<String, Contribution> next = new HashMap<>(contributions);
        next.put(source, contribution);
        return new AttributeData(next);
    }

    public AttributeData without(String source) {
        Map<String, Contribution> next = new HashMap<>(contributions);
        next.remove(source);
        return new AttributeData(next);
    }
}
