package net.yvanchuxiuzhen.system.creature;

import net.neoforged.bus.api.IEventBus;
import net.yvanchuxiuzhen.platform.content.ContentTables;

/**
 * 生物系统的启动装配，只在 Mod 构造时调用一次。
 */
public final class CreatureSetup {
    private static final ContentTables CONTENT = new ContentTables("creature");

    private CreatureSetup() {
    }

    public static void register(IEventBus modBus) {
        CONTENT.register(modBus);
    }
}
