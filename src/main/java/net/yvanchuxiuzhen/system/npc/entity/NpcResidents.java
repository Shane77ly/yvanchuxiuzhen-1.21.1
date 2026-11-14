package net.yvanchuxiuzhen.system.npc.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.yvanchuxiuzhen.platform.content.ContentFiles;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;

/** 21至30号居民与来源刷怪蛋装配；居民实例不经过商人档案初始化。 */
public final class NpcResidents {
    private NpcResidents() { }

    public static List<EntityModelRow> models() {
        return ContentFiles.read("content/npc/resident_models.json", EntityModelRow.CODEC);
    }

    public static void register(ContentTables content) {
        for (EntityModelRow model : models()) {
            content.<NpcResidentAnimated>livingEntity(model.id(), (type, level) -> new NpcResidentAnimated(type, level, model), model::attributes);
        }
        for (int index = 23; index <= 30; index++) {
            content.<NpcResident>livingEntity("businessman_" + index, NpcResident::new, NpcResident::attributes);
        }
        for (EggRow egg : ContentFiles.read("content/npc/resident_eggs.json", EggRow.CODEC)) {
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
