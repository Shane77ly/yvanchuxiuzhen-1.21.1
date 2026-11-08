package net.yvanchuxiuzhen.system.material.content;

import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.ModId;

/** 千材录的原生菜单登记；打开位置用于来源按钮的已加载区块检查。 */
public final class MaterialMenus {
    private static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ModId.VALUE);
    public static final Supplier<MenuType<MaterialAnalysisMenu>> ANALYSIS = MENUS.register("qiancailu_gui",
            () -> IMenuTypeExtension.create((id, inventory, data) -> new MaterialAnalysisMenu(id, inventory, data.readBlockPos())));

    private MaterialMenus() {
    }

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
    }
}
