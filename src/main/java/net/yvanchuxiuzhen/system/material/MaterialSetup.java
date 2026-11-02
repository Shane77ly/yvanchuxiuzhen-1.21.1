package net.yvanchuxiuzhen.system.material;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.yvanchuxiuzhen.platform.Namespaces;

/**
 * 材料系统的启动装配，只在 Mod 构造时调用一次。
 */
public final class MaterialSetup {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Namespaces.XIUZHEN);

    private MaterialSetup() {
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
