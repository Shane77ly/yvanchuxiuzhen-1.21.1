package net.yvanchuxiuzhen.system.liandan.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** 丹方载体物品的强类型配方组件（{@code yvanchuxiuzhen:medicine_recipe}）。 */
public record MedicineRecipe(String name, String description, String medicineDescription,
                             String levelText, int level, String carrierId, List<Ingredient> ingredients) {
    public static final MedicineRecipe EMPTY = new MedicineRecipe("", "", "", "", 0, "", List.of());
    public static final Codec<MedicineRecipe> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("name", "").forGetter(MedicineRecipe::name),
            Codec.STRING.optionalFieldOf("description", "").forGetter(MedicineRecipe::description),
            Codec.STRING.optionalFieldOf("medicine_description", "").forGetter(MedicineRecipe::medicineDescription),
            Codec.STRING.optionalFieldOf("level_text", "").forGetter(MedicineRecipe::levelText),
            Codec.INT.optionalFieldOf("level", 0).forGetter(MedicineRecipe::level),
            Codec.STRING.optionalFieldOf("carrier_id", "").forGetter(MedicineRecipe::carrierId),
            Ingredient.CODEC.listOf().optionalFieldOf("ingredients", List.of()).forGetter(MedicineRecipe::ingredients)
    ).apply(instance, MedicineRecipe::new));
    public static final StreamCodec<ByteBuf, MedicineRecipe> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public MedicineRecipe {
        ingredients = List.copyOf(ingredients);
    }

    public Ingredient ingredient(int slotOneBased) {
        int index = slotOneBased - 1;
        return index >= 0 && index < ingredients.size() ? ingredients.get(index) : Ingredient.EMPTY;
    }

    public boolean isPresent() {
        return !name.isEmpty();
    }

    public record Ingredient(String id, String name) {
        public static final Ingredient EMPTY = new Ingredient("", "");
        public static final Codec<Ingredient> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.optionalFieldOf("id", "").forGetter(Ingredient::id),
                Codec.STRING.optionalFieldOf("name", "").forGetter(Ingredient::name)
        ).apply(instance, Ingredient::new));
    }
}
