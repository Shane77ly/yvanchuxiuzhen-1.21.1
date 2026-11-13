package net.yvanchuxiuzhen.system.book.client;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.yvanchuxiuzhen.system.book.content.BookMenus;

/** 书籍客户端装配登记来源阅读页与装订台。 */
public final class BookClientSetup {
    private BookClientSetup() {
    }

    public static void register(IEventBus modBus) {
        modBus.addListener((RegisterMenuScreensEvent event) -> {
            event.register(BookMenus.READING.get(), BookReadingScreen::new);
            event.register(BookMenus.BINDING.get(), BookBindingScreen::new);
        });
    }
}
