package net.yvanchuxiuzhen.system.cooking.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** 菜谱载体物品的结构化数据组件（{@code yvanchuxiuzhen:cooking_recipe}）。 */
public record CookingRecipeData(String menuName, double grade, String gradeText, String description, int animation,
                                String neededContainer, String outputId, String potType, List<String> ingredients) {
    public static final CookingRecipeData EMPTY = new CookingRecipeData("", 0.0, "", "", 0, "", "", "", List.of());
    public static final Codec<CookingRecipeData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("menu_name", "").forGetter(CookingRecipeData::menuName),
            Codec.DOUBLE.optionalFieldOf("grade", 0.0).forGetter(CookingRecipeData::grade),
            Codec.STRING.optionalFieldOf("grade_text", "").forGetter(CookingRecipeData::gradeText),
            Codec.STRING.optionalFieldOf("description", "").forGetter(CookingRecipeData::description),
            Codec.INT.optionalFieldOf("animation", 0).forGetter(CookingRecipeData::animation),
            Codec.STRING.optionalFieldOf("needed_container", "").forGetter(CookingRecipeData::neededContainer),
            Codec.STRING.optionalFieldOf("output_id", "").forGetter(CookingRecipeData::outputId),
            Codec.STRING.optionalFieldOf("pot_type", "").forGetter(CookingRecipeData::potType),
            Codec.STRING.listOf().optionalFieldOf("ingredients", List.of()).forGetter(CookingRecipeData::ingredients)
    ).apply(instance, CookingRecipeData::new));
    public static final StreamCodec<ByteBuf, CookingRecipeData> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public CookingRecipeData {
        ingredients = List.copyOf(ingredients);
    }

    public String ingredient(int slot) {
        return slot >= 0 && slot < ingredients.size() ? ingredients.get(slot) : "";
    }
}
