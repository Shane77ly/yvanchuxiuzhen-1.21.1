package net.yvanchuxiuzhen.system.combat.effect;

import net.yvanchuxiuzhen.platform.content.ContentTables;

/** 战斗系统状态效果的行为登记，按效果的作用分组。 */
public final class CombatEffects {
    private CombatEffects() {
    }

    public static void register(ContentTables tables) {
        ControlEffects.register(tables);
        CultivationEffects.register(tables);
    }
}
