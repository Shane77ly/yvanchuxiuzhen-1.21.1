package net.yvanchuxiuzhen.system.material.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;

/** 来源 {@code lianqicailiaolist} 的材料模板，实例等级与品阶另存于物品组件。 */
public record MaterialDefinition(String itemId, Optional<Integer> fixedWeight, Optional<Integer> expValue,
                                 List<AttributeEntry> attributeEntries, List<SpecialEffectEntry> specialEffectEntries,
                                 List<String> booleanTraits) {
    private static final Codec<String> VALUE_CODEC = Codec.withAlternative(Codec.STRING,
            Codec.INT.xmap(String::valueOf, Integer::parseInt));

    public static final Codec<AttributeEntry> ATTRIBUTE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(AttributeEntry::name),
            VALUE_CODEC.fieldOf("value").forGetter(AttributeEntry::value)
    ).apply(instance, AttributeEntry::new));

    public static final Codec<SpecialEffectEntry> SPECIAL_EFFECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("namespace").forGetter(SpecialEffectEntry::namespace),
            Codec.STRING.fieldOf("name").forGetter(SpecialEffectEntry::name),
            VALUE_CODEC.fieldOf("value").forGetter(SpecialEffectEntry::value)
    ).apply(instance, SpecialEffectEntry::new));

    public static final Codec<MaterialDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("item_id").forGetter(MaterialDefinition::itemId),
            Codec.INT.optionalFieldOf("fixed_weight").forGetter(MaterialDefinition::fixedWeight),
            Codec.INT.optionalFieldOf("exp_value").forGetter(MaterialDefinition::expValue),
            ATTRIBUTE_CODEC.listOf().fieldOf("attribute_entries").forGetter(MaterialDefinition::attributeEntries),
            SPECIAL_EFFECT_CODEC.listOf().fieldOf("special_effect_entries").forGetter(MaterialDefinition::specialEffectEntries),
            Codec.STRING.listOf().fieldOf("boolean_traits").forGetter(MaterialDefinition::booleanTraits)
    ).apply(instance, MaterialDefinition::new));

    public MaterialDefinition {
        attributeEntries = List.copyOf(attributeEntries);
        specialEffectEntries = List.copyOf(specialEffectEntries);
        booleanTraits = List.copyOf(booleanTraits);
    }

    public ResourceLocation id() {
        return ResourceLocation.parse(itemId);
    }

    /** 当前结构化视图按属性条目后接特殊效果条目返回，重复条目保持各自列表中的顺序。 */
    public List<Entry> entries() {
        List<Entry> entries = new ArrayList<>(attributeEntries.size() + specialEffectEntries.size());
        for (AttributeEntry entry : attributeEntries) {
            entries.add(new Entry(Kind.ATTRIBUTE, "attribute", entry.name(), entry.value()));
        }
        for (SpecialEffectEntry entry : specialEffectEntries) {
            entries.add(new Entry(Kind.SPECIAL, entry.namespace(), entry.name(), entry.value()));
        }
        return List.copyOf(entries);
    }

    public int entryCount() {
        return attributeEntries.size() + specialEffectEntries.size();
    }

    public record AttributeEntry(String name, String value) {
    }

    public record SpecialEffectEntry(String namespace, String name, String value) {
    }

    public record Entry(Kind kind, String namespace, String name, String value) {
    }

    public enum Kind {
        ATTRIBUTE, SPECIAL
    }
}
