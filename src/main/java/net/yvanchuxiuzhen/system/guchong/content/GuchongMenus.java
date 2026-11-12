package net.yvanchuxiuzhen.system.guchong.content;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/** 三档手持蛊罐菜单保持来源注册标识。 */
public final class GuchongMenus {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ModId.VALUE);
    private static final Map<Integer, Supplier<MenuType<TaoguanMenu>>> TYPES = new HashMap<>();

    static {
        for (int number = 1; number <= 3; number++) {
            int grade = number;
            TYPES.put(grade, MENUS.register("taoguan_0" + grade + "_gui",
                    () -> IMenuTypeExtension.create((id, inventory, buffer) -> new TaoguanMenu(id, inventory, grade, buffer.readBlockPos()))));
        }
    }

    private GuchongMenus() { }
    public static void register(IEventBus bus) { MENUS.register(bus); }
    public static MenuType<TaoguanMenu> type(int grade) { return TYPES.get(grade).get(); }
}
