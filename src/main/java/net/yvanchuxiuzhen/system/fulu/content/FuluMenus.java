package net.yvanchuxiuzhen.system.fulu.content;

import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/** 制符菜单打开数据携带共享装置坐标，库存由服务端菜单同步。 */
public final class FuluMenus {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ModId.VALUE);
    public static final Supplier<MenuType<DrawingTableMenu>> DRAWING_TABLE = MENUS.register("huafu_gui", () -> IMenuTypeExtension.create(DrawingTableMenu::new));
    public static final Supplier<MenuType<AshBurnerMenu>> ASH_BURNER = MENUS.register("huafushaohui_gui", () -> IMenuTypeExtension.create(AshBurnerMenu::new));

    private FuluMenus() {
    }

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
    }
}
