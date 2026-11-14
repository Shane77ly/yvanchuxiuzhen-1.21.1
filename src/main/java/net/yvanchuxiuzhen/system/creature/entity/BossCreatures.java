package net.yvanchuxiuzhen.system.creature.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.yvanchuxiuzhen.platform.content.ContentFiles;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.creature.entity.boss.Boss01Entity;
import net.yvanchuxiuzhen.system.creature.entity.boss.Boss02Entity;
import net.yvanchuxiuzhen.system.creature.entity.boss.Boss0201Entity;
import net.yvanchuxiuzhen.system.creature.entity.boss.Boss04Entity;

/** 妖兽首领实体、技能子实体与来源刷怪蛋装配。 */
public final class BossCreatures {
    private BossCreatures() { }

    public static List<EntityModelRow> models() {
        return ContentFiles.read("content/creature/boss_models.json", EntityModelRow.CODEC);
    }

    public static void register(ContentTables content) {
        List<EntityModelRow> models = models();
        EntityModelRow axeModel = models.stream().filter(row -> row.id().equals("ycxzboss_0201")).findFirst().orElseThrow();
        for (EntityModelRow row : models) {
            switch (row.id()) {
                case "ycxzboss_01" -> content.<Boss01Entity>livingEntity(row.id(),
                        (type, level) -> new Boss01Entity(type, level, row), () -> Boss01Entity.attributes(row));
                case "ycxzboss_02" -> content.<Boss02Entity>livingEntity(row.id(),
                        (type, level) -> new Boss02Entity(type, level, row, axeModel), () -> Boss02Entity.attributes(row));
                case "ycxzboss_0201" -> content.<Boss0201Entity>livingEntity(row.id(),
                        (type, level) -> new Boss0201Entity(type, level, row), () -> Boss0201Entity.attributes(row));
                case "ycxzboss_04" -> content.<Boss04Entity>livingEntity(row.id(),
                        (type, level) -> new Boss04Entity(type, level, row), () -> Boss04Entity.attributes(row));
                default -> throw new IllegalStateException("未知首领身份：" + row.id());
            }
        }
        for (EggRow egg : ContentFiles.read("content/creature/boss_eggs.json", EggRow.CODEC)) {
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
