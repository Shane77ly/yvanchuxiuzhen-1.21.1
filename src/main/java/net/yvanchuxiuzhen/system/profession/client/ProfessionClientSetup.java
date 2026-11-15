package net.yvanchuxiuzhen.system.profession.client;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.yvanchuxiuzhen.system.profession.content.ProfessionMenus;

/** 副职业页的客户端装配。 */
public final class ProfessionClientSetup {
    private ProfessionClientSetup() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener((RegisterMenuScreensEvent event) -> event.register(ProfessionMenus.PAGE.get(), ProfessionScreen::new));
    }
}
