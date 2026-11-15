package net.yvanchuxiuzhen.system.fulu.api;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.yvanchuxiuzhen.system.fulu.FuluKnowledgeRuntime;
import net.yvanchuxiuzhen.system.fulu.data.FuluData;

/** 符箓知识的公开查询与学习入口；反馈、扣书和知识写入由符箓系统统一承担。 */
public final class Fulu {
    private Fulu() {
    }

    /** 成品奖励的品阶由符箓系统写入，沿用自然施用和提示的同一实例格式。 */
    public static ItemStack createReward(Item carrier, int level, String levelText) {
        ItemStack stack = createReward(carrier, level);
        CustomData.update(DataComponents.CUSTOM_DATA, stack, data -> data.putString("fululeveltxt", levelText));
        return stack;
    }

    /** 来源仅赋品阶数值的奖励不额外生成品阶文字字段。 */
    public static ItemStack createReward(Item carrier, int level) {
        ItemStack stack = new ItemStack(carrier);
        CompoundTag data = new CompoundTag();
        data.putInt("fululevel", level);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
        return stack;
    }

    public static FuluKnowledge knowledge(Player player) {
        return player.getData(FuluData.KNOWLEDGE).visible();
    }

    /** 按来源编号1至11解锁符头；重复学习只提示，成功不扣书并设置烧灰标志。 */
    public static boolean unlockHead(ServerPlayer player, int head) {
        return FuluKnowledgeRuntime.unlockHead(player, head);
    }

    /** 按来源编号1至8解锁符胆；成功后优先扣主手、否则扣副手带学习标志的一件物品。 */
    public static boolean unlockCore(ServerPlayer player, int core) {
        return FuluKnowledgeRuntime.unlockCore(player, core);
    }

    /** 分派原初系统命令的符头、符胆分支；返回是否识别该分支，不表示解锁成功。 */
    public static boolean command(CommandSourceStack source, String arguments) {
        return FuluKnowledgeRuntime.command(source, arguments);
    }
}
