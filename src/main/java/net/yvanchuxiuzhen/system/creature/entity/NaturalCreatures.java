package net.yvanchuxiuzhen.system.creature.entity;

import java.util.List;
import java.util.stream.Stream;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.ContentFiles;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.system.creature.entity.xiuzhe.Xiuzhe0101Entity;

/** 魔兽、桃木精与修者的工厂、来源刷怪蛋和生成位置规则。 */
public final class NaturalCreatures {
    private static final TagKey<Block> GRASS_MATERIAL = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "legacy_grass_material"));
    private NaturalCreatures() { }

    public static List<EntityModelRow> models() {
        return Stream.of("moshou", "taomujing", "xiuzhe")
                .flatMap(name -> ContentFiles.read("content/creature/" + name + "_models.json", EntityModelRow.CODEC).stream())
                .toList();
    }

    public static void register(ContentTables content, IEventBus modBus) {
        content.<Moshou001Entity>livingEntity("moshou_001", Moshou001Entity::new, () -> MoshouEntity.attributes(.2));
        content.<Moshou003Entity>livingEntity("moshou_003", Moshou003Entity::new, () -> MoshouEntity.attributes(.3));
        content.<TaomujingEntity>livingEntity("taomujing", TaomujingEntity::new, TaomujingEntity::attributes);
        content.<Xiuzhe0101Entity>livingEntity("xiuzhe_0101", Xiuzhe0101Entity::new, Xiuzhe0101Entity::attributes);
        content.itemFactory("creature:spawn_egg_moshou_001", (properties, tooltip) -> new DeferredSpawnEggItem(
                content.entityType("moshou_001"), -16777114, -26368, properties));
        content.itemFactory("creature:spawn_egg_moshou_003", (properties, tooltip) -> new DeferredSpawnEggItem(
                content.entityType("moshou_003"), -10092544, -3407872, properties));
        content.itemFactory("creature:spawn_egg_taomujing", (properties, tooltip) -> new DeferredSpawnEggItem(
                content.entityType("taomujing"), -11058137, -5668269, properties));
        content.itemFactory("creature:spawn_egg_xiuzhe_0101", (properties, tooltip) -> new DeferredSpawnEggItem(
                content.entityType("xiuzhe_0101"), -11513816, -8622766, properties));
        modBus.addListener((RegisterSpawnPlacementsEvent event) -> {
            for (String id : List.of("moshou_001", "moshou_003")) {
                event.register(content.<MoshouEntity>entityType(id).get(), SpawnPlacementTypes.ON_GROUND,
                        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (type, level, reason, pos, random) ->
                                level.getDifficulty() != Difficulty.PEACEFUL && Monster.isDarkEnoughToSpawn(level, pos, random)
                                        && Mob.checkMobSpawnRules(type, level, reason, pos, random),
                        RegisterSpawnPlacementsEvent.Operation.REPLACE);
            }
            event.register(content.<TaomujingEntity>entityType("taomujing").get(), SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (type, level, reason, pos, random) ->
                            level.getBlockState(pos.below()).is(GRASS_MATERIAL) && level.getRawBrightness(pos, 0) > 8,
                    RegisterSpawnPlacementsEvent.Operation.REPLACE);
            event.register(content.<Xiuzhe0101Entity>entityType("xiuzhe_0101").get(), SpawnPlacementTypes.ON_GROUND,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (type, level, reason, pos, random) ->
                            level.getBlockState(pos.below()).is(GRASS_MATERIAL) && level.getRawBrightness(pos, 0) > 8,
                    RegisterSpawnPlacementsEvent.Operation.REPLACE);
        });
    }
}
