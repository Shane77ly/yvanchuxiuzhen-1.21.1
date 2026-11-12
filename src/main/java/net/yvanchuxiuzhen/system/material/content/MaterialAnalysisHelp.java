package net.yvanchuxiuzhen.system.material.content;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/** 千材录两个说明按钮按来源顺序发送属性换算与材料等级品阶说明。 */
public final class MaterialAnalysisHelp {
    private MaterialAnalysisHelp() {
    }

    public static void attributes(ServerPlayer player) {
        player.displayClientMessage(Component.literal("众所周知，炼器材料会给炼制的物品提供属性加持，比如精铁锭可以增加武器的物理攻击和法术攻击。"), false);
        player.displayClientMessage(Component.literal("但是，材料并不是直接增加物理攻击等属性，而是通过增加属性对应的积累值，积累值会每100点转换为若干点属性。"), false);
        player.displayClientMessage(Component.literal("举个例子：1级精铁锭增加30点物理攻击累积值，所以需要4个精铁锭才能让累积值达到100点以上，从而转换为1点攻击力。"), false);
        player.displayClientMessage(Component.literal("如果只用三个精铁锭，那么物理攻击累积值只有90点，将不足以转换成攻击力，所以不会有任何效果。"), false);
        player.displayClientMessage(Component.literal("§6血量上限§b累积值每100点，转换为§65§b点血量上限"), false);
        player.displayClientMessage(Component.literal("§6灵力上限§b累积值每100点，转换为§640§b点灵力上限"), false);
        player.displayClientMessage(Component.literal("§6精力上限§b累积值每100点，转换为§640§b点精力上限"), false);
        player.displayClientMessage(Component.literal("§6物理攻击§b累积值每100点，转换为§61§b点物理攻击"), false);
        player.displayClientMessage(Component.literal("§6物理防御§b累积值每100点，转换为§61§b点物理防御"), false);
        player.displayClientMessage(Component.literal("§6法术攻击§b累积值每100点，转换为§61§b点法术攻击"), false);
        player.displayClientMessage(Component.literal("§6法术防御§b累积值每100点，转换为§61§b点法术防御"), false);
        player.displayClientMessage(Component.literal("§6精神攻击§b累积值每100点，转换为§61§b点精神攻击"), false);
        player.displayClientMessage(Component.literal("§6精神防御§b累积值每100点，转换为§61§b点精神防御"), false);
        player.displayClientMessage(Component.literal("§6护甲值累§b积值每100点，转换为§61§b点护甲"), false);
        player.displayClientMessage(Component.literal("§6护甲韧性§b累积值每100点，转换为§61§b点护甲韧性"), false);
        player.displayClientMessage(Component.literal("§6移动速度§b累积值每100点，转换为§65%§b的移速加成"), false);
        player.displayClientMessage(Component.literal("§6攻击速度§b累积值每100点，转换为§65%§b的攻击速度"), false);
        player.displayClientMessage(Component.literal("§6击退抗性§b累积值每100点，转换为§65%§b的击退抗性"), false);
        player.displayClientMessage(Component.literal("§6各类附魔§b累积值每100点，转换为§61%§b级的附魔效果"), false);
    }

    public static void levels(ServerPlayer player) {
        player.displayClientMessage(Component.literal("炼器材料拥有等级与品阶两种属性，等级区间为1-40，每个等级又分五个品阶，分别是次品、下品、中品、上品和极品。"), false);
        player.displayClientMessage(Component.literal("炼器材料增加某个属性的累积值不是固定的，而是根据材料的等级与品阶来计算的。等级越高，品阶越好，增加的累积值就越多。"), false);
    }
}
