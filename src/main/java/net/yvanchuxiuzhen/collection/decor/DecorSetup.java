package net.yvanchuxiuzhen.collection.decor;

import net.neoforged.bus.api.IEventBus;
import net.yvanchuxiuzhen.platform.content.ContentTables;

/**
 * 装饰内容族的启动装配，只在 Mod 构造时调用一次。
 */
public final class DecorSetup {
    private static final ContentTables CONTENT = new ContentTables("decor");

    private DecorSetup() {
    }

    public static void register(IEventBus modBus) {
        CONTENT.register(modBus);
    }
}
