package net.yvanchuxiuzhen.system.creature.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.yvanchuxiuzhen.platform.content.ContentFiles;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;

/** 八种蝴蝶的实体工厂、属性与自然生成放置规则。 */
public final class Butterflies {
    private Butterflies() {
    }

    public static List<EntityModelRow> models() {
        return ContentFiles.read("content/creature/butterfly_models.json", EntityModelRow.CODEC);
    }

    public static void register(ContentTables content, IEventBus modBus) {
        for (EntityModelRow row : models()) {
            content.<ButterflyEntity>livingEntity(row.id(), (type, level) -> new ButterflyEntity(type, level, row),
                    () -> ButterflyEntity.attributes(row));
        }
        for (EggRow egg : ContentFiles.read("content/creature/butterfly_eggs.json", EggRow.CODEC)) {
            content.itemFactory("creature:spawn_egg_" + egg.id(), (properties, tooltip) -> new DeferredSpawnEggItem(
                    content.<ButterflyEntity>entityType(egg.id()), egg.baseColor(), egg.spotColor(), properties));
        }
        modBus.addListener((RegisterSpawnPlacementsEvent event) -> {
            for (EntityModelRow row : models()) {
                event.register(content.<ButterflyEntity>entityType(row.id()).get(), SpawnPlacementTypes.NO_RESTRICTIONS,
                        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
            }
        });
    }

    /** 来源刷怪蛋使用各注册身份独立的底色与斑点色。 */
    private record EggRow(String id, int baseColor, int spotColor) {
        private static final Codec<EggRow> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(EggRow::id),
                Codec.INT.fieldOf("base_color").forGetter(EggRow::baseColor),
                Codec.INT.fieldOf("spot_color").forGetter(EggRow::spotColor)
        ).apply(instance, EggRow::new));
    }
}
