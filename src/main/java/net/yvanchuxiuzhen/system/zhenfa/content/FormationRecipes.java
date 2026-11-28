package net.yvanchuxiuzhen.system.zhenfa.content;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.yvanchuxiuzhen.platform.content.ContentFiles;

/** 由 {@code content/zhenfa/formations.json} 驱动的阵法配方注册表。 */
public final class FormationRecipes {
    public static final String RESOURCE_PATH = "content/zhenfa/formations.json";
    private static Map<String, FormationRecipeDefinition> byCode;

    private FormationRecipes() {
    }

    public static List<FormationRecipeDefinition> all() {
        return List.copyOf(index().values());
    }

    public static Optional<FormationRecipeDefinition> match(String code) {
        return Optional.ofNullable(index().get(code));
    }

    private static Map<String, FormationRecipeDefinition> index() {
        if (byCode == null) {
            Map<String, FormationRecipeDefinition> loaded = new LinkedHashMap<>();
            for (FormationRecipeDefinition recipe : ContentFiles.read(RESOURCE_PATH, FormationRecipeDefinition.CODEC)) {
                loaded.put(recipe.code(), recipe);
            }
            byCode = Map.copyOf(loaded);
        }
        return byCode;
    }
}
