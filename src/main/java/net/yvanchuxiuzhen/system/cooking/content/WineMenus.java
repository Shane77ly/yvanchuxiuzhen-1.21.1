package net.yvanchuxiuzhen.system.cooking.content;

import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/** 木甑菜单保留来源注册身份，打开数据仅携带方块坐标。 */
public final class WineMenus {
    private static final DeferredRegister<MenuType<?>> TYPES = DeferredRegister.create(Registries.MENU, ModId.VALUE);
    public static final Supplier<MenuType<WoodenStillMenu>> WOODEN_STILL = TYPES.register("foodmuzeng_gui",
            () -> IMenuTypeExtension.create(WoodenStillMenu::new));

    private WineMenus() {
    }

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
    }
}
