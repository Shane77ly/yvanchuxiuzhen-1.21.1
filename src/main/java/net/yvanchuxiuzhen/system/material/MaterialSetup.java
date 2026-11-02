package net.yvanchuxiuzhen.system.material;

import net.neoforged.bus.api.IEventBus;
import net.yvanchuxiuzhen.platform.content.ContentTables;

/**
 * 材料系统的启动装配，只在 Mod 构造时调用一次。
 */
public final class MaterialSetup {
    private static final ContentTables CONTENT = new ContentTables("material");

    private MaterialSetup() {
    }

    public static void register(IEventBus modBus) {
        CONTENT.register(modBus);
    }
}
