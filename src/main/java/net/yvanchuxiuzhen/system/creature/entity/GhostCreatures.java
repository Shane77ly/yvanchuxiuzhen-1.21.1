package net.yvanchuxiuzhen.system.creature.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.yvanchuxiuzhen.platform.content.ContentFiles;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;

/** 鬼火与小尸王的实体、属性及来源刷怪蛋装配；来源未启用自然生成。 */
public final class GhostCreatures {
    private GhostCreatures() { }

    public static List<EntityModelRow> models() {
        return ContentFiles.read("content/creature/guiguai_models.json", EntityModelRow.CODEC);
    }

    public static void register(ContentTables content) {
        for (EntityModelRow row : models()) {
            switch (row.id()) {
                case "guiguai_02" -> content.<Guiguai02Entity>livingEntity(row.id(),
                        (type, level) -> new Guiguai02Entity(type, level, row), () -> Guiguai02Entity.attributes(row));
                case "guiguai_03" -> content.<Guiguai03Entity>livingEntity(row.id(),
                        (type, level) -> new Guiguai03Entity(type, level, row), () -> Guiguai03Entity.attributes(row));
                default -> throw new IllegalStateException("未知鬼怪身份：" + row.id());
            }
        }
        for (EggRow egg : ContentFiles.read("content/creature/guiguai_eggs.json", EggRow.CODEC)) {
            content.itemFactory("creature:spawn_egg_" + egg.id(), (properties, tooltip) -> new DeferredSpawnEggItem(
                    content.entityType(egg.id()), egg.baseColor(), egg.spotColor(), properties));
        }
    }

    private record EggRow(String id, int baseColor, int spotColor) {
        private static final Codec<EggRow> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(EggRow::id),
                Codec.INT.fieldOf("base_color").forGetter(EggRow::baseColor),
                Codec.INT.fieldOf("spot_color").forGetter(EggRow::spotColor)).apply(instance, EggRow::new));
    }
}
