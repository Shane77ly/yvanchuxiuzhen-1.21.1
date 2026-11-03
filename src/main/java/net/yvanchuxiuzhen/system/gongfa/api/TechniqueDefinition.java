package net.yvanchuxiuzhen.system.gongfa.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

/**
 * 一种功法的定义（来源功法书在 {@code Gongfalist01}–{@code 08} 或 {@code /technique} 命令中写入的固定字段）：
 * 书的物品、名字、品阶与文字、所需境界与文字、灵根要求（如“金”“阴水”“五”“无”）、简介、五行值（{@code gold}、{@code water}、
 * {@code tree}、{@code fire}、{@code soil}）与十三项属性档位（顺序同 {@link TechniqueStat}）。
 */
public record TechniqueDefinition(ResourceLocation item, String name, String gradeText, double grade, String levelText, double level,
                                  String body, String intro, Map<String, Double> elements, List<String> tiers) {
    public static final MapCodec<TechniqueDefinition> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("item").forGetter(TechniqueDefinition::item),
            Codec.STRING.fieldOf("name").forGetter(TechniqueDefinition::name),
            Codec.STRING.fieldOf("grade_text").forGetter(TechniqueDefinition::gradeText),
            Codec.DOUBLE.fieldOf("grade").forGetter(TechniqueDefinition::grade),
            Codec.STRING.fieldOf("level_text").forGetter(TechniqueDefinition::levelText),
            Codec.DOUBLE.fieldOf("level").forGetter(TechniqueDefinition::level),
            Codec.STRING.fieldOf("body").forGetter(TechniqueDefinition::body),
            Codec.STRING.fieldOf("intro").forGetter(TechniqueDefinition::intro),
            Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).optionalFieldOf("elements", Map.of()).forGetter(TechniqueDefinition::elements),
            Codec.STRING.listOf(TechniqueStat.values().length, TechniqueStat.values().length).fieldOf("tiers").forGetter(TechniqueDefinition::tiers)
    ).apply(instance, TechniqueDefinition::new));
    public static final Codec<TechniqueDefinition> CODEC = MAP_CODEC.codec();

    public String tier(TechniqueStat stat) {
        return tiers.get(stat.ordinal());
    }

    /** 五行值，{@code element} 为 {@code gold}、{@code water}、{@code tree}、{@code fire} 或 {@code soil}；未写的为 0。 */
    public double element(String element) {
        return elements.getOrDefault(element, 0.0);
    }
}
