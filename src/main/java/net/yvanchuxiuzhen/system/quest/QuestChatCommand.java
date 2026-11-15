package net.yvanchuxiuzhen.system.quest;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.MessageArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/** 来源聊天选择命令只按本维度显示名写记分板，参数树保持来源定义。 */
final class QuestChatCommand {
    private QuestChatCommand() {
    }

    static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("taskduihua")
                .then(Commands.argument("name", MessageArgument.message())
                        .then(Commands.argument("number", DoubleArgumentType.doubleArg())
                                .executes(QuestChatCommand::execute)
                                .then(Commands.argument("arguments", StringArgumentType.greedyString()).executes(QuestChatCommand::execute)))));
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        String name;
        try {
            name = MessageArgument.getMessage(context, "name").getString();
        } catch (CommandSyntaxException exception) {
            // 来源名字消息解析失败时按空字符串查找，不向执行者发送额外提示。
            name = "";
        }
        for (ServerPlayer player : context.getSource().getLevel().players()) {
            if (!player.getDisplayName().getString().equals(name)) continue;
            Scoreboard scoreboard = player.getScoreboard();
            Objective objective = scoreboard.getObjective("renwuweizhi");
            if (objective == null) objective = scoreboard.addObjective("renwuweizhi", ObjectiveCriteria.DUMMY,
                    Component.literal("renwuweizhi"), ObjectiveCriteria.RenderType.INTEGER, false, null);
            scoreboard.getOrCreatePlayerScore(player, objective).set((int) DoubleArgumentType.getDouble(context, "number"));
        }
        return 0;
    }
}
