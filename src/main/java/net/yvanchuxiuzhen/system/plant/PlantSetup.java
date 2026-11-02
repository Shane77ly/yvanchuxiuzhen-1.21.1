package net.yvanchuxiuzhen.system.plant;

import net.neoforged.bus.api.IEventBus;
import net.yvanchuxiuzhen.platform.content.ContentTables;

/**
 * 植物系统的启动装配，只在 Mod 构造时调用一次。
 */
public final class PlantSetup {
    private static final ContentTables CONTENT = new ContentTables("plant");

    private PlantSetup() {
    }

    public static void register(IEventBus modBus) {
        CONTENT.register(modBus);
    }
}
