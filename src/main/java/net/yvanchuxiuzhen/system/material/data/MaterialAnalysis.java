package net.yvanchuxiuzhen.system.material.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.yvanchuxiuzhen.system.material.api.MaterialDefinition;
import net.yvanchuxiuzhen.system.material.api.MaterialRevealedEntry;

/** 玩家材料辨识进度、已揭示名称与特殊标记；菜单选中缓存由菜单持有。 */
public record MaterialAnalysis(Map<String, Integer> progress, Map<String, Integer> maximum,
                               Map<String, List<MaterialRevealedEntry>> revealed, List<String> specialTraits,
                               boolean yihuoInitialized) {
    public static final MaterialAnalysis EMPTY = new MaterialAnalysis(Map.of(), Map.of(), Map.of(), List.of(), false);
    public static final Codec<MaterialAnalysis> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("progress").forGetter(MaterialAnalysis::progress),
            Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("maximum", Map.of()).forGetter(MaterialAnalysis::maximum),
            Codec.unboundedMap(Codec.STRING, MaterialRevealedEntry.CODEC.listOf()).optionalFieldOf("revealed", Map.of()).forGetter(MaterialAnalysis::revealed),
            Codec.STRING.listOf().optionalFieldOf("special_traits", List.of()).forGetter(MaterialAnalysis::specialTraits),
            Codec.BOOL.optionalFieldOf("yihuo_initialized", false).forGetter(MaterialAnalysis::yihuoInitialized)
    ).apply(instance, MaterialAnalysis::new));
    public static final StreamCodec<ByteBuf, MaterialAnalysis> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public MaterialAnalysis {
        progress = Map.copyOf(progress);
        maximum = Map.copyOf(maximum);
        Map<String, List<MaterialRevealedEntry>> entries = new HashMap<>();
        revealed.forEach((itemId, value) -> entries.put(itemId, List.copyOf(value)));
        revealed = Map.copyOf(entries);
        specialTraits = List.copyOf(specialTraits);
    }

    public int progress(String itemId) {
        return progress.getOrDefault(itemId, 0);
    }

    public int maximum(String itemId) {
        return maximum.getOrDefault(itemId, 0);
    }

    public List<MaterialRevealedEntry> revealed(String itemId) {
        List<MaterialRevealedEntry> entries = revealed.getOrDefault(itemId, List.of());
        return entries.subList(0, Math.min(progress(itemId), entries.size()));
    }

    public MaterialAnalysis withMaximum(String itemId, int value) {
        Map<String, Integer> next = new HashMap<>(maximum);
        next.put(itemId, value);
        return new MaterialAnalysis(progress, next, revealed, specialTraits, yihuoInitialized);
    }

    /** 按当前进度追加来源三元组，分别保存属性、特殊标记和附魔的显示判定。 */
    public MaterialAnalysis reveal(String itemId, MaterialDefinition.Entry entry) {
        Map<String, Integer> nextProgress = new HashMap<>(progress);
        int ordinal = progress(itemId) + 1;
        nextProgress.put(itemId, ordinal);
        Map<String, List<MaterialRevealedEntry>> nextRevealed = new HashMap<>(revealed);
        List<MaterialRevealedEntry> entries = new ArrayList<>(revealed.getOrDefault(itemId, List.of()));
        while (entries.size() < ordinal) {
            entries.add(MaterialRevealedEntry.EMPTY);
        }
        MaterialRevealedEntry previous = entries.get(ordinal - 1);
        MaterialRevealedEntry nextEntry = switch (entry.kind()) {
            case ATTRIBUTE -> new MaterialRevealedEntry(previous.namespace(), entry.name(), previous.open());
            case TRAIT -> new MaterialRevealedEntry("teshu", entry.name(), previous.open());
            case SPECIAL -> new MaterialRevealedEntry(entry.namespace(), entry.name(), true);
        };
        entries.set(ordinal - 1, nextEntry);
        nextRevealed.put(itemId, entries);
        List<String> nextTraits = new ArrayList<>(specialTraits);
        if (entry.kind() == MaterialDefinition.Kind.TRAIT && !nextTraits.contains(entry.name())) {
            nextTraits.add(entry.name());
        }
        return new MaterialAnalysis(nextProgress, maximum, nextRevealed, nextTraits, yihuoInitialized);
    }

    public MaterialAnalysis withYihuoInitialized() {
        return new MaterialAnalysis(progress, maximum, revealed, specialTraits, true);
    }

}
