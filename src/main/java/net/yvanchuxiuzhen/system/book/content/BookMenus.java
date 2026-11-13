package net.yvanchuxiuzhen.system.book.content;

import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/** 公共书阅读菜单保留来源 book_gui 身份，内容取当前主手实例。 */
public final class BookMenus {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ModId.VALUE);
    public static final Supplier<MenuType<BookReadingMenu>> READING = MENUS.register("book_gui",
            () -> IMenuTypeExtension.create((id, inventory, buffer) -> new BookReadingMenu(id)));

    private BookMenus() {
    }

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
    }

    public static void open(ServerPlayer player) {
        player.openMenu(new SimpleMenuProvider((id, inventory, owner) -> new BookReadingMenu(id),
                Component.translatable("menu.yvanchuxiuzhen.book_reading")));
    }
}
