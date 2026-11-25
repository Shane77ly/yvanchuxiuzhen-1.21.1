package net.yvanchuxiuzhen.system.quest.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** 高级通宝玉唯一银行动作的会话；模板 ID、游标与启动标记属于任务系统。 */
public record BankDialogueSession(String script, int cursor, boolean begin) {
    public static final String SCRIPT = "bank_dialogue";
    public static final BankDialogueSession EMPTY = new BankDialogueSession("", 0, false);
    public static final Codec<BankDialogueSession> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("renwulist", "").forGetter(BankDialogueSession::script),
            Codec.INT.optionalFieldOf("renwuweizhi", 0).forGetter(BankDialogueSession::cursor),
            Codec.BOOL.optionalFieldOf("renwubegin", false).forGetter(BankDialogueSession::begin)
    ).apply(instance, BankDialogueSession::new));
    public static final StreamCodec<ByteBuf, BankDialogueSession> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);
}
