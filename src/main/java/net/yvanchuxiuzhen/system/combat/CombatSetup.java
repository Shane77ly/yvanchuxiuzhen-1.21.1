package net.yvanchuxiuzhen.system.combat;

import net.neoforged.bus.api.IEventBus;
import net.yvanchuxiuzhen.platform.content.ContentTables;
import net.yvanchuxiuzhen.platform.content.block.ContentBlock;
import net.yvanchuxiuzhen.system.combat.content.CrackedBedrockBlock;
import net.yvanchuxiuzhen.system.combat.damage.HurtHandler;
import net.yvanchuxiuzhen.system.combat.damage.ShanghaiCommand;
import net.yvanchuxiuzhen.system.combat.effect.CombatEffects;
import net.yvanchuxiuzhen.system.combat.enchant.BedrockCracking;
import net.yvanchuxiuzhen.system.combat.enchant.ChainMining;

/**
 * 战斗系统的启动装配，只在 Mod 构造时调用一次。登记状态效果的行为、伤害事件处理与命令 {@code /shanghai}、连锁采集与穿石。
 */
public final class CombatSetup {
    private static final ContentTables CONTENT = new ContentTables("combat")
            .blockFactory("combat:bedrock_01", traits -> ContentBlock.create(traits, t -> new CrackedBedrockBlock(t, "bedrock_02")))
            .blockFactory("combat:bedrock_02", traits -> ContentBlock.create(traits, t -> new CrackedBedrockBlock(t, "bedrock_03")));

    private CombatSetup() {
    }

    public static void register(IEventBus modBus) {
        CombatEffects.register(CONTENT);
        CONTENT.register(modBus);
        HurtHandler.register();
        ShanghaiCommand.register();
        ChainMining.register();
        BedrockCracking.register();
    }
}
