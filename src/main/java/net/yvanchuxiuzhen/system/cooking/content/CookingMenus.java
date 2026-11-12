package net.yvanchuxiuzhen.system.cooking.content;

import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/** 铁锅和砂锅菜单使用各自注册身份，额外打开数据只携带方块位置。 */
public final class CookingMenus {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ModId.VALUE);
    public static final Supplier<MenuType<CookingPotMenu>> IRON = MENUS.register("tieguo_gui",
            () -> IMenuTypeExtension.create((id, inventory, buffer) -> new CookingPotMenu(id, inventory, buffer, true)));
    public static final Supplier<MenuType<CookingPotMenu>> CLAY = MENUS.register("shaguo_gui",
            () -> IMenuTypeExtension.create((id, inventory, buffer) -> new CookingPotMenu(id, inventory, buffer, false)));

    private CookingMenus() {
    }

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
    }
}
