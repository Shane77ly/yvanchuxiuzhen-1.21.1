package net.yvanchuxiuzhen.system.treasure.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** 宝匣物品承载的强类型配置指令组件（{@code yvanchuxiuzhen:casket_command}）。 */
public record CasketCommand(String command) {
    public static final CasketCommand EMPTY = new CasketCommand("");
    public static final Codec<CasketCommand> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("command", "").forGetter(CasketCommand::command)
    ).apply(instance, CasketCommand::new));
    public static final StreamCodec<ByteBuf, CasketCommand> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);
}
