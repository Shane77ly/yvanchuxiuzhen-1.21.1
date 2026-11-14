package net.yvanchuxiuzhen.system.town.content;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/** 来源建设页与五个城镇页面身份；自然案台只进入认主或占领展示页。 */
public final class TownMenus {
    private static final DeferredRegister<MenuType<?>> TYPES = DeferredRegister.create(Registries.MENU, ModId.VALUE);
    public static final Supplier<MenuType<TownBuildMenu>> BUILD = TYPES.register("chengzhenbuild_gui", () -> IMenuTypeExtension.create(TownBuildMenu::new));
    private static final List<Supplier<MenuType<TownMenu>>> PAGES = new ArrayList<>();

    static {
        for (int page = 1; page <= 5; page++) {
            final int number = page;
            PAGES.add(TYPES.register("chengzhen_ui_0" + page, () -> IMenuTypeExtension.create(
                    (id, inventory, buffer) -> new TownMenu(id, inventory, buffer.readBlockPos(), number))));
        }
    }

    private TownMenus() {
    }

    public static MenuType<TownMenu> page(int page) {
        return PAGES.get(page - 1).get();
    }

    public static void register(IEventBus bus) {
        TYPES.register(bus);
    }
}
