package net.yvanchuxiuzhen.collection.festival;

import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.yvanchuxiuzhen.platform.content.ContentFiles;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.platform.content.ItemRow;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;

/**
 * 节日内容族的启动装配，只在 Mod 构造时调用一次。
 */
public final class FestivalSetup {
    private static final ContentTables CONTENT = new ContentTables("festival");

    private FestivalSetup() {
    }

    public static void register(IEventBus modBus) {
        BlockFireworks.register(CONTENT, modBus);
        EntityModelRow model = caishenyeModel();
        CONTENT.<Caishenye>livingEntity("caishenye", (type, level) -> new Caishenye(type, level, model), model::attributes);
        CONTENT.itemFactory("festival:caishenye_spawn_egg", (properties, tooltip) ->
                new DeferredSpawnEggItem(CONTENT.entityType("caishenye"), -3407872, -13261, properties));
        CONTENT.itemFactory("festival:sparkler_02", (properties, tooltip) ->
                new HandheldFireworkItem(properties, tooltip, HandheldFireworkItem.Kind.ELECTRIC));
        CONTENT.itemFactory("festival:sparkler_03", (properties, tooltip) ->
                new HandheldFireworkItem(properties, tooltip, HandheldFireworkItem.Kind.RANDOM_COLOR));
        CONTENT.itemFactory("festival:sparkler_07", (properties, tooltip) ->
                new HandheldFireworkItem(properties, tooltip, HandheldFireworkItem.Kind.SEQUENCE_COLOR));
        EntityModelRow lantern = wishLanternModel();
        CONTENT.<WishLantern>livingEntity("fuqi_0301", (type, level) -> new WishLantern(type, level, lantern),
                () -> lantern.attributes().add(Attributes.FLYING_SPEED, lantern.lifecycle().flyingSpeed().orElseThrow()));
        CONTENT.itemFactory("festival:wish_lantern", WishLanternItem::new);
        CONTENT.<DoubleBangFirework>livingEntity("pao_0901", DoubleBangFirework::new, DoubleBangFirework::attributes);
        CONTENT.itemFactory("festival:double_bang", DoubleBangFireworkItem::new);
        CONTENT.<SimpleFireworkProjectileEntity>entity("projectile_pao_01",
                (type, level) -> new SimpleFireworkProjectileEntity(type, level, SimpleFireworkProjectileItem.Kind.PAO_01));
        CONTENT.itemFactory("festival:pao_01", (properties, tooltip) ->
                new SimpleFireworkProjectileItem(properties, tooltip, SimpleFireworkProjectileItem.Kind.PAO_01));
        CONTENT.<SimpleFireworkProjectileEntity>entity("projectile_pao_08",
                (type, level) -> new SimpleFireworkProjectileEntity(type, level, SimpleFireworkProjectileItem.Kind.PAO_08));
        CONTENT.itemFactory("festival:pao_08", (properties, tooltip) ->
                new SimpleFireworkProjectileItem(properties, tooltip, SimpleFireworkProjectileItem.Kind.PAO_08));
        CONTENT.<Pao13Entity>entity("projectile_pao_13", Pao13Entity::new);
        CONTENT.itemFactory("festival:pao_13", Pao13Item::new);
        CONTENT.itemFactory("festival:costume_gift", (properties, tooltip) ->
                new FestivalGiftItem(properties, tooltip, FestivalGiftItem.Kind.COSTUME));
        CONTENT.itemFactory("festival:red_packet", (properties, tooltip) ->
                new FestivalGiftItem(properties, tooltip, FestivalGiftItem.Kind.RED_PACKET));
        for (ItemRow row : ContentFiles.read("content/festival/items.json", ItemRow.CODEC)) {
            if (!row.factory().startsWith("festival:fortune_costume_")) continue;
            ItemRow.Armor armor = row.armor().orElseThrow();
            ArmorItem.Type type = ArmorItem.Type.valueOf(armor.slot());
            CONTENT.itemFactory(row.factory(), (properties, tooltip) -> new FortuneCostumeItem(
                    CONTENT.armorMaterial(armor.material()), type,
                    properties.durability(type.getDurability(row.durabilityMultiplier().orElseThrow())), tooltip));
        }
        for (ThrownFirework.Kind kind : ThrownFirework.Kind.values()) {
            EntityModelRow firework = thrownFireworkModels().stream().filter(row -> row.id().equals(kind.id())).findFirst().orElseThrow();
            CONTENT.<ThrownFirework>livingEntity(kind.id(), (type, level) -> new ThrownFirework(type, level, firework, kind), firework::attributes);
            CONTENT.itemFactory("festival:thrown_" + kind.name().toLowerCase(Locale.ENGLISH), (properties, tooltip) ->
                    new ThrownFireworkItem(properties, tooltip, kind));
        }
        CONTENT.register(modBus);
        NeoForge.EVENT_BUS.addListener(FestivalFireworkDelays::tick);
        NeoForge.EVENT_BUS.addListener(FestivalFireworkDelays::stopped);
        NeoForge.EVENT_BUS.addListener(EventPriority.HIGH, false, PlayerTickEvent.Post.class, FortuneCostumeRuntime::tick);
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

    public static Supplier<EntityType<WishLantern>> wishLanternType() {
        return CONTENT.entityType("fuqi_0301");
    }

    public static EntityModelRow wishLanternModel() {
        return ContentFiles.read("content/festival/wish_lantern_models.json", EntityModelRow.CODEC).getFirst();
    }

    public static Supplier<EntityType<ThrownFirework>> thrownFireworkType(ThrownFirework.Kind kind) {
        return CONTENT.entityType(kind.id());
    }

    public static List<EntityModelRow> thrownFireworkModels() {
        return ContentFiles.read("content/festival/thrown_firework_models.json", EntityModelRow.CODEC);
    }

    public static Supplier<EntityType<DoubleBangFirework>> doubleBangType() {
        return CONTENT.entityType("pao_0901");
    }

    public static Supplier<EntityType<SimpleFireworkProjectileEntity>> pao01Type() {
        return CONTENT.entityType("projectile_pao_01");
    }

    public static Supplier<EntityType<SimpleFireworkProjectileEntity>> pao08Type() {
        return CONTENT.entityType("projectile_pao_08");
    }

    public static Supplier<EntityType<Pao13Entity>> pao13Type() {
        return CONTENT.entityType("projectile_pao_13");
    }
}
