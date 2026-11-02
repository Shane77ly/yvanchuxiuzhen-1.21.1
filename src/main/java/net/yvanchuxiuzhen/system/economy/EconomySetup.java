package net.yvanchuxiuzhen.system.economy;

import net.neoforged.bus.api.IEventBus;
import net.yvanchuxiuzhen.platform.content.ContentTables;

/**
 * 经济系统的启动装配，只在 Mod 构造时调用一次。
 */
public final class EconomySetup {
    private static final ContentTables CONTENT = new ContentTables("economy");

    private EconomySetup() {
    }

    public static void register(IEventBus modBus) {
        CONTENT.register(modBus);
    }
}
