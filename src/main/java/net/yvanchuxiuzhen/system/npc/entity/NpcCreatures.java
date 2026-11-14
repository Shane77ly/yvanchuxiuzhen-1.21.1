package net.yvanchuxiuzhen.system.npc.entity;

import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.yvanchuxiuzhen.platform.content.ContentFiles;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;

/** 交易商与大管家的来源注册、模型和实例初始化配置。 */
public final class NpcCreatures {
    private NpcCreatures() { }

    public static List<EntityModelRow> models() {
        return ContentFiles.read("content/npc/entity_models.json", EntityModelRow.CODEC);
    }

    public static void register(ContentTables content) {
        List<CompoundTag> profiles = ContentFiles.read("content/npc/spawn_data.json", CompoundTag.CODEC);
        for (EntityModelRow model : models()) {
            CompoundTag profile = profiles.stream().filter(value -> value.getString("id").equals(model.id())).findFirst().orElseThrow();
            content.<NpcAnimated>livingEntity(model.id(), (type, level) -> new NpcAnimated(type, level, model, profile), model::attributes);
        }
        for (String id : new String[]{"businessman_09", "businessman_10", "businessman_11"}) {
            CompoundTag profile = profiles.stream().filter(value -> value.getString("id").equals(id)).findFirst().orElseThrow();
            content.<NpcMob>livingEntity(id, (type, level) -> new NpcMob(type, level, profile), NpcMob::attributes);
        }
    }
}
