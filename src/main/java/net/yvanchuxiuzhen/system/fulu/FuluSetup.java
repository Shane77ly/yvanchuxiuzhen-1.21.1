package net.yvanchuxiuzhen.system.fulu;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.platform.content.ContentTables;
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

    private FuluSetup() {
    }

    public static void register(IEventBus modBus) {
        CONTENT.register(modBus);
        FuluData.register(modBus);
        FuluBlockEntities.register(modBus);
        FuluMenus.register(modBus);
        InheritanceContent.register(modBus);
        NeoForge.EVENT_BUS.addListener(DrawingTableRuntime::rightClick);
        modBus.addListener(FuluSetup::payloads);
    }

    private static void payloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(DrawingTableButtonPayload.TYPE, DrawingTableButtonPayload.STREAM_CODEC, DrawingTableButtonPayload::handle);
        event.registrar("1").playToServer(AshBurnerHelpPayload.TYPE, AshBurnerHelpPayload.STREAM_CODEC, AshBurnerHelpPayload::handle);
    }
}
