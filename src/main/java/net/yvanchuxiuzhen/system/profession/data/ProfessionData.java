package net.yvanchuxiuzhen.system.profession.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.yvanchuxiuzhen.system.profession.api.Profession;

/** 全部副职业进度的唯一存档真值，附件只向玩家本人同步。 */
public final class ProfessionData {
    public static final Codec<ProfessionData> CODEC = Codec.unboundedMap(Profession.CODEC, ProfessionProgress.CODEC)
            .comapFlatMap(values -> values.size() == Profession.values().length
                    ? DataResult.success(new ProfessionData(values))
                    : DataResult.error(() -> "副职业存档必须包含全部十种职业"), data -> data.progress);
    public static final StreamCodec<RegistryFriendlyByteBuf, ProfessionData> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    private final Map<Profession, ProfessionProgress> progress;

    public ProfessionData() {
        progress = new EnumMap<>(Profession.class);
        for (Profession profession : Profession.values()) {
            progress.put(profession, ProfessionProgress.initial());
        }
    }

    private ProfessionData(Map<Profession, ProfessionProgress> values) {
        progress = new EnumMap<>(values);
    }

    public ProfessionProgress get(Profession profession) {
        return progress.get(profession);
    }

    public void set(Profession profession, ProfessionProgress value) {
        progress.put(profession, value);
    }
}
