package net.yvanchuxiuzhen.system.storage.content;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/** 保留四个来源菜单身份；客户端槽位通过菜单同步接收，不直接修改手持袋组件。 */
public final class BagMenus {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ModId.VALUE);
    private static final Map<BagKind, Supplier<MenuType<BagMenu>>> TYPES = new EnumMap<>(BagKind.class);

    static {
        for (BagKind kind : BagKind.values()) {
            TYPES.put(kind, MENUS.register(kind.menuId(), () -> IMenuTypeExtension.create((id, inventory, data) -> new BagMenu(id, inventory, kind))));
        }
    }

    private BagMenus() {
    }

    public static MenuType<BagMenu> type(BagKind kind) {
        return TYPES.get(kind).get();
    }

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
    }
}
