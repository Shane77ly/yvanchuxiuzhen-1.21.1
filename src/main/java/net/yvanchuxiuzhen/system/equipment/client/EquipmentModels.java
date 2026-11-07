package net.yvanchuxiuzhen.system.equipment.client;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.yvanchuxiuzhen.platform.content.ContentFiles;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;

/** 装备动画实体的模型表缓存。 */
final class EquipmentModels {
    private static final Map<String, EntityModelRow> MODELS = ContentFiles.read("content/equipment/entity_models.json", EntityModelRow.CODEC).stream()
            .collect(Collectors.toUnmodifiableMap(EntityModelRow::id, Function.identity()));

    private EquipmentModels() {
    }

    static EntityModelRow model(String id) {
        return MODELS.get(id);
    }
}
