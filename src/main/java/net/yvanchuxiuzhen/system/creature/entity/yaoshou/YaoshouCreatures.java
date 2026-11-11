package net.yvanchuxiuzhen.system.creature.entity.yaoshou;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.ContentFiles;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;

/** 已接回行为链的编号妖兽实体、属性、来源出生条件与刷怪蛋注册。 */
public final class YaoshouCreatures {
    private static final Set<String> INTEGRATED = Set.of("yaoshou_001", "yaoshou_002", "yaoshou_003", "yaoshou_004", "yaoshou_005", "yaoshou_006", "yaoshou_007", "yaoshou_008", "yaoshou_009", "yaoshou_010", "yaoshou_018", "yaoshou_019", "yaoshou_020", "yaoshou_021", "yaoshou_022", "yaoshou_025", "yaoshou_027", "yaoshou_029", "yaoshou_030", "yaoshou_038", "yaoshou_041", "yaoshou_042", "yaoshou_043", "yaoshou_045", "yaoshou_049", "yaoshou_052");
    private static final TagKey<Block> GRASS_MATERIAL = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "legacy_grass_material"));

    private YaoshouCreatures() { }

    public static List<EntityModelRow> models() {
        return Stream.of("001_002", "003_018", "019_036", "037_054")
                .flatMap(batch -> ContentFiles.read("content/creature/yaoshou_" + batch + "_models.json", EntityModelRow.CODEC).stream())
                .filter(row -> INTEGRATED.contains(row.id())).toList();
    }

    public static void register(ContentTables content, IEventBus modBus) {
        YaoshouAttributes.register(modBus);
        for (EntityModelRow row : models()) {
            switch (row.id()) {
                case "yaoshou_001" -> content.<Yaoshou001Entity>livingEntity(row.id(),
                        (type, level) -> new Yaoshou001Entity(type, level, row), () -> Yaoshou001Entity.attributes(row));
                case "yaoshou_002" -> content.<Yaoshou002Entity>livingEntity(row.id(),
                        (type, level) -> new Yaoshou002Entity(type, level, row), () -> Yaoshou002Entity.attributes(row));
                case "yaoshou_019" -> content.<Yaoshou019Entity>livingEntity(row.id(),
                        (type, level) -> new Yaoshou019Entity(type, level, row), () -> Yaoshou019Entity.attributes(row));
                case "yaoshou_020" -> content.<Yaoshou020Entity>livingEntity(row.id(),
                        (type, level) -> new Yaoshou020Entity(type, level, row), () -> Yaoshou020Entity.attributes(row));
                case "yaoshou_030" -> content.<Yaoshou030Entity>livingEntity(row.id(),
                        (type, level) -> new Yaoshou030Entity(type, level, row), () -> Yaoshou030Entity.attributes(row));
                case "yaoshou_038" -> content.<Yaoshou038Entity>livingEntity(row.id(),
                        (type, level) -> new Yaoshou038Entity(type, level, row), () -> Yaoshou038Entity.attributes(row));
                case "yaoshou_041" -> content.<Yaoshou041Entity>livingEntity(row.id(),
                        (type, level) -> new Yaoshou041Entity(type, level, row), () -> Yaoshou041Entity.attributes(row));
                case "yaoshou_042" -> content.<Yaoshou042Entity>livingEntity(row.id(),
                        (type, level) -> new Yaoshou042Entity(type, level, row), () -> Yaoshou042Entity.attributes(row));
                case "yaoshou_043" -> content.<Yaoshou043Entity>livingEntity(row.id(),
                        (type, level) -> new Yaoshou043Entity(type, level, row), () -> Yaoshou043Entity.attributes(row));
                case "yaoshou_045" -> content.<Yaoshou045Entity>livingEntity(row.id(),
                        (type, level) -> new Yaoshou045Entity(type, level, row), () -> Yaoshou045Entity.attributes(row));
                case "yaoshou_049" -> content.<Yaoshou049Entity>livingEntity(row.id(),
                        (type, level) -> new Yaoshou049Entity(type, level, row), () -> Yaoshou049Entity.attributes(row));
                case "yaoshou_052" -> content.<Yaoshou052Entity>livingEntity(row.id(),
                        (type, level) -> new Yaoshou052Entity(type, level, row), () -> Yaoshou052Entity.attributes(row));
                case "yaoshou_003" -> content.<Yaoshou003Entity>livingEntity(row.id(),
                        (type, level) -> new Yaoshou003Entity(type, level, row), () -> Yaoshou003Entity.attributes(row));
                case "yaoshou_004" -> content.<Yaoshou004Entity>livingEntity(row.id(),
                        (type, level) -> new Yaoshou004Entity(type, level, row), () -> Yaoshou004Entity.attributes(row));
                case "yaoshou_005" -> content.<Yaoshou005Entity>livingEntity(row.id(),
                        (type, level) -> new Yaoshou005Entity(type, level, row), () -> Yaoshou005Entity.attributes(row));
                case "yaoshou_006" -> content.<Yaoshou006Entity>livingEntity(row.id(),
                        (type, level) -> new Yaoshou006Entity(type, level, row), () -> Yaoshou006Entity.attributes(row));
                case "yaoshou_007" -> content.<Yaoshou007Entity>livingEntity(row.id(),
                        (type, level) -> new Yaoshou007Entity(type, level, row), () -> Yaoshou007Entity.attributes(row));
                case "yaoshou_008" -> content.<Yaoshou008Entity>livingEntity(row.id(),
                        (type, level) -> new Yaoshou008Entity(type, level, row), () -> Yaoshou008Entity.attributes(row));
                case "yaoshou_009" -> content.<Yaoshou009Entity>livingEntity(row.id(),
                        (type, level) -> new Yaoshou009Entity(type, level, row), () -> Yaoshou009Entity.attributes(row));
                case "yaoshou_010" -> content.<Yaoshou010Entity>livingEntity(row.id(),
                        (type, level) -> new Yaoshou010Entity(type, level, row), () -> Yaoshou010Entity.attributes(row));
                case "yaoshou_018" -> content.<Yaoshou018Entity>livingEntity(row.id(),
                        (type, level) -> new Yaoshou018Entity(type, level, row), () -> Yaoshou018Entity.attributes(row));
                case "yaoshou_021" -> content.<Yaoshou021Entity>livingEntity(row.id(),
                        (type, level) -> new Yaoshou021Entity(type, level, row), () -> Yaoshou021Entity.attributes(row));
                case "yaoshou_022" -> content.<Yaoshou022Entity>livingEntity(row.id(),
                        (type, level) -> new Yaoshou022Entity(type, level, row), () -> Yaoshou022Entity.attributes(row));
                case "yaoshou_025" -> content.<Yaoshou025Entity>livingEntity(row.id(),
                        (type, level) -> new Yaoshou025Entity(type, level, row), () -> Yaoshou025Entity.attributes(row));
                case "yaoshou_027" -> content.<Yaoshou027Entity>livingEntity(row.id(),
                        (type, level) -> new Yaoshou027Entity(type, level, row), () -> Yaoshou027Entity.attributes(row));
                case "yaoshou_029" -> content.<Yaoshou029Entity>livingEntity(row.id(),
                        (type, level) -> new Yaoshou029Entity(type, level, row), () -> Yaoshou029Entity.attributes(row));
                default -> throw new IllegalStateException("未知妖兽身份：" + row.id());
            }
        }
        List<EggRow> eggs = Stream.of("001_002", "003_018", "019_036", "037_054")
                .flatMap(batch -> ContentFiles.read("content/creature/yaoshou_" + batch + "_eggs.json", EggRow.CODEC).stream())
                .filter(row -> INTEGRATED.contains(row.id())).toList();
        for (EggRow egg : eggs) {
            content.itemFactory("creature:spawn_egg_" + egg.id(), (properties, tooltip) -> new DeferredSpawnEggItem(
                    content.entityType(egg.id()), egg.baseColor(), egg.spotColor(), properties));
        }
        modBus.addListener((RegisterSpawnPlacementsEvent event) -> {
            for (EntityModelRow row : models()) {
                boolean aquatic = row.id().equals("yaoshou_009") || row.id().equals("yaoshou_029");
                event.register(content.<YaoshouEntity>entityType(row.id()).get(),
                        aquatic ? SpawnPlacementTypes.IN_WATER : SpawnPlacementTypes.ON_GROUND,
                        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                        (type, level, reason, pos, random) -> aquatic
                                ? level.getBlockState(pos).is(Blocks.WATER) && level.getBlockState(pos.above()).is(Blocks.WATER)
                                : level.getBlockState(pos.below()).is(GRASS_MATERIAL) && level.getRawBrightness(pos, 0) > 8,
                        RegisterSpawnPlacementsEvent.Operation.REPLACE);
            }
        });
    }

    private record EggRow(String id, int baseColor, int spotColor) {
        private static final Codec<EggRow> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(EggRow::id),
                Codec.INT.fieldOf("base_color").forGetter(EggRow::baseColor),
                Codec.INT.fieldOf("spot_color").forGetter(EggRow::spotColor)).apply(instance, EggRow::new));
    }
}
