package net.yvanchuxiuzhen.system.economy.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** 通宝玉自身的认主与double余额；物品余额不等同于玩家银行账户。 */
public record BankCard(String masterUUID, double lingshi01, double lingshi02, double lingshi03, double lingshi04, double lingshi05, double lingshi06, double lingshi07, double lingshi08, double lingshi09) {
    public static final BankCard EMPTY = new BankCard("", 0, 0, 0, 0, 0, 0, 0, 0, 0);
    public static final Codec<BankCard> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("belongToMasterUUID", "").forGetter(BankCard::masterUUID),
            Codec.DOUBLE.optionalFieldOf("lingshi01", 0.0).forGetter(BankCard::lingshi01),
            Codec.DOUBLE.optionalFieldOf("lingshi02", 0.0).forGetter(BankCard::lingshi02),
            Codec.DOUBLE.optionalFieldOf("lingshi03", 0.0).forGetter(BankCard::lingshi03),
            Codec.DOUBLE.optionalFieldOf("lingshi04", 0.0).forGetter(BankCard::lingshi04),
            Codec.DOUBLE.optionalFieldOf("lingshi05", 0.0).forGetter(BankCard::lingshi05),
            Codec.DOUBLE.optionalFieldOf("lingshi06", 0.0).forGetter(BankCard::lingshi06),
            Codec.DOUBLE.optionalFieldOf("lingshi07", 0.0).forGetter(BankCard::lingshi07),
            Codec.DOUBLE.optionalFieldOf("lingshi08", 0.0).forGetter(BankCard::lingshi08),
            Codec.DOUBLE.optionalFieldOf("lingshi09", 0.0).forGetter(BankCard::lingshi09)
    ).apply(instance, BankCard::new));
    public static final StreamCodec<ByteBuf, BankCard> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public BankCard withMaster(String uuid) {
        return new BankCard(uuid, lingshi01, lingshi02, lingshi03, lingshi04, lingshi05, lingshi06, lingshi07, lingshi08, lingshi09);
    }

    public double balance(int tier) {
        return switch (tier) {
            case 1 -> lingshi01;
            case 2 -> lingshi02;
            case 3 -> lingshi03;
            case 4 -> lingshi04;
            case 5 -> lingshi05;
            case 6 -> lingshi06;
            case 7 -> lingshi07;
            case 8 -> lingshi08;
            case 9 -> lingshi09;
            default -> throw new IllegalArgumentException("灵石等级必须为1至9：" + tier);
        };
    }
}
