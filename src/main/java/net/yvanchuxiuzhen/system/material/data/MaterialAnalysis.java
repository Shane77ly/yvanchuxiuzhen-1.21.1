package net.yvanchuxiuzhen.system.material.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** 玩家材料辨识进度，对应来源 {@code playercailiaojiexi}；界面选中缓存不写入。 */
public record MaterialAnalysis(Map<String, Integer> progress, boolean yihuoInitialized) {
    public static final MaterialAnalysis EMPTY = new MaterialAnalysis(Map.of(), false);
    public static final Codec<MaterialAnalysis> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("progress").forGetter(MaterialAnalysis::progress),
            Codec.BOOL.optionalFieldOf("yihuo_initialized", false).forGetter(MaterialAnalysis::yihuoInitialized)
    ).apply(instance, MaterialAnalysis::new));
    public static final StreamCodec<ByteBuf, MaterialAnalysis> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public MaterialAnalysis {
        progress = Map.copyOf(progress);
    }

    public int progress(String itemId) {
        return progress.getOrDefault(itemId, 0);
    }

    public MaterialAnalysis withProgress(String itemId, int value) {
        Map<String, Integer> next = new HashMap<>(progress);
        next.put(itemId, value);
        return new MaterialAnalysis(next, yihuoInitialized);
    }

    public MaterialAnalysis withYihuoInitialized() {
        return new MaterialAnalysis(progress, true);
    }
}
