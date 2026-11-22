package net.yvanchuxiuzhen.system.book.content;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;
import net.yvanchuxiuzhen.system.book.data.BookQuizData;
import net.yvanchuxiuzhen.system.book.network.BookQuizButtonPayload;

/** 书籍阅读、装订与答题菜单保留来源身份，展示与库存分别由所属菜单管理。 */
public final class BookMenus {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ModId.VALUE);
    public static final Supplier<MenuType<BookReadingMenu>> READING = MENUS.register("book_gui",
            () -> IMenuTypeExtension.create((id, inventory, buffer) -> new BookReadingMenu(id)));

    public static final Supplier<MenuType<BookBindingMenu>> BINDING = MENUS.register("makebook_gui",
            () -> IMenuTypeExtension.create(BookBindingMenu::new));
    public static final Supplier<MenuType<BookQuizMenu>> QUIZ = MENUS.register("dati_gui",
            () -> IMenuTypeExtension.create(BookQuizMenu::new));

    private BookMenus() {
    }

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
        modBus.addListener(BookMenus::registerPayloads);
    }

    public static void open(ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider((id, inventory, owner) -> new BookReadingMenu(id),
                Component.translatable("menu.yvanchuxiuzhen.book_reading")));
    }

    /** 开发夹具与来源等价调用者使用的显式入口；本系统不把它接到普通书籍右键。 */
    public static void openQuiz(ServerPlayer player) {
        BookQuizData.publish(player);
        BlockPos pos = player.blockPosition();
        player.openMenu(new SimpleMenuProvider((id, inventory, owner) -> new BookQuizMenu(id, pos),
                Component.translatable("menu.yvanchuxiuzhen.book_quiz")), buffer -> buffer.writeBlockPos(pos));
    }

    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(BookQuizButtonPayload.TYPE, BookQuizButtonPayload.STREAM_CODEC, BookQuizButtonPayload::handle);
    }
}
