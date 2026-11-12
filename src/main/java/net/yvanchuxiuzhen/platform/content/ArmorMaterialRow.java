package net.yvanchuxiuzhen.platform.content;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;

/**
 * 盔甲材质内容表的一行（{@code content/<单位>/armor_materials.json}）。defense 依次为靴子、护腿、胸甲、头盔；
 * texture 是 textures/models/armor 下 {@code <名>_layer_1.png} 与 {@code _layer_2.png} 的名称；animated 表示由专属动画渲染器提供纹理，不建立原版分层；
 * 来源的装备音效为空，对应 1.21.1 的空音效事件。
 */
public record ArmorMaterialRow(String id, List<Integer> defense, int enchantability, float toughness, float knockback,
                               Optional<List<String>> repair, String texture, boolean animated) {
    public static final Codec<ArmorMaterialRow> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(ArmorMaterialRow::id),
            Codec.INT.listOf().fieldOf("defense").forGetter(ArmorMaterialRow::defense),
            Codec.INT.fieldOf("enchantability").forGetter(ArmorMaterialRow::enchantability),
            Codec.FLOAT.fieldOf("toughness").forGetter(ArmorMaterialRow::toughness),
            Codec.FLOAT.fieldOf("knockback").forGetter(ArmorMaterialRow::knockback),
            Codec.STRING.listOf().optionalFieldOf("repair").forGetter(ArmorMaterialRow::repair),
            Codec.STRING.fieldOf("texture").forGetter(ArmorMaterialRow::texture),
            Codec.BOOL.optionalFieldOf("animated", false).forGetter(ArmorMaterialRow::animated)
    ).apply(instance, ArmorMaterialRow::new));
}
