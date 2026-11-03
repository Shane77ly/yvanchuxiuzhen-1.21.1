package net.yvanchuxiuzhen.system.gongfa.client;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.system.gongfa.content.GongfaBlockEntities;

/** 功法系统的客户端装配：功法书的提示文本与参悟界面。 */
public final class GongfaClientSetup {
    private GongfaClientSetup() {
    }

    public static void register(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(TechniqueTooltip::onTooltip);
        modBus.addListener(GongfaClientSetup::registerScreens);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(GongfaBlockEntities.COMPREHENSION_MENU.get(), ComprehensionScreen::new);
    }
}
