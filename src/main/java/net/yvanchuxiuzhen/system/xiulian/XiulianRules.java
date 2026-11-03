package net.yvanchuxiuzhen.system.xiulian;

import net.minecraft.world.level.GameRules;

/**
 * 修炼系统的游戏规则（来源注册名不变）：{@code linggenlevel} 随机灵根的上限（默认 9），
 * {@code jinruyouxichushenxvanze} 首次进入游戏时是否随机灵根并打开出身界面（默认真）。
 */
public final class XiulianRules {
    public static final GameRules.Key<GameRules.IntegerValue> ROOT_LEVEL =
            GameRules.register("linggenlevel", GameRules.Category.PLAYER, GameRules.IntegerValue.create(9));
    public static final GameRules.Key<GameRules.BooleanValue> BIRTH_SELECTION =
            GameRules.register("jinruyouxichushenxvanze", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));

    private XiulianRules() {
    }

    /** 在 Mod 构造时触发类加载，使规则在世界读取前登记。 */
    public static void register() {
    }
}
