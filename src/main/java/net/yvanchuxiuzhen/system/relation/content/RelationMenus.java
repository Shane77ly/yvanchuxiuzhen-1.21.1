package net.yvanchuxiuzhen.system.relation.content;

import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/** 关系系统的菜单类型：好友界面 {@code friendgui} 与好友申请界面 {@code zuduiteam_gui}（来源注册名）。 */
public final class RelationMenus {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ModId.VALUE);
    public static final Supplier<MenuType<FriendMenu>> FRIEND = MENUS.register("friendgui",
            () -> new MenuType<>(FriendMenu::new, FeatureFlags.VANILLA_SET));
    public static final Supplier<MenuType<TeamRequestMenu>> TEAM_REQUEST = MENUS.register("zuduiteam_gui",
            () -> new MenuType<>(TeamRequestMenu::new, FeatureFlags.VANILLA_SET));

    private RelationMenus() {
    }

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
    }
}
