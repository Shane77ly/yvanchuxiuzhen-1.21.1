package net.yvanchuxiuzhen.system.craft;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;
import net.yvanchuxiuzhen.system.craft.content.CraftBlockEntities;
import net.yvanchuxiuzhen.system.craft.content.CraftMenus;
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

    public static void register(IEventBus modBus) {
        CONTENT.register(modBus);
        CraftBlockEntities.register(modBus);
        CraftMenus.register(modBus);
        modBus.addListener((RegisterPayloadHandlersEvent event) -> event.registrar("1").playToServer(
                CarpentryButtonPayload.TYPE, CarpentryButtonPayload.STREAM_CODEC, CarpentryButtonPayload::handle));
        NeoForge.EVENT_BUS.addListener(CraftCommands::register);
        NeoForge.EVENT_BUS.addListener(CarpentryInteractions::use);
    }
}
