package net.yvanchuxiuzhen.system.npc.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.yvanchuxiuzhen.platform.content.ContentFiles;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;

/** 阴司五位实体的独立属性、模型与刷怪蛋装配。 */
public final class NpcUnderworldCreatures {
    private NpcUnderworldCreatures() { }

    public static List<EntityModelRow> models() {
        return ContentFiles.read("content/npc/underworld_models.json", EntityModelRow.CODEC);
    }

    public static void register(ContentTables content) {
        for (EntityModelRow model : models()) {
            switch (model.id()) {
                case "yanwang" -> content.<NpcUnderworldJudge>livingEntity(model.id(),
                        (type, level) -> new NpcUnderworldJudge(type, level, model), model::attributes);
                case "wuchangbai", "wuchanghei", "niutou", "mamian" -> content.<NpcUnderworldGuard>livingEntity(model.id(),
                        (type, level) -> new NpcUnderworldGuard(type, level, model), model::attributes);
                default -> throw new IllegalStateException("未知阴司身份：" + model.id());
            }
        }
        for (EggRow egg : ContentFiles.read("content/npc/underworld_eggs.json", EggRow.CODEC)) {
            content.itemFactory("npc:spawn_egg_" + egg.id(), (properties, tooltip) -> new DeferredSpawnEggItem(
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
