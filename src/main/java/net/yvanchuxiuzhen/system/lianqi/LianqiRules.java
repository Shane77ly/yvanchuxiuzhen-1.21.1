package net.yvanchuxiuzhen.system.lianqi;

import net.minecraft.world.level.GameRules;

/**
 * 炼器系统的游戏规则。{@code weaponlevelup} 控制炼器鼎是否允许升级武器、法宝和护甲，来源默认开启。
 */
public final class LianqiRules {
    public static final GameRules.Key<GameRules.BooleanValue> WEAPON_LEVEL_UP =
            GameRules.register("weaponlevelup", GameRules.Category.MISC, GameRules.BooleanValue.create(true));

    private LianqiRules() {
    }

    /** 在 Mod 构造时触发类加载，使规则在世界读取前登记。 */
    public static void register() {
    }
}
