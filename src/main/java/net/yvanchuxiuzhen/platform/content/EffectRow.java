package net.yvanchuxiuzhen.platform.content;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * 状态效果内容表的一行（{@code content/<单位>/effects.json}）：效果 ID、类别（{@code MobEffectCategory} 的名称）与颜色。
 * 效果的行为由所属系统用 {@link ContentTables#effectBehavior} 登记，没有登记的效果只是标记，供其他过程读取等级。
 */
public record EffectRow(String id, String category, int color) {
    public static final Codec<EffectRow> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(EffectRow::id),
            Codec.STRING.fieldOf("category").forGetter(EffectRow::category),
            Codec.INT.fieldOf("color").forGetter(EffectRow::color)
    ).apply(instance, EffectRow::new));
}
