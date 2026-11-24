package net.yvanchuxiuzhen.system.lianqi.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

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

    /** 供仍在过渡中的装备炼制接口消费的瞬时配方参数标签（不挂载至物品 CUSTOM_DATA）。 */
    public CompoundTag toRecipeTag() {
        CompoundTag tag = new CompoundTag();
        tag.putString("tuzhiname", name);
        if (!nameComponentJson.isEmpty()) tag.putString("tuzhiname_component", nameComponentJson);
        tag.putString("fabaoid", itemId);
        tag.putString("fabaoshuoming", itemDescription);
        if (!itemDescriptionComponentJson.isEmpty()) tag.putString("fabaoshuoming_component", itemDescriptionComponentJson);
        tag.putString("fabaotype", type);
        tag.putString("item_level", levelText);
        tag.putInt("item_level_num", level);
        if (!sourceCarrierId.isEmpty()) tag.putString("belongToItemID", sourceCarrierId);
        for (int index = 0; index < ingredients.size(); index++) {
            Ingredient slot = ingredients.get(index);
            String key = "cailiao0" + index;
            if (!slot.id().isEmpty()) tag.putString(key, slot.id());
            if (!slot.name().isEmpty()) tag.putString(key + "name", slot.name());
            if (!slot.nameComponentJson().isEmpty()) tag.putString(key + "name_component", slot.nameComponentJson());
            if (slot.count() > 0) tag.putInt(key + "num", slot.count());
        }
        return tag;
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
