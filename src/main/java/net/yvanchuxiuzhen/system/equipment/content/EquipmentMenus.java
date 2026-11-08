package net.yvanchuxiuzhen.system.equipment.content;

import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/** 饰品页与制作台保留来源菜单身份，额外数据只携带打开坐标。 */
public final class EquipmentMenus {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ModId.VALUE);
    public static final Supplier<MenuType<AccessoryMenu>> ACCESSORIES = MENUS.register("player_gui_03",
            () -> IMenuTypeExtension.create((id, inventory, data) -> new AccessoryMenu(id, inventory, data.readBlockPos())));
    public static final Supplier<MenuType<JewelryTableMenu>> JEWELRY_TABLE = MENUS.register("gu_ishipintai",
            () -> IMenuTypeExtension.create(JewelryTableMenu::new));

    private EquipmentMenus() {
    }

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
    }
}
