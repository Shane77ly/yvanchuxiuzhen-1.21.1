package net.yvanchuxiuzhen.system.cooking;

import net.neoforged.bus.api.IEventBus;
import net.yvanchuxiuzhen.platform.content.ContentTables;

/**
 * 烹饪系统的启动装配，只在 Mod 构造时调用一次。
 */
public final class CookingSetup {
    private static final ContentTables CONTENT = new ContentTables("cooking");

    private CookingSetup() {
    }

    public static void register(IEventBus modBus) {
        CONTENT.register(modBus);
    }
}
