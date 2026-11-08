package net.yvanchuxiuzhen.system.storage.client;

import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.yvanchuxiuzhen.system.storage.content.BagKind;
import net.yvanchuxiuzhen.system.storage.content.BagMenus;

/** 储物系统原生菜单的客户端装配。 */
public final class StorageClientSetup {
    private StorageClientSetup() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener((RegisterMenuScreensEvent event) -> {
            for (BagKind kind : BagKind.values()) event.register(BagMenus.type(kind), BagScreen::new);
        });
        NeoForge.EVENT_BUS.addListener((ItemTossEvent event) -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.screen instanceof BagScreen screen && event.getEntity().getItem().is(screen.getMenu().kind().item())) {
                minecraft.player.closeContainer();
            }
        });
    }
}
