package net.yvanchuxiuzhen.system.town.client;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.yvanchuxiuzhen.system.town.content.TownMenus;

/** 城镇原生客户端页面独立于专服加载。 */
public final class TownClientSetup {
    private TownClientSetup() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(TownClientSetup::screens);
    }

    private static void screens(RegisterMenuScreensEvent event) {
        event.register(TownMenus.BUILD.get(), TownBuildScreen::new);
        for (int page = 1; page <= 5; page++) event.register(TownMenus.page(page), TownScreen::new);
    }
}
