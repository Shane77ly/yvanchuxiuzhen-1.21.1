package net.yvanchuxiuzhen.system.book.client;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.yvanchuxiuzhen.system.book.content.BookMenus;

/** 书籍客户端装配只登记来源阅读页。 */
public final class BookClientSetup {
    private BookClientSetup() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener((RegisterMenuScreensEvent event) -> event.register(BookMenus.READING.get(), BookReadingScreen::new));
    }
}
