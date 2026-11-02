package net.yvanchuxiuzhen.collection.tiandao;

import net.neoforged.bus.api.IEventBus;
import net.yvanchuxiuzhen.platform.content.ContentTables;

/**
 * 天道内容族的启动装配，只在 Mod 构造时调用一次。
 */
public final class TiandaoSetup {
    private static final ContentTables CONTENT = new ContentTables("tiandao");

    private TiandaoSetup() {
    }

    public static void register(IEventBus modBus) {
        CONTENT.register(modBus);
    }
}
