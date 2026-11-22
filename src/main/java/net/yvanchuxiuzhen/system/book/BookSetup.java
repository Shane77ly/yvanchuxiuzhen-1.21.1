package net.yvanchuxiuzhen.system.book;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;
import net.yvanchuxiuzhen.system.book.content.BookBindingBlock;
import net.yvanchuxiuzhen.system.book.content.BookBlockEntities;
import net.yvanchuxiuzhen.system.book.content.BookMenus;
import net.yvanchuxiuzhen.system.book.content.StudyBookItem;
import net.yvanchuxiuzhen.system.book.data.BookQuizData;

/**
 * 书籍系统的启动装配，只在 Mod 构造时调用一次。
 */
public final class BookSetup {
    private static final ContentTables CONTENT = new ContentTables("book")
            .blockFactory("book:binding_table", traits -> ContentBlock.create(traits, BookBindingBlock::new))
            .itemFactory("book:study_book", StudyBookItem::new);

    private BookSetup() {
    }

    public static void register(IEventBus modBus) {
        CONTENT.register(modBus);
        BookQuizData.register(modBus);
        BookMenus.register(modBus);
        BookBlockEntities.register(modBus);
        NeoForge.EVENT_BUS.addListener(BookBindingRuntime::tick);
    }
}
