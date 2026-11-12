package net.yvanchuxiuzhen.system.craft;

import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;
import net.yvanchuxiuzhen.system.craft.content.CraftBlockEntities;
import net.yvanchuxiuzhen.system.craft.content.CraftMenus;
import net.yvanchuxiuzhen.system.craft.content.ChrysalisDisplayBlock;
import net.yvanchuxiuzhen.system.craft.content.PotteryBlankBlock;
import net.yvanchuxiuzhen.system.craft.content.PotteryBlankItem;
import net.yvanchuxiuzhen.system.craft.content.PotteryWheelBlock;
import net.yvanchuxiuzhen.system.craft.content.PotteryWheelBlockItem;
import net.yvanchuxiuzhen.system.craft.content.SilkwormBoxBlock;
import net.yvanchuxiuzhen.system.craft.content.SilkwormBoxBlockItem;
import net.yvanchuxiuzhen.system.craft.content.SilkwormItem;
import net.yvanchuxiuzhen.system.craft.content.SilkwormMob;
import net.yvanchuxiuzhen.system.craft.network.PotteryWheelButtonPayload;
import net.yvanchuxiuzhen.system.craft.content.CarpentryBlock;
import net.yvanchuxiuzhen.system.craft.content.CarpentryBlockItem;
import net.yvanchuxiuzhen.system.craft.content.SilkReelBlock;
import net.yvanchuxiuzhen.system.craft.content.SilkReelBlockItem;
import net.yvanchuxiuzhen.system.craft.network.CarpentryButtonPayload;
import net.yvanchuxiuzhen.system.craft.content.LoomBlock;
import net.yvanchuxiuzhen.system.craft.content.LoomBlockItem;
import net.yvanchuxiuzhen.system.craft.content.StoneMillBlock;
import net.yvanchuxiuzhen.system.craft.content.StoneMillBlockItem;

/**
 * 工艺系统的启动装配，只在 Mod 构造时调用一次。
 */
public final class CraftSetup {
    private static final ContentTables CONTENT = new ContentTables("craft")
            .blockFactory("craft:stone_mill", traits -> ContentBlock.create(traits,
                    value -> new StoneMillBlock(value, "stonemill")))
            .blockFactory("craft:stone_roller", traits -> ContentBlock.create(traits,
                    value -> new StoneMillBlock(value, "stoneroller")))
            .blockFactory("craft:pottery_wheel", traits -> ContentBlock.create(traits, PotteryWheelBlock::new))
            .blockFactory("craft:pottery_blank", traits -> ContentBlock.create(traits, PotteryBlankBlock::new))
            .blockFactory("craft:silkworm_box", traits -> ContentBlock.create(traits, SilkwormBoxBlock::new))
            .blockFactory("craft:chrysalis_display", traits -> ContentBlock.create(traits, ChrysalisDisplayBlock::new))
            .itemFactory("craft:pottery_wheel", (properties, tooltip) -> new PotteryWheelBlockItem(
                    BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "taolun")), properties, tooltip))
            .itemFactory("craft:pottery_blank", PotteryBlankItem::new)
            .itemFactory("craft:silkworm_box", (properties, tooltip) -> new SilkwormBoxBlockItem(
                    BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "canhe")), properties, tooltip))
            .itemFactory("craft:silkworm", (properties, tooltip) -> new SilkwormItem(properties, tooltip, "silkwormentity"))
            .itemFactory("craft:silkworm_gold", (properties, tooltip) -> new SilkwormItem(properties, tooltip, "silkwormentitygold"))
            .blockFactory("craft:loom", traits -> ContentBlock.create(traits, LoomBlock::new))
            .blockFactory("craft:carpentry", traits -> ContentBlock.create(traits, CarpentryBlock::new))
            .blockFactory("craft:silk_reel", traits -> ContentBlock.create(traits, SilkReelBlock::new))
            .itemFactory("craft:stone_mill", (properties, tooltip) -> new StoneMillBlockItem(
                    BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "stonemill")), properties, tooltip))
            .itemFactory("craft:stone_roller", (properties, tooltip) -> new StoneMillBlockItem(
                    BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "stoneroller")), properties, tooltip))
            .itemFactory("craft:loom", (properties, tooltip) -> new LoomBlockItem(
                    BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "zhibuji")), properties, tooltip))
            .itemFactory("craft:carpentry", (properties, tooltip) -> new CarpentryBlockItem(
                    BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "mugongtai")), properties, tooltip))
            .itemFactory("craft:silk_reel", (properties, tooltip) -> new SilkReelBlockItem(
                    BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(ModId.VALUE, "saosilun")), properties, tooltip));

    private CraftSetup() {
    }

    public static Supplier<EntityType<SilkwormMob>> silkworm(SilkwormMob.Kind kind) {
        return CONTENT.entityType(kind.id());
    }

    public static void register(IEventBus modBus) {
        for (SilkwormMob.Kind kind : SilkwormMob.Kind.values()) {
            CONTENT.<SilkwormMob>livingEntity(kind.id(), (type, level) -> new SilkwormMob(type, level, kind),
                    () -> SilkwormMob.attributes(kind));
            boolean gold = kind == SilkwormMob.Kind.GOLD_WORM || kind == SilkwormMob.Kind.GOLD_MOTH;
            CONTENT.itemFactory("craft:spawn_egg_" + kind.id(), (properties, tooltip) -> new DeferredSpawnEggItem(
                    silkworm(kind), gold ? -52 : -1, gold ? -154 : -3355444, properties));
        }
        CONTENT.register(modBus);
        CraftBlockEntities.register(modBus);
        CraftMenus.register(modBus);
        modBus.addListener((RegisterPayloadHandlersEvent event) -> event.registrar("1").playToServer(
                CarpentryButtonPayload.TYPE, CarpentryButtonPayload.STREAM_CODEC, CarpentryButtonPayload::handle));
        NeoForge.EVENT_BUS.addListener(CraftCommands::register);
        NeoForge.EVENT_BUS.addListener(CarpentryInteractions::use);
        NeoForge.EVENT_BUS.addListener(PotteryInteractions::use);
        modBus.addListener((RegisterPayloadHandlersEvent event) -> event.registrar("1").playToServer(
                PotteryWheelButtonPayload.TYPE, PotteryWheelButtonPayload.STREAM_CODEC, PotteryWheelButtonPayload::handle));
    }
}
