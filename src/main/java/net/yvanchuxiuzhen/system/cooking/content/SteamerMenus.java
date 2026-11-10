package net.yvanchuxiuzhen.system.cooking.content;

import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/** 保留系统侧蒸锅菜单与修真侧笼屉菜单的统一命名空间身份。 */
public final class SteamerMenus {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(BuiltInRegistries.MENU, ModId.VALUE);
    public static final Supplier<MenuType<SteamerMenu>> POT = MENUS.register("zhengguo_gui",
            () -> IMenuTypeExtension.create((id, inventory, buffer) -> new SteamerMenu(id, inventory, buffer, false)));
    public static final Supplier<MenuType<SteamerMenu>> TRAY = MENUS.register("longti_gui",
            () -> IMenuTypeExtension.create((id, inventory, buffer) -> new SteamerMenu(id, inventory, buffer, true)));

    private SteamerMenus() {
    }

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
    }
}
