package net.yvanchuxiuzhen.system.lianqi.content;

import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/** 炼器装置菜单登记；打开数据携带绑定方块位置。 */
public final class LianqiMenus {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ModId.VALUE);
    public static final Supplier<MenuType<TripodMenu>> TRIPOD = MENUS.register("tripod_gui", () -> IMenuTypeExtension.create(TripodMenu::new));
    public static final Supplier<MenuType<RepairTableMenu>> REPAIR_TABLE = MENUS.register("xiulitai", () -> IMenuTypeExtension.create(RepairTableMenu::new));
    public static final Supplier<MenuType<SewingBasketMenu>> SEWING_BASKET = MENUS.register("zhenxianpoluo_gui", () -> IMenuTypeExtension.create(SewingBasketMenu::new));
    public static final Supplier<MenuType<BlueprintTableMenu>> BLUEPRINT_TABLE = MENUS.register("tiandaotuzhitai_gui", () -> IMenuTypeExtension.create(BlueprintTableMenu::new));

    private LianqiMenus() {
    }

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
    }
}
