package net.yvanchuxiuzhen.system.skill.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;

/**
 * 技能动画实体的表现与属性（{@code content/skill/entity_models.json}，由 {@code extract_entity_models.py} 从来源的
 * Model、Layer、Renderer 与 Entity 类提取）：GeckoLib 几何与动画文件名（不含后缀）、阴影半径、默认纹理、头部骨骼、发光层纹理、
 * 默认循环动画与离地时的循环动画，以及生物属性（移速、生命、护甲、攻击、跟踪范围）和是否无 AI。
 */
public record EntityModelRow(String id, String geo, String animation, float shadow, String texture, Optional<String> head, Optional<String> glow, String idle,
                             Optional<String> air, double speed, double health, double armor, double attack, double follow, boolean noAi) {
    public static final Codec<EntityModelRow> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("id").forGetter(EntityModelRow::id),
            Codec.STRING.fieldOf("geo").forGetter(EntityModelRow::geo),
            Codec.STRING.fieldOf("animation").forGetter(EntityModelRow::animation),
            Codec.FLOAT.fieldOf("shadow").forGetter(EntityModelRow::shadow),
            Codec.STRING.fieldOf("texture").forGetter(EntityModelRow::texture),
            Codec.STRING.optionalFieldOf("head").forGetter(EntityModelRow::head),
            Codec.STRING.optionalFieldOf("glow").forGetter(EntityModelRow::glow),
            Codec.STRING.fieldOf("idle").forGetter(EntityModelRow::idle),
            Codec.STRING.optionalFieldOf("air").forGetter(EntityModelRow::air),
            Codec.DOUBLE.fieldOf("speed").forGetter(EntityModelRow::speed),
            Codec.DOUBLE.fieldOf("health").forGetter(EntityModelRow::health),
            Codec.DOUBLE.fieldOf("armor").forGetter(EntityModelRow::armor),
            Codec.DOUBLE.fieldOf("attack").forGetter(EntityModelRow::attack),
            Codec.DOUBLE.fieldOf("follow").forGetter(EntityModelRow::follow),
            Codec.BOOL.fieldOf("no_ai").forGetter(EntityModelRow::noAi)
    ).apply(instance, EntityModelRow::new));
}
