package net.yvanchuxiuzhen.system.fulu;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.platform.content.ContentFiles;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.platform.content.entity.EntityModelRow;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;
import net.yvanchuxiuzhen.system.fulu.content.AshBurnerBlock;
import net.yvanchuxiuzhen.system.fulu.content.DrawingTableBlock;
import net.yvanchuxiuzhen.system.fulu.content.FuluBlockEntities;
import net.yvanchuxiuzhen.system.fulu.content.FuluMenus;
import net.yvanchuxiuzhen.system.fulu.content.SootBlock;
import net.yvanchuxiuzhen.system.fulu.content.TalismanItem;
import net.yvanchuxiuzhen.system.fulu.content.RangedTalismanItem;
import net.yvanchuxiuzhen.system.fulu.content.TalismanBeast;
import net.yvanchuxiuzhen.system.fulu.content.TalismanProjectile;
import net.yvanchuxiuzhen.system.fulu.content.TalismanSummon;
import net.yvanchuxiuzhen.system.fulu.content.TalismanProjectile.Kind;
import net.yvanchuxiuzhen.system.fulu.content.InheritanceBlock;
import net.yvanchuxiuzhen.system.fulu.content.InheritanceBlockItem;
import net.yvanchuxiuzhen.system.fulu.content.InheritanceContent;
import net.yvanchuxiuzhen.system.fulu.data.FuluData;
import net.yvanchuxiuzhen.system.fulu.network.AshBurnerHelpPayload;
import net.yvanchuxiuzhen.system.fulu.network.DrawingTableButtonPayload;

/**
 * 符箓系统的启动装配，只在 Mod 构造时调用一次。
 */
public final class FuluSetup {
    private static final ContentTables CONTENT = new ContentTables("fulu")
            .blockFactory("fulu:drawing_table", traits -> ContentBlock.create(traits, DrawingTableBlock::new))
            .blockFactory("fulu:ash_burner", traits -> ContentBlock.create(traits, AshBurnerBlock::new))
            .blockFactory("fulu:soot", traits -> ContentBlock.create(traits, SootBlock::new))
            .blockFactory("fulu:inheritance", traits -> ContentBlock.create(traits, InheritanceBlock::new))
            .itemFactory("fulu:inheritance", (properties, tooltip) -> new InheritanceBlockItem(
                    BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "chuancheng")), properties, tooltip));

    /** 共享施用过程驱动的持续效果符编号，对应内容表工厂 {@code fulu:talisman_<编号>}。 */
    /** 蓄放后射出弹体的远程符编号，对应内容表工厂 {@code fulu:ranged_<编号>}。 */
    private static final int[] RANGED = {10, 20, 28, 33, 35};
    private static final int[] TALISMANS = {1, 2, 3, 4, 5, 6, 7, 8, 9, 11, 12, 13, 14, 17, 19, 21, 24, 25, 26, 27, 29, 31, 32, 34, 36, 38, 39, 46};

    static {
        for (int number : TALISMANS) {
            CONTENT.itemFactory("fulu:talisman_%02d".formatted(number), (properties, tooltip) -> new TalismanItem(properties, tooltip, number));
        }
        for (int number : RANGED) {
            CONTENT.itemFactory("fulu:ranged_%02d".formatted(number), (properties, tooltip) -> new RangedTalismanItem(properties, tooltip, number));
        }
    }

    static {
        for (Kind kind : Kind.values()) {
            CONTENT.<TalismanProjectile>entity(kind.id(), (type, level) -> new TalismanProjectile(type, level, kind));
        }
    }

    /** 动画召唤物的模型与属性表，只含已接回的四种。 */
    private static final Map<String, EntityModelRow> BEASTS = new LinkedHashMap<>();

    static {
        for (EntityModelRow row : ContentFiles.read("content/fulu/entity_models.json", EntityModelRow.CODEC)) {
            if (List.of(TalismanBeast.THUNDER_PULL, TalismanBeast.EARTH_HAND, TalismanBeast.SWORD_SOUL, TalismanBeast.FATE_WHEEL, TalismanBeast.WATER_DRAGON).contains(row.id())) {
                BEASTS.put(row.id(), row);
                CONTENT.<TalismanBeast>livingEntity(row.id(), (type, level) -> new TalismanBeast(type, level, row), () -> TalismanBeast.attributes(row));
            }
        }
        CONTENT.<TalismanSummon>livingEntity(TalismanSummon.ID, TalismanSummon::new, TalismanSummon::attributes);
    }

    private FuluSetup() {
    }

    @SuppressWarnings("unchecked")
    public static Supplier<EntityType<TalismanProjectile>> projectile(Kind kind) {
        return CONTENT.entityType(kind.id());
    }

    public static Map<String, EntityModelRow> beasts() {
        return BEASTS;
    }

    public static Supplier<EntityType<TalismanBeast>> beast(String id) {
        return CONTENT.entityType(id);
    }

    public static Supplier<EntityType<TalismanSummon>> summon() {
        return CONTENT.entityType(TalismanSummon.ID);
    }

    public static void register(IEventBus modBus) {
        CONTENT.register(modBus);
        FuluData.register(modBus);
        FuluBlockEntities.register(modBus);
        FuluMenus.register(modBus);
        InheritanceContent.register(modBus);
        NeoForge.EVENT_BUS.addListener(DrawingTableRuntime::rightClick);
        FuluTick.register();
        modBus.addListener(FuluSetup::payloads);
    }

    private static void payloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(DrawingTableButtonPayload.TYPE, DrawingTableButtonPayload.STREAM_CODEC, DrawingTableButtonPayload::handle);
        event.registrar("1").playToServer(AshBurnerHelpPayload.TYPE, AshBurnerHelpPayload.STREAM_CODEC, AshBurnerHelpPayload::handle);
    }
}
