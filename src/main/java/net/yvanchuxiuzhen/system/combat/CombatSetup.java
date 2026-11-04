package net.yvanchuxiuzhen.system.combat;

import net.neoforged.bus.api.IEventBus;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.system.combat.effect.CombatEffects;

/**
 * 战斗系统的启动装配，只在 Mod 构造时调用一次。
 */
public final class CombatSetup {
    private static final ContentTables CONTENT = new ContentTables("combat");

    private CombatSetup() {
    }

    public static void register(IEventBus modBus) {
        CombatEffects.register(CONTENT);
        CONTENT.register(modBus);
    }
}
