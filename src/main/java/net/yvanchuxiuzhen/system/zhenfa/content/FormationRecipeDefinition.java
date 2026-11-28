package net.yvanchuxiuzhen.system.zhenfa.content;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** 阵法配方定义，装载自 {@code content/zhenfa/formations.json}。 */
public record FormationRecipeDefinition(String code, String name, String category, double costMultiplier) {
    public static final Codec<FormationRecipeDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("code").forGetter(FormationRecipeDefinition::code),
            Codec.STRING.fieldOf("name").forGetter(FormationRecipeDefinition::name),
            Codec.STRING.fieldOf("category").forGetter(FormationRecipeDefinition::category),
            Codec.DOUBLE.fieldOf("cost_multiplier").forGetter(FormationRecipeDefinition::costMultiplier)
    ).apply(instance, FormationRecipeDefinition::new));
}
