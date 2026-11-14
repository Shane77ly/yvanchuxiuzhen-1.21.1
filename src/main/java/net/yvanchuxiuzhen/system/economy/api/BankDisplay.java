package net.yvanchuxiuzhen.system.economy.api;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** 银行菜单的本人只读显示投影；账户真值由银行账户附件持有。 */
public record BankDisplay(String master, BankAccount amounts, boolean ready) {
    public static final BankDisplay EMPTY = new BankDisplay("", BankAccount.EMPTY, false);
    public static final StreamCodec<ByteBuf, BankDisplay> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, BankDisplay::master,
            BankAccount.STREAM_CODEC, BankDisplay::amounts,
            ByteBufCodecs.BOOL, BankDisplay::ready,
            BankDisplay::new);
}
