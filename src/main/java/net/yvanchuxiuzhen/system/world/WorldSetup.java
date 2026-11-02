package net.yvanchuxiuzhen.system.world;

import net.neoforged.bus.api.IEventBus;
import net.yvanchuxiuzhen.platform.content.ContentTables;

/**
 * 世界系统的启动装配，只在 Mod 构造时调用一次。
 */
public final class WorldSetup {
    private static final ContentTables CONTENT = new ContentTables("world");

    private WorldSetup() {
    }

    public static void register(IEventBus modBus) {
        CONTENT.register(modBus);
    }
}
