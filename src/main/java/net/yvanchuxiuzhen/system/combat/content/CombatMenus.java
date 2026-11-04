package net.yvanchuxiuzhen.system.combat.content;

import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/** 战斗系统的菜单类型：木人桩设置界面 {@code murenzhuang_gui}（来源注册名）。 */
public final class CombatMenus {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ModId.VALUE);
    public static final Supplier<MenuType<DummyMenu>> DUMMY = MENUS.register("murenzhuang_gui", () -> IMenuTypeExtension.create(DummyMenu::new));

    private CombatMenus() {
    }

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
    }
}
