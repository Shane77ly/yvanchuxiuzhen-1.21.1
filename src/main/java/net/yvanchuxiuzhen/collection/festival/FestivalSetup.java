package net.yvanchuxiuzhen.collection.festival;

import java.util.function.Supplier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.yvanchuxiuzhen.platform.content.ContentFiles;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;

/**
 * 节日内容族的启动装配，只在 Mod 构造时调用一次。
 */
public final class FestivalSetup {
    private static final ContentTables CONTENT = new ContentTables("festival");

    private FestivalSetup() {
    }

    public static void register(IEventBus modBus) {
        EntityModelRow model = caishenyeModel();
        CONTENT.<Caishenye>livingEntity("caishenye", (type, level) -> new Caishenye(type, level, model), model::attributes);
        CONTENT.itemFactory("festival:caishenye_spawn_egg", (properties, tooltip) ->
                new DeferredSpawnEggItem(CONTENT.entityType("caishenye"), -3407872, -13261, properties));
        CONTENT.register(modBus);
        modBus.addListener((RegisterSpawnPlacementsEvent event) -> event.register(
                CONTENT.<Caishenye>entityType("caishenye").get(), SpawnPlacementTypes.ON_GROUND,
                Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (type, level, reason, pos, random) -> Caishenye.naturalSpawnAllowed(),
                RegisterSpawnPlacementsEvent.Operation.REPLACE));
    }

    public static Supplier<EntityType<Caishenye>> caishenyeType() {
        return CONTENT.entityType("caishenye");
    }

    public static EntityModelRow caishenyeModel() {
        return ContentFiles.read("content/festival/caishenye_models.json", EntityModelRow.CODEC).getFirst();
    }
}
