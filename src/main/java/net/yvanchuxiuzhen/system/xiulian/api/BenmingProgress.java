package net.yvanchuxiuzhen.system.xiulian.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * equipment 提交给修炼系统的本命槽内物品的培养进度，供丹田界面显示（来源丹田界面直接读槽内物品的
 * {@code belongToMasterUUID}、{@code benminglevel}、{@code benmingexp}、{@code benmingexpmax}）。
 * {@code owned} 为该物品是否认主于本人。
 */
public record BenmingProgress(boolean owned, double level, double exp, double expMax) {
    public static final BenmingProgress NONE = new BenmingProgress(false, 0, 0, 0);
    public static final Codec<BenmingProgress> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("owned").forGetter(BenmingProgress::owned),
            Codec.DOUBLE.fieldOf("level").forGetter(BenmingProgress::level),
            Codec.DOUBLE.fieldOf("exp").forGetter(BenmingProgress::exp),
            Codec.DOUBLE.fieldOf("exp_max").forGetter(BenmingProgress::expMax)
    ).apply(instance, BenmingProgress::new));
}
