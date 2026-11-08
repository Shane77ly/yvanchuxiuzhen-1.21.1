package net.yvanchuxiuzhen.system.profession.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** 单种职业的来源三个独立数值；等级和经验均保留 double，不在存档或写入时取整、截断。 */
public record ProfessionProgress(double level, double experience, double experienceMax) {
    public static final Codec<ProfessionProgress> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.DOUBLE.fieldOf("level").forGetter(ProfessionProgress::level),
            Codec.DOUBLE.fieldOf("experience").forGetter(ProfessionProgress::experience),
            Codec.DOUBLE.fieldOf("experience_max").forGetter(ProfessionProgress::experienceMax)
    ).apply(instance, ProfessionProgress::new));

    public static ProfessionProgress initial() {
        return new ProfessionProgress(0, 0, 100);
    }
}
