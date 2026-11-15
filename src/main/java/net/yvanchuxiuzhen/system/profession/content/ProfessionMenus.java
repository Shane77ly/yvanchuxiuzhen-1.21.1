package net.yvanchuxiuzhen.system.profession.content;

import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/** 副职业页菜单注册，菜单不拥有职业进度。 */
public final class ProfessionMenus {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ModId.VALUE);
    public static final Supplier<MenuType<ProfessionMenu>> PAGE = MENUS.register("player_gui_05",
            () -> new MenuType<>(ProfessionMenu::new, FeatureFlags.VANILLA_SET));

    private ProfessionMenus() {
    }

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
    }
}
