package net.yvanchuxiuzhen.system.fulu;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.platform.content.ContentFiles;

/** 来源画符表的有序配方；符头、符胆以稳定编号匹配，首个匹配决定产物和耗墨。 */
public final class DrawingRecipes {
    private static final List<Recipe> RECIPES = ContentFiles.read("content/fulu/drawing_recipes.json", Recipe.CODEC);

    private DrawingRecipes() {
    }

    public static Optional<Recipe> find(int head, int core) {
        return RECIPES.stream().filter(recipe -> recipe.head() == head && recipe.core() == core).findFirst();
    }

    public static Component name(ResourceLocation item) {
        return Component.translatable("gui.yvanchuxiuzhen.huafu_gui.recipe." + item.getPath());
    }

    public record Recipe(int head, int core, ResourceLocation item, int ink) {
        private static final Codec<Recipe> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.intRange(1, 11).fieldOf("head").forGetter(Recipe::head),
                Codec.intRange(1, 8).fieldOf("core").forGetter(Recipe::core),
                ResourceLocation.CODEC.fieldOf("item").forGetter(Recipe::item),
                Codec.intRange(1, 64).fieldOf("ink").forGetter(Recipe::ink)
        ).apply(instance, Recipe::new));
    }
}
