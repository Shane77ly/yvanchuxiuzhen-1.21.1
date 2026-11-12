package net.yvanchuxiuzhen.system.equipment.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** 覆火葫芦保存的十档火焰数量；组件取代来源物品上的 {@code fire1num}–{@code fire10num} 标签。 */
public record FabaoFireData(double fire1, double fire2, double fire3, double fire4, double fire5,
                            double fire6, double fire7, double fire8, double fire9, double fire10) {
    public static final FabaoFireData EMPTY = new FabaoFireData(0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
    public static final Codec<FabaoFireData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("fire1").forGetter(FabaoFireData::fire1),
            Codec.DOUBLE.fieldOf("fire2").forGetter(FabaoFireData::fire2),
            Codec.DOUBLE.fieldOf("fire3").forGetter(FabaoFireData::fire3),
            Codec.DOUBLE.fieldOf("fire4").forGetter(FabaoFireData::fire4),
            Codec.DOUBLE.fieldOf("fire5").forGetter(FabaoFireData::fire5),
            Codec.DOUBLE.fieldOf("fire6").forGetter(FabaoFireData::fire6),
            Codec.DOUBLE.fieldOf("fire7").forGetter(FabaoFireData::fire7),
            Codec.DOUBLE.fieldOf("fire8").forGetter(FabaoFireData::fire8),
            Codec.DOUBLE.fieldOf("fire9").forGetter(FabaoFireData::fire9),
            Codec.DOUBLE.fieldOf("fire10").forGetter(FabaoFireData::fire10)
    ).apply(instance, FabaoFireData::new));

    public double get(int grade) {
        return switch (grade) {
            case 1 -> fire1;
            case 2 -> fire2;
            case 3 -> fire3;
            case 4 -> fire4;
            case 5 -> fire5;
            case 6 -> fire6;
            case 7 -> fire7;
            case 8 -> fire8;
            case 9 -> fire9;
            case 10 -> fire10;
            default -> 0;
        };
    }

    public FabaoFireData add(int grade, double amount) {
        return with(grade, get(grade) + amount);
    }

    public FabaoFireData with(int grade, double amount) {
        return switch (grade) {
            case 1 -> new FabaoFireData(amount, fire2, fire3, fire4, fire5, fire6, fire7, fire8, fire9, fire10);
            case 2 -> new FabaoFireData(fire1, amount, fire3, fire4, fire5, fire6, fire7, fire8, fire9, fire10);
            case 3 -> new FabaoFireData(fire1, fire2, amount, fire4, fire5, fire6, fire7, fire8, fire9, fire10);
            case 4 -> new FabaoFireData(fire1, fire2, fire3, amount, fire5, fire6, fire7, fire8, fire9, fire10);
            case 5 -> new FabaoFireData(fire1, fire2, fire3, fire4, amount, fire6, fire7, fire8, fire9, fire10);
            case 6 -> new FabaoFireData(fire1, fire2, fire3, fire4, fire5, amount, fire7, fire8, fire9, fire10);
            case 7 -> new FabaoFireData(fire1, fire2, fire3, fire4, fire5, fire6, amount, fire8, fire9, fire10);
            case 8 -> new FabaoFireData(fire1, fire2, fire3, fire4, fire5, fire6, fire7, amount, fire9, fire10);
            case 9 -> new FabaoFireData(fire1, fire2, fire3, fire4, fire5, fire6, fire7, fire8, amount, fire10);
            case 10 -> new FabaoFireData(fire1, fire2, fire3, fire4, fire5, fire6, fire7, fire8, fire9, amount);
            default -> this;
        };
    }
}
