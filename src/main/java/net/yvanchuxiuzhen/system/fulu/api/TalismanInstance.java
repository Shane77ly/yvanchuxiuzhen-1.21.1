package net.yvanchuxiuzhen.system.fulu.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** 成品符箓上的品阶、提示名、确认计数等实例数据（{@code yvanchuxiuzhen:talisman}）。 */
public record TalismanInstance(int level, String levelText, int confirmations, int bonusCount) {
    public static final TalismanInstance EMPTY = new TalismanInstance(0, "", 0, 0);
    public static final Codec<TalismanInstance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("level", 0).forGetter(TalismanInstance::level),
            Codec.STRING.optionalFieldOf("level_text", "").forGetter(TalismanInstance::levelText),
            Codec.INT.optionalFieldOf("confirmations", 0).forGetter(TalismanInstance::confirmations),
            Codec.INT.optionalFieldOf("bonus_count", 0).forGetter(TalismanInstance::bonusCount)
    ).apply(instance, TalismanInstance::new));
    public static final StreamCodec<ByteBuf, TalismanInstance> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public double effectiveGrade() {
        return level < 1 ? 1.0 : level;
    }

    public TalismanInstance withConfirmations(int newConfirmations) {
        return new TalismanInstance(level, levelText, newConfirmations, bonusCount);
    }
}
