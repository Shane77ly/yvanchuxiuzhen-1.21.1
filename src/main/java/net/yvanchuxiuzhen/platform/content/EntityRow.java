package net.yvanchuxiuzhen.platform.content;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * 实体类型内容表的一行（{@code content/<单位>/entities.json}）：实体 ID、生物类别（{@code MobCategory} 的名称）、
 * 碰撞箱宽高、跟踪范围（格）、更新间隔（刻）、是否免疫火焰（来源的“是否接收速度更新”在 1.21.1 由实体类型自行决定，不再登记）。实体类与属性由所属系统用
 * {@link ContentTables#entity} 登记。
 */
public record EntityRow(String id, String category, float width, float height, int trackingRange, int updateInterval,
        boolean fireImmune) {
    public static final Codec<EntityRow> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(EntityRow::id),
            Codec.STRING.fieldOf("category").forGetter(EntityRow::category),
            Codec.FLOAT.fieldOf("width").forGetter(EntityRow::width),
            Codec.FLOAT.fieldOf("height").forGetter(EntityRow::height),
            Codec.INT.fieldOf("tracking_range").forGetter(EntityRow::trackingRange),
            Codec.INT.fieldOf("update_interval").forGetter(EntityRow::updateInterval),
            Codec.BOOL.fieldOf("fire_immune").forGetter(EntityRow::fireImmune)
    ).apply(instance, EntityRow::new));
}
