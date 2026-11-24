package net.yvanchuxiuzhen.system.fulu.api;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** 尸体封存符携带的序列化尸体快照组件（{@code yvanchuxiuzhen:sealed_corpse}）。 */
public record SealedCorpse(CompoundTag corpse) {
    public static final SealedCorpse EMPTY = new SealedCorpse(new CompoundTag());
    public static final Codec<SealedCorpse> CODEC = CompoundTag.CODEC.xmap(SealedCorpse::new, SealedCorpse::corpse);
    public static final StreamCodec<ByteBuf, SealedCorpse> STREAM_CODEC = ByteBufCodecs.COMPOUND_TAG.map(SealedCorpse::new, SealedCorpse::corpse);

    public SealedCorpse {
        corpse = corpse.copy();
    }

    @Override
    public CompoundTag corpse() {
        return corpse.copy();
    }
}
