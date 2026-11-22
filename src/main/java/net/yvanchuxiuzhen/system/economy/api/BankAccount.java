package net.yvanchuxiuzhen.system.economy.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** 玩家银行的九级整型账户；负数、整数溢出与价值入账的截断沿用来源数值语义。 */
public record BankAccount(int lingshi01, int lingshi02, int lingshi03, int lingshi04, int lingshi05, int lingshi06, int lingshi07, int lingshi08, int lingshi09) {
    public static final BankAccount EMPTY = new BankAccount(0, 0, 0, 0, 0, 0, 0, 0, 0);
    public static final Codec<BankAccount> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("lingshi01", 0).forGetter(BankAccount::lingshi01),
            Codec.INT.optionalFieldOf("lingshi02", 0).forGetter(BankAccount::lingshi02),
            Codec.INT.optionalFieldOf("lingshi03", 0).forGetter(BankAccount::lingshi03),
            Codec.INT.optionalFieldOf("lingshi04", 0).forGetter(BankAccount::lingshi04),
            Codec.INT.optionalFieldOf("lingshi05", 0).forGetter(BankAccount::lingshi05),
            Codec.INT.optionalFieldOf("lingshi06", 0).forGetter(BankAccount::lingshi06),
            Codec.INT.optionalFieldOf("lingshi07", 0).forGetter(BankAccount::lingshi07),
            Codec.INT.optionalFieldOf("lingshi08", 0).forGetter(BankAccount::lingshi08),
            Codec.INT.optionalFieldOf("lingshi09", 0).forGetter(BankAccount::lingshi09)
    ).apply(instance, BankAccount::new));
    public static final StreamCodec<ByteBuf, BankAccount> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public int balance(int tier) {
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

    public BankAccount withBalance(int tier, int balance) {
        return switch (tier) {
            case 1 -> new BankAccount(balance, lingshi02, lingshi03, lingshi04, lingshi05, lingshi06, lingshi07, lingshi08, lingshi09);
            case 2 -> new BankAccount(lingshi01, balance, lingshi03, lingshi04, lingshi05, lingshi06, lingshi07, lingshi08, lingshi09);
            case 3 -> new BankAccount(lingshi01, lingshi02, balance, lingshi04, lingshi05, lingshi06, lingshi07, lingshi08, lingshi09);
            case 4 -> new BankAccount(lingshi01, lingshi02, lingshi03, balance, lingshi05, lingshi06, lingshi07, lingshi08, lingshi09);
            case 5 -> new BankAccount(lingshi01, lingshi02, lingshi03, lingshi04, balance, lingshi06, lingshi07, lingshi08, lingshi09);
            case 6 -> new BankAccount(lingshi01, lingshi02, lingshi03, lingshi04, lingshi05, balance, lingshi07, lingshi08, lingshi09);
            case 7 -> new BankAccount(lingshi01, lingshi02, lingshi03, lingshi04, lingshi05, lingshi06, balance, lingshi08, lingshi09);
            case 8 -> new BankAccount(lingshi01, lingshi02, lingshi03, lingshi04, lingshi05, lingshi06, lingshi07, balance, lingshi09);
            case 9 -> new BankAccount(lingshi01, lingshi02, lingshi03, lingshi04, lingshi05, lingshi06, lingshi07, lingshi08, balance);
            default -> throw new IllegalArgumentException("灵石等级必须为1至9：" + tier);
        };
    }

    public static int weight(int tier) {
        return switch (tier) {
            case 1 -> 1;
            case 2 -> 4;
            case 3 -> 16;
            case 4 -> 64;
            case 5 -> 256;
            case 6 -> 1024;
            case 7 -> 4096;
            case 8 -> 16384;
            case 9 -> 65536;
            default -> throw new IllegalArgumentException("灵石等级必须为1至9：" + tier);
        };
    }

    /** 来源先进行int乘法，再加到double，只计算正余额。 */
    public double purchasingPower() {
        double value = 0;
        for (int tier = 9; tier >= 1; tier--) {
            int amount = balance(tier);
            if (amount >= 1) value += amount * weight(tier);
        }
        return value;
    }

    /** 汇入的正价值从九级向二级floor拆分，一级余值至少为1才记入；旧余额不重新兑换。 */
    public BankAccount creditValue(double value) {
        if (!(value > 0)) return this;
        double[] deposits = new double[9];
        for (int tier = 9; tier >= 2; tier--) {
            int unit = weight(tier);
            if (value >= unit) {
                double count = Math.floor(value / unit);
                deposits[tier - 1] = count;
                value -= count * unit;
            }
        }
        if (value >= 1) deposits[0] = value;
        BankAccount result = this;
        for (int tier = 1; tier <= 9; tier++) {
            result = result.withBalance(tier, (int) (balance(tier) + deposits[tier - 1]));
        }
        return result;
    }
}
