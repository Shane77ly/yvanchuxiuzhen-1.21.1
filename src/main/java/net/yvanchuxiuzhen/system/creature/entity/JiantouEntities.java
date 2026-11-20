package net.yvanchuxiuzhen.system.creature.entity;

import net.yvanchuxiuzhen.platform.content.ContentFiles;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;

/** 箭头显示实体的来源模型、属性与实体工厂装配。 */
public final class JiantouEntities {
    private JiantouEntities() { }

    public static EntityModelRow model() {
        return ContentFiles.read("content/creature/jiantou_model.json", EntityModelRow.CODEC).getFirst();
    }

    public static void register(ContentTables content) {
        EntityModelRow row = model();
        content.<JiantouEntity>livingEntity(row.id(), (type, level) -> new JiantouEntity(type, level, row),
                () -> JiantouEntity.attributes(row));
    }
}
