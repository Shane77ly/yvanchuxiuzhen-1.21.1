package net.yvanchuxiuzhen.system.lianqi.content;

import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/** 炼器菜单打开数据携带真实鼎位置。 */
public final class LianqiMenus {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ModId.VALUE);
    public static final Supplier<MenuType<TripodMenu>> TRIPOD = MENUS.register("tripod_gui", () -> IMenuTypeExtension.create(TripodMenu::new));

    private LianqiMenus() {
    }

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
    }
}
