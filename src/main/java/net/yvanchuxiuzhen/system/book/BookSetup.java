package net.yvanchuxiuzhen.system.book;

import net.neoforged.bus.api.IEventBus;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.system.book.content.StudyBookItem;

/**
 * 书籍系统的启动装配，只在 Mod 构造时调用一次。
 */
public final class BookSetup {
    private static final ContentTables CONTENT = new ContentTables("book").itemFactory("book:study_book", StudyBookItem::new);

    private BookSetup() {
    }

    public static void register(IEventBus modBus) {
        CONTENT.register(modBus);
    }
}
