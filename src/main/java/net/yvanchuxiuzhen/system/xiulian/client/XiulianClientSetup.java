package net.yvanchuxiuzhen.system.xiulian.client;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.yvanchuxiuzhen.system.xiulian.content.XiulianMenus;

/** 修炼系统的客户端装配：出身界面。 */
public final class XiulianClientSetup {
    private XiulianClientSetup() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener(XiulianClientSetup::registerScreens);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(XiulianMenus.BIRTH.get(), BirthScreen::new);
    }
}
