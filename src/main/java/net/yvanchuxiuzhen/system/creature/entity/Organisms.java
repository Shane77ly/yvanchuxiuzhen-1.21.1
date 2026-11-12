package net.yvanchuxiuzhen.system.creature.entity;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.ContentFiles;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;

/** Organism09–12的独立实体工厂、刷怪蛋、属性与原生刷怪位置规则。 */
public final class Organisms {
    private Organisms() { }

    public static List<EntityModelRow> models() {
        return ContentFiles.read("content/creature/organism_models.json", EntityModelRow.CODEC);
    }

    public static void register(ContentTables content, IEventBus modBus) {
        for (EntityModelRow row : models()) {
            switch (row.id()) {
                case "organism_09" -> content.<Organism09Entity>livingEntity(row.id(),
                        (type, level) -> new Organism09Entity(type, level, row), () -> Organism09Entity.attributes(row));
                case "organism_10" -> content.<Organism10Entity>livingEntity(row.id(),
                        (type, level) -> new Organism10Entity(type, level, row), () -> Organism10Entity.attributes(row));
                case "organism_11" -> content.<Organism11Entity>livingEntity(row.id(),
                        (type, level) -> new Organism11Entity(type, level, row), () -> Organism11Entity.attributes(row));
                case "organism_12" -> content.<Organism12Entity>livingEntity(row.id(),
                        (type, level) -> new Organism12Entity(type, level, row), () -> Organism12Entity.attributes(row));
                default -> throw new IllegalStateException("未知环境动物身份：" + row.id());
            }
        }
        for (EggRow egg : ContentFiles.read("content/creature/organism_eggs.json", EggRow.CODEC)) {
            content.itemFactory("creature:spawn_egg_" + egg.id(), (properties, tooltip) -> new DeferredSpawnEggItem(
                    content.entityType(egg.id()), egg.baseColor(), egg.spotColor(), properties));
        }
        modBus.addListener((RegisterSpawnPlacementsEvent event) -> {
            event.register(content.<Organism10Entity>entityType("organism_10").get(), SpawnPlacementTypes.NO_RESTRICTIONS,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mob::checkMobSpawnRules, RegisterSpawnPlacementsEvent.Operation.REPLACE);
            event.register(content.<Organism11Entity>entityType("organism_11").get(), SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (type, level, reason, pos, random) ->
                            level.getBlockState(pos.below()).is(Blocks.GRASS_BLOCK) && level.getRawBrightness(pos, 0) > 8,
                    RegisterSpawnPlacementsEvent.Operation.REPLACE);
            event.register(content.<Organism12Entity>entityType("organism_12").get(), SpawnPlacementTypes.IN_WATER,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (type, level, reason, pos, random) ->
                            level.getBlockState(pos).is(Blocks.WATER) && level.getBlockState(pos.above()).is(Blocks.WATER),
                    RegisterSpawnPlacementsEvent.Operation.REPLACE);
        });
    }

    static SoundEvent ambient11() {
        return BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "organism11"));
    }

    private record EggRow(String id, int baseColor, int spotColor) {
        private static final Codec<EggRow> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(EggRow::id), Codec.INT.fieldOf("base_color").forGetter(EggRow::baseColor),
                Codec.INT.fieldOf("spot_color").forGetter(EggRow::spotColor)).apply(instance, EggRow::new));
    }
}
