package net.yvanchuxiuzhen.collection.festival;

import net.neoforged.bus.api.IEventBus;
import net.yvanchuxiuzhen.platform.content.ContentTables;

/**
 * 节日内容族的启动装配，只在 Mod 构造时调用一次。
 */
public final class FestivalSetup {
    private static final ContentTables CONTENT = new ContentTables("festival");

    private FestivalSetup() {
    }

    public static void register(IEventBus modBus) {
        CONTENT.register(modBus);
    }
}
