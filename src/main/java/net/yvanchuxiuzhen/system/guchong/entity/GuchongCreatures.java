package net.yvanchuxiuzhen.system.guchong.entity;

import java.util.List;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.yvanchuxiuzhen.platform.content.ContentFiles;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.yvanchuxiuzhen.platform.ModId;

/** 01、03、04号蛊虫的独立工厂、来源属性和03/04地面生成规则。 */
public final class GuchongCreatures {
    private static final TagKey<Block> GRASS_MATERIAL = TagKey.create(Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "legacy_grass_material"));

    private GuchongCreatures() { }

    public static List<EntityModelRow> models() {
        return ContentFiles.read("content/guchong/entity_models.json", EntityModelRow.CODEC);
    }

    public static void register(ContentTables content, IEventBus modBus) {
        for (EntityModelRow row : models()) {
            switch (row.id()) {
                case "guchong_01" -> content.<Guchong01Entity>livingEntity(row.id(),
                        (type, level) -> new Guchong01Entity(type, level, row), row::attributes);
                case "guchong_03" -> content.<Guchong03Entity>livingEntity(row.id(),
                        (type, level) -> new Guchong03Entity(type, level, row), GuchongEntity::attributes);
                case "guchong_04" -> content.<Guchong04Entity>livingEntity(row.id(),
                        (type, level) -> new Guchong04Entity(type, level, row), GuchongEntity::attributes);
                default -> throw new IllegalStateException("未知蛊虫身份：" + row.id());
            }
        }
        modBus.addListener((RegisterSpawnPlacementsEvent event) -> {
            for (EntityModelRow row : models()) {
                if (row.id().equals("guchong_01")) continue;
                event.register(content.<GuchongEntity>entityType(row.id()).get(), SpawnPlacementTypes.ON_GROUND,
                        Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (type, level, reason, pos, random) ->
                                level.getBlockState(pos.below()).is(GRASS_MATERIAL) && level.getRawBrightness(pos, 0) > 8,
                        RegisterSpawnPlacementsEvent.Operation.REPLACE);
            }
        });
    }
}
