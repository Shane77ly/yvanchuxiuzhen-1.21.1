package net.yvanchuxiuzhen.system.xiulian.content;

import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/** 修炼系统的菜单类型：出身界面 {@code player_join_gui}、升级界面 {@code playershengjixianshi}（来源注册名）。 */
public final class XiulianMenus {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ModId.VALUE);
    public static final Supplier<MenuType<BirthMenu>> BIRTH = MENUS.register("player_join_gui",
            () -> new MenuType<>(BirthMenu::new, FeatureFlags.VANILLA_SET));
    public static final Supplier<MenuType<LevelUpMenu>> LEVEL_UP = MENUS.register("playershengjixianshi",
            () -> new MenuType<>(LevelUpMenu::new, FeatureFlags.VANILLA_SET));

    private XiulianMenus() {
    }

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
    }
}
