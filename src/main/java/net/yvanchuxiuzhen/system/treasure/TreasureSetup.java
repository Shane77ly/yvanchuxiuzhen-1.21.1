package net.yvanchuxiuzhen.system.treasure;

import net.neoforged.bus.api.IEventBus;
import net.yvanchuxiuzhen.platform.content.ContentTables;

/**
 * 宝箱系统的启动装配，只在 Mod 构造时调用一次。
 */
public final class TreasureSetup {
    private static final ContentTables CONTENT = new ContentTables("treasure");

    private TreasureSetup() {
    }

    public static void register(IEventBus modBus) {
        CONTENT.register(modBus);
    }
}
