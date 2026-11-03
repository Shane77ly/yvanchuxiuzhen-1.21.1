package net.yvanchuxiuzhen.system.xiulian.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * gongfa 提交给修炼系统的主修功法信息（来源 xiulian 直接读功法物品的 {@code ExercisesName}、{@code belongTopinjie}、
 * {@code ExercisesLevel} 与 {@code belongToMasterUUID}）。{@code present} 为功法栏是否有书，{@code owned} 为是否本人习得。
 */
public record MainTechnique(boolean present, boolean owned, String name, double grade, double level) {
    public static final MainTechnique NONE = new MainTechnique(false, false, "", 0, 0);
    public static final Codec<MainTechnique> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("present").forGetter(MainTechnique::present),
            Codec.BOOL.fieldOf("owned").forGetter(MainTechnique::owned),
            Codec.STRING.fieldOf("name").forGetter(MainTechnique::name),
            Codec.DOUBLE.fieldOf("grade").forGetter(MainTechnique::grade),
            Codec.DOUBLE.fieldOf("level").forGetter(MainTechnique::level)
    ).apply(instance, MainTechnique::new));
}
