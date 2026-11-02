package net.yvanchuxiuzhen.system.fulu;

import net.neoforged.bus.api.IEventBus;
import net.yvanchuxiuzhen.platform.content.ContentTables;

/**
 * 符箓系统的启动装配，只在 Mod 构造时调用一次。
 */
public final class FuluSetup {
    private static final ContentTables CONTENT = new ContentTables("fulu");

    private FuluSetup() {
    }

    public static void register(IEventBus modBus) {
        CONTENT.register(modBus);
    }
}
