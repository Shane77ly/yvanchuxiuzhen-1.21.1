package net.yvanchuxiuzhen.system.equipment.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** 本命器物的等级、当前经验与当前等级门槛，随物品实例保存。 */
public record BenmingData(int level, double experience, double experienceMax) {
    public static final BenmingData EMPTY = new BenmingData(0, 0, 0);
    public static final Codec<BenmingData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("level").forGetter(BenmingData::level),
            Codec.DOUBLE.fieldOf("experience").forGetter(BenmingData::experience),
            Codec.DOUBLE.fieldOf("experience_max").forGetter(BenmingData::experienceMax)
    ).apply(instance, BenmingData::new));

    public static BenmingData initial() {
        return new BenmingData(0, 0, 2000);
    }
}
