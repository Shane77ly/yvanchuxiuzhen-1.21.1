package net.yvanchuxiuzhen.system.liandan.content;

import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/** 炼丹菜单保留来源注册身份，打开数据携带真实丹炉位置。 */
public final class LiandanMenus {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ModId.VALUE);
    public static final Supplier<MenuType<PharmacologyMenu>> PHARMACOLOGY = MENUS.register("yaolitai",
            () -> IMenuTypeExtension.create(PharmacologyMenu::new));
    public static final Supplier<MenuType<AlchemyMenu>> ALCHEMY = MENUS.register("liandan_gui",
            () -> IMenuTypeExtension.create(AlchemyMenu::new));
    public static final Supplier<MenuType<MedicineSeparationMenu>> MEDICINE_SEPARATION = MENUS.register("fendan_gui",
            () -> IMenuTypeExtension.create((id, inventory, buffer) -> new MedicineSeparationMenu(id, inventory, buffer, false)));
    public static final Supplier<MenuType<MedicineSeparationMenu>> MEDICINE_COMBINATION = MENUS.register("hedan_gui",
            () -> IMenuTypeExtension.create((id, inventory, buffer) -> new MedicineSeparationMenu(id, inventory, buffer, true)));

    private LiandanMenus() {
    }

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
    }
}
