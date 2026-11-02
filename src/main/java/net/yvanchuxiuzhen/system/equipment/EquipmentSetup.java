package net.yvanchuxiuzhen.system.equipment;

import net.neoforged.bus.api.IEventBus;
import net.yvanchuxiuzhen.platform.content.ContentTables;

/**
 * 装备系统的启动装配，只在 Mod 构造时调用一次。
 */
public final class EquipmentSetup {
    private static final ContentTables CONTENT = new ContentTables("equipment");

    private EquipmentSetup() {
    }

    public static void register(IEventBus modBus) {
        CONTENT.register(modBus);
    }
}
