package net.yvanchuxiuzhen.system.craft.content;

import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/** 工艺菜单分别绑定设备库存，打开数据只携带设备坐标。 */
public final class CraftMenus {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ModId.VALUE);
    public static final Supplier<MenuType<StoneMillMenu>> STONE_MILL = MENUS.register("stonemill_gui",
            () -> IMenuTypeExtension.create(StoneMillMenu::new));

    public static final Supplier<MenuType<CarpentryMenu>> CARPENTRY = MENUS.register("mujiang_gui",
            () -> IMenuTypeExtension.create(CarpentryMenu::new));

    public static final Supplier<MenuType<PotteryWheelMenu>> POTTERY_WHEEL = MENUS.register("taolun_gui",
            () -> IMenuTypeExtension.create(PotteryWheelMenu::new));

    private CraftMenus() {
    }

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
    }
}
