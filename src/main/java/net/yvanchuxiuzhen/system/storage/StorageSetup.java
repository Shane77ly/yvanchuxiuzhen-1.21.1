package net.yvanchuxiuzhen.system.storage;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.system.material.api.MaterialInventoryRequestEvent;
import net.yvanchuxiuzhen.system.storage.api.MaterialStorage;
import net.yvanchuxiuzhen.system.storage.content.BagInventory;
import net.yvanchuxiuzhen.system.storage.content.BagItem;
import net.yvanchuxiuzhen.system.storage.content.BagKind;
import net.yvanchuxiuzhen.system.storage.content.BagMenu;
import net.yvanchuxiuzhen.system.storage.content.BagMenus;
import net.yvanchuxiuzhen.system.storage.content.MaterialBagItem;
import net.yvanchuxiuzhen.system.storage.content.MaterialBagMenu;
import net.yvanchuxiuzhen.system.storage.data.StorageData;

/**
 * 储物系统的启动装配，只在 Mod 构造时调用一次。
 */
public final class StorageSetup {
    private static final ContentTables CONTENT = new ContentTables("storage");

    private StorageSetup() {
    }

    public static void register(IEventBus modBus) {
        BagMenus.register(modBus);
        StorageData.register(modBus);
        CONTENT.itemFactory("qiankundai_06", MaterialBagItem::new);
        for (BagKind kind : BagKind.values()) {
            CONTENT.itemFactory(kind.id(), (properties, tooltip) -> new BagItem(properties, tooltip, kind));
        }
        CONTENT.register(modBus);
        modBus.addListener((RegisterCapabilitiesEvent event) -> {
            for (BagKind kind : BagKind.values()) {
                event.registerItem(Capabilities.ItemHandler.ITEM, (stack, context) -> new BagInventory(stack, kind), kind.item());
            }
        });
        NeoForge.EVENT_BUS.addListener((PlayerTickEvent.Post event) -> {
            if (event.getEntity().containerMenu instanceof BagMenu menu
                    && !event.getEntity().getMainHandItem().is(menu.kind().item())) {
                event.getEntity().closeContainer();
            }
            if (event.getEntity() instanceof ServerPlayer player
                    && player.containerMenu instanceof MaterialBagMenu menu) menu.tick(player);
        });
        NeoForge.EVENT_BUS.addListener((MaterialInventoryRequestEvent event) -> {
            if (event.action() == MaterialInventoryRequestEvent.Action.COUNT) {
                event.respond(MaterialStorage.snapshot(event.player()).preferences().spending()
                        ? MaterialStorage.snapshot(event.player()).count(event.itemId()) : 0);
            } else {
                event.respond(event.amount() - MaterialStorage.consumeForAnalysis(event.player(), event.itemId(), event.amount()));
            }
        });
    }
}
