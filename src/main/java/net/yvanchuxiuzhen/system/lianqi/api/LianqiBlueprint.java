package net.yvanchuxiuzhen.system.lianqi.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.yvanchuxiuzhen.system.equipment.api.EquipmentRefinement;

/** 炼器图纸载体上的结构化配方数据组件（{@code yvanchuxiuzhen:lianqi_blueprint}）。 */
public record LianqiBlueprint(String name, String nameComponentJson, String description, String itemId,
                              String itemDescription, String itemDescriptionComponentJson, String type,
                              String levelText, int level, String sourceCarrierId, List<Ingredient> ingredients) {
    public static final LianqiBlueprint EMPTY = new LianqiBlueprint("", "", "", "", "", "", "", "", 0, "", List.of());
    public static final Codec<LianqiBlueprint> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("name", "").forGetter(LianqiBlueprint::name),
            Codec.STRING.optionalFieldOf("name_component", "").forGetter(LianqiBlueprint::nameComponentJson),
            Codec.STRING.optionalFieldOf("description", "").forGetter(LianqiBlueprint::description),
            Codec.STRING.optionalFieldOf("item_id", "").forGetter(LianqiBlueprint::itemId),
            Codec.STRING.optionalFieldOf("item_description", "").forGetter(LianqiBlueprint::itemDescription),
            Codec.STRING.optionalFieldOf("item_description_component", "").forGetter(LianqiBlueprint::itemDescriptionComponentJson),
            Codec.STRING.optionalFieldOf("type", "").forGetter(LianqiBlueprint::type),
            Codec.STRING.optionalFieldOf("level_text", "").forGetter(LianqiBlueprint::levelText),
            Codec.INT.optionalFieldOf("level", 0).forGetter(LianqiBlueprint::level),
            Codec.STRING.optionalFieldOf("source_carrier_id", "").forGetter(LianqiBlueprint::sourceCarrierId),
            Ingredient.CODEC.listOf().optionalFieldOf("ingredients", List.of()).forGetter(LianqiBlueprint::ingredients)
    ).apply(instance, LianqiBlueprint::new));
    public static final StreamCodec<ByteBuf, LianqiBlueprint> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public LianqiBlueprint {
        ingredients = List.copyOf(ingredients);
    }

    public Ingredient ingredient(int slot) {
        return slot >= 0 && slot < ingredients.size() ? ingredients.get(slot) : Ingredient.EMPTY;
    }

    public String displayDescription() {
        return description.isEmpty() ? itemDescription : description;
    }

    public boolean isPresent() {
        return !type.isEmpty() || !name.isEmpty();
    }

    /** 转为供装备炼制消费的强类型参数快照。 */
    public EquipmentRefinement toRefinement() {
        EquipmentRefinement.Mutable target = EquipmentRefinement.mutable();
        target.putString("tuzhiname", name);
        if (!nameComponentJson.isEmpty()) target.putString("tuzhiname_component", nameComponentJson);
        target.putString("fabaoid", itemId);
        target.putString("fabaoshuoming", itemDescription);
        if (!itemDescriptionComponentJson.isEmpty()) target.putString("fabaoshuoming_component", itemDescriptionComponentJson);
        target.putString("fabaotype", type);
        target.putString("item_level", levelText);
        target.putInt("item_level_num", level);
        if (!sourceCarrierId.isEmpty()) target.putString("belongToItemID", sourceCarrierId);
        for (int index = 0; index < ingredients.size(); index++) {
            Ingredient slot = ingredients.get(index);
            String key = "cailiao0" + index;
            if (!slot.id().isEmpty()) target.putString(key, slot.id());
            if (!slot.name().isEmpty()) target.putString(key + "name", slot.name());
            if (!slot.nameComponentJson().isEmpty()) target.putString(key + "name_component", slot.nameComponentJson());
            if (slot.count() > 0) target.putInt(key + "num", slot.count());
        }
        return target.toImmutable();
    }

    public record Ingredient(String id, String name, String nameComponentJson, int count) {
        public static final Ingredient EMPTY = new Ingredient("", "", "", 0);
        public static final Codec<Ingredient> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.optionalFieldOf("id", "").forGetter(Ingredient::id),
                Codec.STRING.optionalFieldOf("name", "").forGetter(Ingredient::name),
                Codec.STRING.optionalFieldOf("name_component", "").forGetter(Ingredient::nameComponentJson),
                Codec.INT.optionalFieldOf("count", 0).forGetter(Ingredient::count)
        ).apply(instance, Ingredient::new));
    }
}
