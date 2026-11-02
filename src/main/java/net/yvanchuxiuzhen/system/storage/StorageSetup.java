package net.yvanchuxiuzhen.system.storage;

import net.neoforged.bus.api.IEventBus;
import net.yvanchuxiuzhen.platform.content.ContentTables;

/**
 * 储物系统的启动装配，只在 Mod 构造时调用一次。
 */
public final class StorageSetup {
    private static final ContentTables CONTENT = new ContentTables("storage");

    private StorageSetup() {
    }

    public static void register(IEventBus modBus) {
        CONTENT.register(modBus);
    }
}
